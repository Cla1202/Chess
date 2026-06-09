package com.example.chess.ui.home.viewmodel;

import android.app.Application;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.chess.R;
import com.example.chess.database.LevelProgress;
import com.example.chess.model.Board;
import com.example.chess.model.MoveRequest;
import com.example.chess.model.Piece;
import com.example.chess.model.Pawn;
import com.example.chess.model.QuizLevel;
import com.example.chess.repository.ChessRepository;
import com.example.chess.util.ChessUtil;
import com.example.chess.util.MoveCalculator;
import com.example.chess.util.ServiceLocator; // IMPORT AGIUNTO

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameViewModel extends AndroidViewModel {

    public enum StatusColorType { NORMAL, WARNING, DANGER, SUCCESS }

    private Board board;
    private final ChessRepository repository; // La repository ora viene iniettata
    private String mode;
    private QuizLevel quizLevel;
    private long startTimeMillis;

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

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Runnable pendingAnimationAction = null;

    public interface PromotionListener { void onPieceSelected(char type); }

    public GameViewModel(@NonNull Application application) {
        super(application);
        // UPDATED: Use ServiceLocator to get the ChessRepository instance
        this.repository = ServiceLocator.getInstance().getChessRepository(application);
    }

    // ... (Il resto dei metodi rimane invariato)

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
        if (!timerSettingEnabled) {
            isTimerVisible.setValue(false);
            return;
        }

        executor.execute(() -> {
            LevelProgress p = repository.getProgress(quizLevel.getId(), getCurrentUserId());
            boolean isComp = (p != null && p.isCompleted);

            if (!isComp) {
                isTimerVisible.postValue(true);
                new Handler(Looper.getMainLooper()).post(this::startTimer);
            } else {
                isTimerVisible.postValue(false);
            }
        });
    }

    // ... (Tutti gli altri metodi, inclusi processMove, playBotMove, ecc., rimangono identici)
    // Assicurati che i metodi di callback e logica interna non siano stati alterati.

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
        selectedPosition.setValue(null);
        hints.setValue(new ArrayList<>());

        pendingAnimationAction = () -> {
            updateCapturedSignal();
            if ("BOT".equals(mode)) { if (!checkEndGameBot()) playBotMove(); }
            else { updateLocalStatus(); }
        };

        triggerEvent(new GameEvent(GameEvent.Type.ANIMATE_MOVE, s, e, p));
    }

    private void handleQuizMove(int sR, int sC, int eR, int eC, Piece moving) {
        MoveRequest expected = quizLevel.getSolutionMoves().get(currentQuizMoveIndex);
        if (sR == expected.startRow && sC == expected.startCol && eR == expected.endRow && eC == expected.endCol) {
            stopTimer();
            board.movePiece(sR, sC, eR, eC);
            int startIdx = sR * 8 + sC, endIdx = eR * 8 + eC;
            selectedPosition.setValue(null);
            hints.setValue(new ArrayList<>());

            pendingAnimationAction = () -> {
                currentQuizMoveIndex++;
                updateCapturedSignal();
                if (currentQuizMoveIndex < quizLevel.getSolutionMoves().size()) {
                    updateStatusText(getStr(R.string.risposta_computer), StatusColorType.NORMAL);
                    playQuizComputerMove();
                } else {
                    updateStatusText(getStr(R.string.livello_superato), StatusColorType.SUCCESS);
                    isThinking.setValue(true);
                    saveQuizProgress();
                    new Handler(Looper.getMainLooper()).postDelayed(this::finishGame, 1500);
                }
            };

            triggerEvent(new GameEvent(GameEvent.Type.ANIMATE_MOVE, startIdx, endIdx, moving));
        } else {
            quizErrorCount++;
            int rem = quizLevel.getMaxAttempts() - quizErrorCount;
            if (rem <= 0) {
                stopTimer();
                updateStatusText(getStr(R.string.hai_perso), StatusColorType.DANGER);
                finishGame();
            } else {
                updateStatusText(getStr(R.string.mossa_errata, rem), StatusColorType.DANGER);
                isHintActive = false;
                selectedPosition.setValue(null);
                hints.setValue(new ArrayList<>());
            }
        }
    }

    private void playQuizComputerMove() {
        isThinking.setValue(true);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            MoveRequest move = quizLevel.getSolutionMoves().get(currentQuizMoveIndex);
            Piece p = board.getPiece(move.startRow, move.startCol);
            board.movePiece(move.startRow, move.startCol, move.endRow, move.endCol);

            pendingAnimationAction = () -> {
                currentQuizMoveIndex++;
                updateCapturedSignal();
                if (currentQuizMoveIndex < quizLevel.getSolutionMoves().size()) {
                    updateStatusText(getStr(R.string.tocca_a_te), StatusColorType.NORMAL);
                    isThinking.setValue(false);
                    startTimer();
                } else {
                    updateStatusText(getStr(R.string.livello_superato), StatusColorType.SUCCESS);
                    isThinking.setValue(true);
                    saveQuizProgress();
                    new Handler(Looper.getMainLooper()).postDelayed(this::finishGame, 1500);
                }
            };

            triggerEvent(new GameEvent(GameEvent.Type.ANIMATE_MOVE, move.startRow * 8 + move.startCol, move.endRow * 8 + move.endCol, p));
        }, 1000);
    }

    private void playBotMove() {
        isThinking.setValue(true);
        updateBotStatus();

        repository.getBestMove(board.toFen(), 5, new ChessRepository.BotMoveCallback() {
            @Override
            public void onSuccess(String bestmove) {
                String m = bestmove.replace("bestmove ", "").trim();
                if (m.length() >= 4) {
                    int s = ChessUtil.algebraicToIndex(m.substring(0, 2)), e = ChessUtil.algebraicToIndex(m.substring(2, 4));
                    Piece p = board.getPiece(s/8, s%8);
                    board.movePiece(s/8, s%8, e/8, e%8, m.length() == 5 ? m.charAt(4) : 'q');

                    pendingAnimationAction = () -> {
                        isThinking.setValue(false);
                        updateCapturedSignal();
                        updateBotStatus();
                        checkEndGameBot();
                    };

                    triggerEvent(new GameEvent(GameEvent.Type.ANIMATE_MOVE, s, e, p));
                } else {
                    isThinking.setValue(false);
                    updateBotStatus();
                }
            }

            @Override
            public void onError(Throwable t) {
                isThinking.setValue(false);
                Log.e("BOT_ERROR", "Errore del bot: " + t.getMessage());
                updateBotStatus();
            }
        });
    }

    private void updateBotStatus() {
        boolean w = board.isWhiteTurn(), inC = board.isKingInCheck(w);
        if (w) {
            updateStatusText(getStr(R.string.turno_bot, getStr(R.string.bianco), inC ? getStr(R.string.scacco) : ""), inC ? StatusColorType.DANGER : StatusColorType.NORMAL);
        } else {
            updateStatusText(getStr(R.string.bot_pensa), StatusColorType.WARNING);
        }
    }

    private boolean checkEndGameBot() {
        boolean t = board.isWhiteTurn();
        if (!board.hasAnyLegalMoves(t)) {
            if (board.isKingInCheck(t)) {
                updateStatusText(getStr(R.string.scacco_matto_vince, getStr(t ? R.string.nero : R.string.bianco).toUpperCase()), StatusColorType.WARNING);
            } else {
                updateStatusText(getStr(R.string.stallo), StatusColorType.WARNING);
            }
            finishGame();
            return true;
        }
        return false;
    }

    private void updateLocalStatus() {
        boolean w = board.isWhiteTurn(), inC = board.isKingInCheck(w);
        if (!board.hasAnyLegalMoves(w)) {
            if (inC) {
                updateStatusText(getStr(R.string.scacco_matto_vince, getStr(w ? R.string.nero : R.string.bianco).toUpperCase()), StatusColorType.WARNING);
            } else {
                updateStatusText(getStr(R.string.stallo), StatusColorType.WARNING);
            }
            finishGame();
        } else {
            updateStatusText(getStr(R.string.turno, getStr(w ? R.string.bianco : R.string.nero), inC ? getStr(R.string.scacco) : ""), inC ? StatusColorType.DANGER : StatusColorType.NORMAL);
        }
    }

    private void startTimer() {
        stopTimer();
        countDownTimer = new CountDownTimer(30000L, 100) {
            @Override public void onTick(long ms) { remainingTime.setValue(ms); }
            @Override public void onFinish() {
                remainingTime.setValue(0L);
                updateStatusText(getStr(R.string.tempo_scaduto), StatusColorType.DANGER);
                triggerEvent(new GameEvent(GameEvent.Type.FAIL_RESET));
            }
        }.start();
    }

    private void stopTimer() { if (countDownTimer != null) countDownTimer.cancel(); }

    private void saveQuizProgress() {
        executor.execute(() -> {
            long time = System.currentTimeMillis() - startTimeMillis;
            LevelProgress p = new LevelProgress(quizLevel.getId(), getCurrentUserId(), true, quizErrorCount, time, System.currentTimeMillis());
            repository.saveProgress(p);
            new Handler(Looper.getMainLooper()).post(() -> showToast(getStr(R.string.progresso_salvato)));
        });
    }

    private String getCurrentUserId() {
        return currentUserId;
    }

    public void showQuizHint() {
        if (quizLevel == null || currentQuizMoveIndex >= quizLevel.getSolutionMoves().size()) return;
        MoveRequest m = quizLevel.getSolutionMoves().get(currentQuizMoveIndex);
        selectedPosition.setValue(m.startRow * 8 + m.startCol);
        isHintActive = true;
        showToast(getStr(R.string.suggerimento_attivato));
        refreshUI();
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

    public void onAnimationFinished() {
        if (pendingAnimationAction != null) {
            pendingAnimationAction.run();
            pendingAnimationAction = null;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopTimer();
        if (executor != null) {
            executor.shutdown();
        }
    }

    private void updateStatusText(String t, StatusColorType type) { status.setValue(new StatusInfo(t, type)); }
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

    public static class StatusInfo {
        public final String text;
        public final StatusColorType colorType;

        public StatusInfo(String t, StatusColorType c) {
            this.text = t;
            this.colorType = c;
        }
    }

    public static class GameEvent {
        public enum Type { TOAST, FINISH, UPDATE_CAPTURED, ANIMATE_MOVE, SHOW_PROMOTION, FAIL_RESET }
        public final Type type;
        public final String data;
        public int startPos, endPos;
        public Piece piece;
        public boolean isWhite;
        public PromotionListener listener;

        public GameEvent(Type t) { this.type = t; this.data = null; }
        public GameEvent(Type t, String d) { this.type = t; this.data = d; }

        public GameEvent(Type t, int s, int e, Piece p) {
            this.type = t; this.startPos = s; this.endPos = e; this.piece = p; this.data = null;
        }

        public GameEvent(Type t, boolean w, PromotionListener l) {
            this.type = t; this.isWhite = w; this.listener = l; this.data = null;
        }
    }
}