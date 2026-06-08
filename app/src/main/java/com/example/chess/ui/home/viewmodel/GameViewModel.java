package com.example.chess.ui.home.viewmodel;

import android.app.Application;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.chess.R;
import com.example.chess.database.LevelProgress;
import com.example.chess.model.Board;
import com.example.chess.model.MoveRequest;
import com.example.chess.model.Pawn;
import com.example.chess.model.Piece;
import com.example.chess.model.QuizLevel;
import com.example.chess.repository.ChessRepository;
import com.example.chess.service.StockfishService;
import com.example.chess.util.ChessUtil;
import com.example.chess.util.Constants;
import com.example.chess.util.MoveCalculator;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameViewModel extends AndroidViewModel {

    private Board board;
    private final ChessRepository repository;
    private String mode;
    private QuizLevel quizLevel;
    private long startTimeMillis;

    // Variabile per memorizzare l'ID dell'utente loggato senza dipendere da Firebase
    private String currentUserId = "guest";

    private final MutableLiveData<Integer> selectedPosition = new MutableLiveData<>(null);
    private final MutableLiveData<List<Integer>> hints = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<StatusInfo> status = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isThinking = new MutableLiveData<>(false);
    private final MutableLiveData<GameEvent> gameEvent = new MutableLiveData<>();

    private final MutableLiveData<Long> remainingTime = new MutableLiveData<>(30000L);
    private final MutableLiveData<Boolean> isTimerVisible = new MutableLiveData<>(false);

    private CountDownTimer countDownTimer;
    private int currentQuizMoveIndex = 0;
    private int quizErrorCount = 0;
    private boolean isHintActive = false;

    public interface PromotionListener { void onPieceSelected(char type); }

    public GameViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ChessRepository(application);
    }

    // Nuovo metodo per ricevere l'ID utente dall'Activity
    public void setCurrentUserId(String userId) {
        if (userId != null && !userId.isEmpty()) {
            this.currentUserId = userId;
        }
    }

    public void init(String mode, QuizLevel level, boolean timerSettingEnabled) {
        if (board != null) return;
        this.mode = mode;
        this.quizLevel = level;
        this.board = repository.getNewGame();
        this.startTimeMillis = System.currentTimeMillis();

        if ("QUIZ".equals(mode) && quizLevel != null) {
            board.setupCustomBoard(quizLevel.getInitialBoardSetup(), quizLevel.isWhiteTurnToStart());
            checkQuizCompletionAndStartTimer(timerSettingEnabled);
        } else if ("BOT".equals(mode)) {
            updateBotStatus();
        } else {
            updateLocalStatus();
        }
        refreshUI();
    }

    private void checkQuizCompletionAndStartTimer(boolean timerSettingEnabled) {
        if (!timerSettingEnabled) { isTimerVisible.setValue(false); return; }
        new Thread(() -> {
            LevelProgress p = repository.getProgress(quizLevel.getId(), getCurrentUserId());
            boolean isComp = (p != null && p.isCompleted);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (!isComp) { isTimerVisible.setValue(true); startTimer(); }
                else isTimerVisible.setValue(false);
            });
        }).start();
    }

    public void handleSquareClick(int position) {
        if (Boolean.TRUE.equals(isThinking.getValue())) return;
        if ("BOT".equals(mode) && !board.isWhiteTurn()) return;

        int row = MoveCalculator.toRow(position), col = MoveCalculator.toCol(position);
        Integer sel = selectedPosition.getValue();

        if (sel == null) {
            Piece p = board.getPiece(row, col);
            if (p != null && p.isWhite() == board.isWhiteTurn()) selectPiece(position);
        } else {
            Piece target = board.getPiece(row, col);
            if (target != null && target.isWhite() == board.isWhiteTurn()) { selectPiece(position); return; }
            processMove(sel, position);
        }
    }

    private void selectPiece(int pos) {
        selectedPosition.setValue(pos);
        if (!(isHintActive && "QUIZ".equals(mode))) {
            hints.setValue(board.getLegalMovesForPiece(pos / 8, pos % 8));
        }
    }

    private void processMove(int startPos, int endPos) {
        int sR = startPos / 8, sC = startPos % 8;
        int eR = endPos / 8, eC = endPos % 8;
        Piece moving = board.getPiece(sR, sC);
        if (moving == null) return;

        if ("QUIZ".equals(mode)) {
            handleQuizMove(sR, sC, eR, eC, moving);
        } else {
            handleStandardMove(sR, sC, eR, eC, moving);
        }
    }

    private void handleStandardMove(int sR, int sC, int eR, int eC, Piece moving) {
        if (moving instanceof Pawn && (eR == 0 || eR == 7) && board.isValidMove(sR, sC, eR, eC)) {
            triggerEvent(new GameEvent(GameEvent.Type.SHOW_PROMOTION, moving.isWhite(), type -> {
                Piece promoMoving = board.getPiece(sR, sC);
                board.movePiece(sR, sC, eR, eC, type);
                finalizeMove(sR * 8 + sC, eR * 8 + eC, promoMoving);
            }));
            return;
        }
        if (board.movePiece(sR, sC, eR, eC)) finalizeMove(sR * 8 + sC, eR * 8 + eC, moving);
        else { selectedPosition.setValue(null); hints.setValue(new ArrayList<>()); }
    }

    private void finalizeMove(int s, int e, Piece p) {
        selectedPosition.setValue(null); hints.setValue(new ArrayList<>());
        triggerEvent(new GameEvent(GameEvent.Type.ANIMATE_MOVE, s, e, p, () -> {
            updateCapturedSignal();
            if ("BOT".equals(mode)) { if (!checkEndGameBot()) playBotMove(); }
            else { updateLocalStatus(); }
        }));
    }

    private void handleQuizMove(int sR, int sC, int eR, int eC, Piece moving) {
        MoveRequest expected = quizLevel.getSolutionMoves().get(currentQuizMoveIndex);
        if (sR == expected.startRow && sC == expected.startCol && eR == expected.endRow && eC == expected.endCol) {
            stopTimer(); board.movePiece(sR, sC, eR, eC);
            int startIdx = sR * 8 + sC, endIdx = eR * 8 + eC;
            selectedPosition.setValue(null); hints.setValue(new ArrayList<>());
            triggerEvent(new GameEvent(GameEvent.Type.ANIMATE_MOVE, startIdx, endIdx, moving, () -> {
                currentQuizMoveIndex++; updateCapturedSignal();
                if (currentQuizMoveIndex < quizLevel.getSolutionMoves().size()) {
                    updateStatusText(getStr(R.string.risposta_computer), Color.WHITE); playQuizComputerMove();
                } else {
                    updateStatusText(getStr(R.string.livello_superato), Color.GREEN);
                    saveQuizProgress();
                    new Handler(Looper.getMainLooper()).postDelayed(this::finishGame, 1500);
                }
            }));
        } else {
            quizErrorCount++; int rem = quizLevel.getMaxAttempts() - quizErrorCount;
            if (rem <= 0) { stopTimer(); updateStatusText(getStr(R.string.hai_perso), Color.RED); finishGame(); }
            else { updateStatusText(getStr(R.string.mossa_errata, rem), Color.RED); isHintActive = false; selectedPosition.setValue(null); hints.setValue(new ArrayList<>()); }
        }
    }

    private void playQuizComputerMove() {
        isThinking.setValue(true);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            MoveRequest move = quizLevel.getSolutionMoves().get(currentQuizMoveIndex);
            Piece p = board.getPiece(move.startRow, move.startCol);
            board.movePiece(move.startRow, move.startCol, move.endRow, move.endCol);
            triggerEvent(new GameEvent(GameEvent.Type.ANIMATE_MOVE, move.startRow * 8 + move.startCol, move.endRow * 8 + move.endCol, p, () -> {
                currentQuizMoveIndex++; updateCapturedSignal(); updateStatusText(getStr(R.string.tocca_a_te), Color.WHITE);
                isThinking.setValue(false); startTimer();
            }));
        }, 1000);
    }

    private void playBotMove() {
        isThinking.setValue(true); updateBotStatus();
        repository.getBestMove(board.toFen(), 5).enqueue(new Callback<StockfishService.StockfishResponse>() {
            @Override public void onResponse(Call<StockfishService.StockfishResponse> call, Response<StockfishService.StockfishResponse> res) {
                if (res.isSuccessful() && res.body() != null) {
                    String m = res.body().bestmove.replace("bestmove ", "").trim();
                    if (m.length() >= 4) {
                        int s = ChessUtil.algebraicToIndex(m.substring(0, 2)), e = ChessUtil.algebraicToIndex(m.substring(2, 4));
                        Piece p = board.getPiece(s/8, s%8);
                        board.movePiece(s/8, s%8, e/8, e%8, m.length() == 5 ? m.charAt(4) : 'q');
                        triggerEvent(new GameEvent(GameEvent.Type.ANIMATE_MOVE, s, e, p, () -> {
                            isThinking.setValue(false); updateCapturedSignal(); updateBotStatus(); checkEndGameBot();
                        }));
                        return;
                    }
                }
                isThinking.setValue(false); updateBotStatus();
            }
            @Override public void onFailure(Call<StockfishService.StockfishResponse> call, Throwable t) { isThinking.setValue(false); updateBotStatus(); }
        });
    }

    private void updateBotStatus() {
        boolean w = board.isWhiteTurn(), inC = board.isKingInCheck(w);
        if (w) updateStatusText(getStr(R.string.turno_bot, getStr(R.string.bianco), inC ? getStr(R.string.scacco) : ""), inC ? Color.RED : Color.WHITE);
        else updateStatusText(getStr(R.string.bot_pensa), Color.LTGRAY);
    }

    private boolean checkEndGameBot() {
        boolean t = board.isWhiteTurn();
        if (!board.hasAnyLegalMoves(t)) {
            if (board.isKingInCheck(t)) updateStatusText(getStr(R.string.scacco_matto_vince, getStr(t ? R.string.nero : R.string.bianco).toUpperCase()), Constants.GOLDENROD);
            else updateStatusText(getStr(R.string.stallo), Color.LTGRAY);
            finishGame(); return true;
        }
        return false;
    }

    private void updateLocalStatus() {
        boolean w = board.isWhiteTurn(), inC = board.isKingInCheck(w);
        if (!board.hasAnyLegalMoves(w)) {
            if (inC) updateStatusText(getStr(R.string.scacco_matto_vince, getStr(w ? R.string.nero : R.string.bianco).toUpperCase()), Constants.GOLDENROD);
            else updateStatusText(getStr(R.string.stallo), Color.LTGRAY);
            finishGame();
        } else updateStatusText(getStr(R.string.turno, getStr(w ? R.string.bianco : R.string.nero), inC ? getStr(R.string.scacco) : ""), inC ? Color.RED : Color.WHITE);
    }

    private void startTimer() {
        stopTimer();
        countDownTimer = new CountDownTimer(30000L, 100) {
            @Override public void onTick(long ms) { remainingTime.setValue(ms); }
            @Override public void onFinish() { remainingTime.setValue(0L); updateStatusText(getStr(R.string.tempo_scaduto), Color.RED); triggerEvent(new GameEvent(GameEvent.Type.FAIL_RESET)); }
        }.start();
    }

    private void stopTimer() { if (countDownTimer != null) countDownTimer.cancel(); }

    private void saveQuizProgress() {
        long time = System.currentTimeMillis() - startTimeMillis;
        LevelProgress p = new LevelProgress(quizLevel.getId(), getCurrentUserId(), true, quizErrorCount, time, System.currentTimeMillis());
        repository.saveProgress(p);
        showToast(getStr(R.string.progresso_salvato));
    }

    // Metodo aggiornato: restituisce l'ID impostato dall'Activity senza usare Firebase
    private String getCurrentUserId() {
        return currentUserId;
    }

    public void showQuizHint() {
        if (quizLevel == null || currentQuizMoveIndex >= quizLevel.getSolutionMoves().size()) return;
        MoveRequest m = quizLevel.getSolutionMoves().get(currentQuizMoveIndex);
        selectedPosition.setValue(m.startRow * 8 + m.startCol);
        isHintActive = true; showToast(getStr(R.string.suggerimento_attivato)); refreshUI();
    }

    public void refreshUI() {
        Integer sel = selectedPosition.getValue();
        if (sel != null) {
            if (isHintActive && "QUIZ".equals(mode)) {
                List<Integer> d = new ArrayList<>();
                MoveRequest m = quizLevel.getSolutionMoves().get(currentQuizMoveIndex);
                d.add(m.endRow * 8 + m.endCol); hints.setValue(d);
            } else hints.setValue(board.getLegalMovesForPiece(sel/8, sel%8));
        } else hints.setValue(new ArrayList<>());
    }

    private void updateStatusText(String t, int c) { status.setValue(new StatusInfo(t, c)); }
    private void showToast(String m) { triggerEvent(new GameEvent(GameEvent.Type.TOAST, m)); }
    private void updateCapturedSignal() { triggerEvent(new GameEvent(GameEvent.Type.UPDATE_CAPTURED)); }
    private void finishGame() { triggerEvent(new GameEvent(GameEvent.Type.FINISH)); }
    private void triggerEvent(GameEvent e) { gameEvent.setValue(e); }

    public Board getBoard() { return board; }
    public LiveData<Integer> getSelectedPosition() { return selectedPosition; }
    public LiveData<List<Integer>> getHints() { return hints; }
    public LiveData<StatusInfo> getStatus() { return status; }
    public LiveData<Boolean> getIsThinking() { return isThinking; }
    public LiveData<GameEvent> getGameEvent() { return gameEvent; }
    public LiveData<Long> getRemainingTime() { return remainingTime; }
    public LiveData<Boolean> getIsTimerVisible() { return isTimerVisible; }
    public String getStr(int id) { return ChessUtil.getLocalizedContext(getApplication()).getString(id); }
    public String getStr(int id, Object... args) { return ChessUtil.getLocalizedContext(getApplication()).getString(id, args); }
    public void onPause() { stopTimer(); }

    public static class StatusInfo { public final String text; public final int color; public StatusInfo(String t, int c) { this.text = t; this.color = c; } }
    public static class GameEvent {
        public enum Type { TOAST, FINISH, UPDATE_CAPTURED, ANIMATE_MOVE, SHOW_PROMOTION, FAIL_RESET }
        public final Type type; public final String data; public int startPos, endPos; public Piece piece; public Runnable onComplete; public boolean isWhite; public PromotionListener listener;
        public GameEvent(Type t) { this.type = t; this.data = null; }
        public GameEvent(Type t, String d) { this.type = t; this.data = d; }
        public GameEvent(Type t, int s, int e, Piece p, Runnable c) { this.type = t; this.startPos = s; this.endPos = e; this.piece = p; this.onComplete = c; this.data = null; }
        public GameEvent(Type t, boolean w, PromotionListener l) { this.type = t; this.isWhite = w; this.listener = l; this.data = null; }
    }
}