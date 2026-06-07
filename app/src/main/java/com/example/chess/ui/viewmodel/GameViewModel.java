package com.example.chess.ui.viewmodel;

import android.app.Application;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.chess.R;
import com.example.chess.model.Board;
import com.example.chess.model.MoveRequest;
import com.example.chess.model.Pawn;
import com.example.chess.model.Piece;
import com.example.chess.repository.ChessRepository;
import com.example.chess.model.QuizLevel;
import com.example.chess.service.StockfishService;
import com.example.chess.util.ChessUtil;
import com.example.chess.util.Constants;
import com.example.chess.util.MoveCalculator;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GameViewModel extends AndroidViewModel {

    private Board board;
    private final ChessRepository repository;
    private String mode;

    // LIVE DATA COMUNI
    private final MutableLiveData<Integer> selectedPosition = new MutableLiveData<>(null);
    private final MutableLiveData<List<Integer>> hints = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<StatusInfo> status = new MutableLiveData<>();
    private final MutableLiveData<GameEvent> gameEvent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isThinking = new MutableLiveData<>(false);

    // STATO SPECIFICO QUIZ
    private QuizLevel quizLevel;
    private int currentMoveIndex = 0;
    private int errorCount = 0;
    private final MutableLiveData<Boolean> isHintActive = new MutableLiveData<>(false);

    public interface PromotionListener {
        void onPieceSelected(char type);
    }

    public GameViewModel(@NonNull Application application) {
        super(application);
        repository = new ChessRepository();
    }

    public void initGame(String mode, QuizLevel level) {
        if (board == null) {
            this.mode = mode;
            this.quizLevel = level;
            board = repository.getNewGame();
            
            switch (mode) {
                case "QUIZ":
                    if (quizLevel != null) {
                        board.setupCustomBoard(quizLevel.getInitialBoardSetup(), quizLevel.isWhiteTurnToStart());
                        updateStatusText(getStr(R.string.turno, getStr(R.string.bianco), ""), Color.WHITE);
                        startTimerView();
                    }
                    break;
                case "BOT":
                    updateBotStatus();
                    break;
                default: // LOCAL PVP
                    updateLocalStatus();
                    break;
            }
            refreshUI();
        }
    }

    public void handleSquareClick(int position) {
        if (Boolean.TRUE.equals(isThinking.getValue())) return;

        switch (mode) {
            case "QUIZ": handleQuizClick(position); break;
            case "BOT": handleBotClick(position); break;
            default: handleLocalClick(position); break;
        }
    }

    // --- LOGICA QUIZ ---
    private void handleQuizClick(int position) {
        int row = position / 8, col = position % 8;
        Integer sel = selectedPosition.getValue();
        if (sel == null) {
            Piece p = board.getPiece(row, col);
            if (p != null && p.isWhite() == board.isWhiteTurn()) {
                selectedPosition.setValue(position); refreshUI();
            }
        } else {
            Piece clicked = board.getPiece(row, col);
            if (clicked != null && clicked.isWhite() == board.isWhiteTurn()) {
                selectedPosition.setValue(position); refreshUI(); return;
            }
            MoveRequest expected = quizLevel.getSolutionMoves().get(currentMoveIndex);
            if (sel / 8 == expected.startRow && sel % 8 == expected.startCol && row == expected.endRow && col == expected.endCol) {
                stopTimerView(); 
                isHintActive.setValue(false);
                Piece moving = board.getPiece(sel / 8, sel % 8);
                board.movePiece(sel / 8, sel % 8, row, col);
                selectedPosition.setValue(null);
                animatePieceMove(sel, position, moving, () -> {
                    currentMoveIndex++; updateCapturedPieces(); refreshUI();
                    if (currentMoveIndex < quizLevel.getSolutionMoves().size()) {
                        updateStatusText(getStr(R.string.risposta_computer), Color.WHITE);
                        playQuizComputerMove();
                    } else {
                        updateStatusText(getStr(R.string.livello_superato), Color.GREEN);
                        onLevelCompleted(quizLevel.getTitle()); finishGame();
                    }
                });
            } else {
                errorCount++; int rem = quizLevel.getMaxAttempts() - errorCount;
                isHintActive.setValue(false);
                if (rem <= 0) { updateStatusText(getStr(R.string.hai_perso), Color.RED); finishGame(); }
                else updateStatusText(getStr(R.string.mossa_errata, rem), Color.RED);
                selectedPosition.setValue(null); refreshUI();
            }
        }
    }

    private void playQuizComputerMove() {
        isThinking.setValue(true);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            MoveRequest move = quizLevel.getSolutionMoves().get(currentMoveIndex);
            Piece p = board.getPiece(move.startRow, move.startCol);
            board.movePiece(move.startRow, move.startCol, move.endRow, move.endCol);
            animatePieceMove(move.startRow * 8 + move.startCol, move.endRow * 8 + move.endCol, p, () -> {
                currentMoveIndex++; updateCapturedPieces(); updateStatusText(getStr(R.string.tocca_a_te), Color.WHITE);
                isThinking.setValue(false); refreshUI(); startTimerView();
            });
        }, 1000);
    }

    public void showQuizHint() {
        if (quizLevel == null || currentMoveIndex >= quizLevel.getSolutionMoves().size()) return;
        MoveRequest m = quizLevel.getSolutionMoves().get(currentMoveIndex);
        selectedPosition.setValue(m.startRow * 8 + m.startCol);
        isHintActive.setValue(true);
        showToast(getStr(R.string.suggerimento_attivato));
        refreshUI();
    }

    // --- LOGICA BOT ---
    private void handleBotClick(int position) {
        if (!board.isWhiteTurn()) return;
        int row = MoveCalculator.toRow(position), col = MoveCalculator.toCol(position);
        Integer sel = selectedPosition.getValue();
        if (sel == null) {
            Piece p = board.getPiece(row, col);
            if (p != null && p.isWhite()) { selectedPosition.setValue(position); refreshUI(); }
        } else {
            Piece moving = board.getPiece(sel / 8, sel % 8), target = board.getPiece(row, col);
            if (target != null && target.isWhite()) { selectedPosition.setValue(position); refreshUI(); return; }
            if (board.movePiece(sel / 8, sel % 8, row, col)) {
                selectedPosition.setValue(null);
                animatePieceMove(sel, position, moving, () -> {
                    updateCapturedPieces(); refreshUI();
                    if (!checkEndGameBot()) playBotMove();
                });
            } else { selectedPosition.setValue(null); refreshUI(); }
        }
    }

    private void playBotMove() {
        isThinking.setValue(true); updateStatusText(getStr(R.string.bot_pensa), Color.LTGRAY);
        Retrofit r = new Retrofit.Builder().baseUrl("https://stockfish.online/").addConverterFactory(GsonConverterFactory.create()).build();
        r.create(StockfishService.class).getBestMove(board.toFen(), 5).enqueue(new Callback<StockfishService.StockfishResponse>() {
            @Override public void onResponse(Call<StockfishService.StockfishResponse> call, Response<StockfishService.StockfishResponse> res) {
                if (res.isSuccessful() && res.body() != null) {
                    String move = res.body().bestmove.replace("bestmove ", "").trim();
                    if (move.length() >= 4) {
                        int s = ChessUtil.algebraicToIndex(move.substring(0, 2)), e = ChessUtil.algebraicToIndex(move.substring(2, 4));
                        Piece p = board.getPiece(s/8, s%8);
                        board.movePiece(s/8, s%8, e/8, e%8, move.length() == 5 ? move.charAt(4) : 'q');
                        animatePieceMove(s, e, p, () -> { isThinking.setValue(false); updateCapturedPieces(); updateBotStatus(); refreshUI(); checkEndGameBot(); });
                        return;
                    }
                }
                isThinking.setValue(false); updateBotStatus();
            }
            @Override public void onFailure(Call<StockfishService.StockfishResponse> call, Throwable t) { isThinking.setValue(false); updateBotStatus(); }
        });
    }

    private void updateBotStatus() {
        boolean white = board.isWhiteTurn(), inCheck = board.isKingInCheck(white);
        if (white) updateStatusText(getStr(R.string.turno_bot, getStr(R.string.bianco), inCheck ? getStr(R.string.scacco) : ""), inCheck ? Color.RED : Color.WHITE);
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

    // --- LOGICA LOCAL PVP ---
    private void handleLocalClick(int position) {
        int row = MoveCalculator.toRow(position), col = MoveCalculator.toCol(position);
        Integer sel = selectedPosition.getValue();
        if (sel == null) {
            Piece p = board.getPiece(row, col);
            if (p != null && p.isWhite() == board.isWhiteTurn()) { selectedPosition.setValue(position); refreshUI(); }
        } else {
            int sR = MoveCalculator.toRow(sel), sC = MoveCalculator.toCol(sel);
            Piece moving = board.getPiece(sR, sC), target = board.getPiece(row, col);
            if (target != null && target.isWhite() == board.isWhiteTurn()) { selectedPosition.setValue(position); refreshUI(); return; }
            if (moving instanceof Pawn && (row == 0 || row == 7) && board.isValidMove(sR, sC, row, col)) {
                showPromotionDialog(moving.isWhite(), type -> {
                    Piece promo = board.getPiece(sR, sC); board.movePiece(sR, sC, row, col, type);
                    stopTimerView(); animatePieceMove(sel, position, promo, () -> {
                        updateCapturedPieces(); updateLocalStatus(); refreshUI();
                        if (board.hasAnyLegalMoves(board.isWhiteTurn())) startTimerView();
                    });
                });
                selectedPosition.setValue(null); refreshUI(); return;
            }
            if (board.movePiece(sR, sC, row, col)) {
                stopTimerView(); selectedPosition.setValue(null);
                animatePieceMove(sel, position, moving, () -> {
                    updateCapturedPieces(); updateLocalStatus(); refreshUI();
                    if (board.hasAnyLegalMoves(board.isWhiteTurn())) startTimerView();
                });
            } else { selectedPosition.setValue(null); refreshUI(); }
        }
    }

    private void updateLocalStatus() {
        boolean white = board.isWhiteTurn(), inCheck = board.isKingInCheck(white);
        if (!board.hasAnyLegalMoves(white)) {
            stopTimerView();
            if (inCheck) updateStatusText(getStr(R.string.scacco_matto_vince, getStr(white ? R.string.nero : R.string.bianco).toUpperCase()), Constants.GOLDENROD);
            else updateStatusText(getStr(R.string.stallo), Color.LTGRAY);
            finishGame();
        } else updateStatusText(getStr(R.string.turno, getStr(white ? R.string.bianco : R.string.nero), inCheck ? getStr(R.string.scacco) : ""), inCheck ? Color.RED : Color.WHITE);
    }

    // --- CALLBACK E UTILITY ---
    public void refreshUI() {
        Integer sel = selectedPosition.getValue();
        if (sel != null) {
            if ("QUIZ".equals(mode) && Boolean.TRUE.equals(isHintActive.getValue())) {
                List<Integer> h = new ArrayList<>();
                if (quizLevel != null && currentMoveIndex < quizLevel.getSolutionMoves().size())
                    h.add(quizLevel.getSolutionMoves().get(currentMoveIndex).endRow * 8 + quizLevel.getSolutionMoves().get(currentMoveIndex).endCol);
                hints.setValue(h);
            } else hints.setValue(board.getLegalMovesForPiece(sel / 8, sel % 8));
        } else hints.setValue(new ArrayList<>());
    }

    public void showToast(String m) { gameEvent.setValue(new GameEvent(GameEvent.Type.TOAST, m)); }
    public void updateStatusText(String t, int c) { status.setValue(new StatusInfo(t, c)); }
    public void finishGame() { gameEvent.setValue(new GameEvent(GameEvent.Type.FINISH)); }
    public void startTimerView() { gameEvent.setValue(new GameEvent(GameEvent.Type.START_TIMER)); }
    public void stopTimerView() { gameEvent.setValue(new GameEvent(GameEvent.Type.STOP_TIMER)); }
    public void updateCapturedPieces() { gameEvent.setValue(new GameEvent(GameEvent.Type.UPDATE_CAPTURED)); }
    public void onLevelCompleted(String id) { gameEvent.setValue(new GameEvent(GameEvent.Type.LEVEL_COMPLETED, id)); }
    public void animatePieceMove(int s, int e, Piece p, Runnable c) { gameEvent.setValue(new GameEvent(GameEvent.Type.ANIMATE_MOVE, s, e, p, c)); }
    public void showPromotionDialog(boolean w, PromotionListener l) { gameEvent.setValue(new GameEvent(GameEvent.Type.SHOW_PROMOTION, w, l)); }

    public Board getBoard() { return board; }
    public LiveData<Integer> getSelectedPosition() { return selectedPosition; }
    public LiveData<List<Integer>> getHints() { return hints; }
    public LiveData<StatusInfo> getStatus() { return status; }
    public LiveData<GameEvent> getGameEvent() { return gameEvent; }
    public LiveData<Boolean> getIsThinking() { return isThinking; }
    public String getStr(int id) { return getApplication().getString(id); }
    public String getStr(int id, Object... args) { return getApplication().getString(id, args); }
    public void handleTimeOut() {
        if ("QUIZ".equals(mode)) {
            updateStatusText(getStr(R.string.tempo_scaduto), Color.RED);
            new Handler(Looper.getMainLooper()).postDelayed(this::finishGame, 2000);
        } else if (!"BOT".equals(mode)) updateStatusText(getStr(R.string.tempo_scaduto_msg), Color.RED);
    }
    public void onPause() { if (board != null) board.toString(); }

    public static class StatusInfo {
        public final String text; public final int color;
        public StatusInfo(String t, int c) { this.text = t; this.color = c; }
    }

    public static class GameEvent {
        public enum Type { REFRESH_UI, TOAST, FINISH, START_TIMER, STOP_TIMER, UPDATE_CAPTURED, LEVEL_COMPLETED, ANIMATE_MOVE, SHOW_PROMOTION }
        public final Type type; public final String data;
        public int startPos, endPos; public Piece piece; public Runnable onComplete;
        public boolean isWhite; public PromotionListener listener;

        public GameEvent(Type t) { this(t, null); }
        public GameEvent(Type t, String d) { this.type = t; this.data = d; }
        public GameEvent(Type t, int s, int e, Piece p, Runnable c) { this.type = t; this.startPos = s; this.endPos = e; this.piece = p; this.onComplete = c; this.data = null; }
        public GameEvent(Type t, boolean w, PromotionListener l) { this.type = t; this.isWhite = w; this.listener = l; this.data = null; }
    }
}
