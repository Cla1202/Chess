package com.example.chess.ui.viewmodel;

import android.app.Application;
import android.graphics.Color;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.chess.database.LevelProgress;
import com.example.chess.model.Board;
import com.example.chess.model.Piece;
import com.example.chess.model.Pawn;
import com.example.chess.model.QuizLevel;
import com.example.chess.model.MoveRequest;
import com.example.chess.repository.ChessRepository;
import com.example.chess.service.StockfishService;
import com.example.chess.util.Constants;
import com.google.firebase.auth.FirebaseAuth;
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

    private final MutableLiveData<Integer> selectedPosition = new MutableLiveData<>(null);
    private final MutableLiveData<List<Integer>> hintPositions = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> statusText = new MutableLiveData<>("");
    private final MutableLiveData<Integer> statusColor = new MutableLiveData<>(Color.WHITE);
    private final MutableLiveData<Boolean> isBotThinking = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> gameFinished = new MutableLiveData<>(false);
    private final MutableLiveData<PromotionRequest> promotionRequest = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> updateCapturedSignal = new MutableLiveData<>(false);
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>(null);
    
    private final MutableLiveData<Long> remainingTime = new MutableLiveData<>(30000L);
    private final MutableLiveData<Boolean> timerActive = new MutableLiveData<>(false);
    private CountDownTimer countDownTimer;

    private int currentQuizMoveIndex = 0;
    private int quizErrorCount = 0;
    private boolean isHintActive = false;

    public GameViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ChessRepository(application);
    }

    public void init(String mode, QuizLevel quizLevel, boolean timerSettingEnabled) {
        this.mode = mode;
        this.quizLevel = quizLevel;
        this.startTimeMillis = System.currentTimeMillis();
        
        if (board == null) {
            board = new Board();
            if ("QUIZ".equals(mode) && quizLevel != null) {
                board.setupCustomBoard(quizLevel.getInitialBoardSetup(), quizLevel.isWhiteTurnToStart());
                checkIfAlreadyCompleted(quizLevel.getId(), timerSettingEnabled);
            }
        }
        updateStatus();
    }

    private void checkIfAlreadyCompleted(int levelId, boolean timerSettingEnabled) {
        String userId = getCurrentUserId();
        new Thread(() -> {
            LevelProgress p = repository.getSyncProgress(levelId, userId);
            boolean isComp = (p != null && p.isCompleted);
            if (!isComp && timerSettingEnabled) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(this::startTimer);
            }
        }).start();
    }

    public Board getBoard() { return board; }
    public LiveData<Integer> getSelectedPosition() { return selectedPosition; }
    public LiveData<List<Integer>> getHintPositions() { return hintPositions; }
    public LiveData<String> getStatusText() { return statusText; }
    public LiveData<Integer> getStatusColor() { return statusColor; }
    public LiveData<Boolean> getIsBotThinking() { return isBotThinking; }
    public LiveData<Boolean> isGameFinished() { return gameFinished; }
    public LiveData<PromotionRequest> getPromotionRequest() { return promotionRequest; }
    public LiveData<Boolean> getUpdateCapturedSignal() { return updateCapturedSignal; }
    public LiveData<String> getToastMessage() { return toastMessage; }
    public LiveData<Long> getRemainingTime() { return remainingTime; }
    public LiveData<Boolean> isTimerActive() { return timerActive; }

    public void handleSquareClick(int position) {
        if (Boolean.TRUE.equals(gameFinished.getValue()) || Boolean.TRUE.equals(isBotThinking.getValue())) return;
        int row = position / 8, col = position % 8;
        if (selectedPosition.getValue() == null) {
            Piece p = board.getPiece(row, col);
            if (p != null && p.isWhite() == board.isWhiteTurn()) selectPiece(position);
        } else processMove(position);
    }

    private void selectPiece(int pos) {
        selectedPosition.setValue(pos);
        if (!(isHintActive && "QUIZ".equals(mode))) hintPositions.setValue(board.getLegalMovesForPiece(pos / 8, pos % 8));
    }

    private void processMove(int pos) {
        int startPos = selectedPosition.getValue();
        Piece target = board.getPiece(pos/8, pos%8);
        if (target != null && target.isWhite() == board.isWhiteTurn()) { selectPiece(pos); return; }

        if ("QUIZ".equals(mode)) handleQuizMove(startPos/8, startPos%8, pos/8, pos%8);
        else handleStandardMove(startPos/8, startPos%8, pos/8, pos%8);
    }

    private void handleStandardMove(int sR, int sC, int eR, int eC) {
        Piece moving = board.getPiece(sR, sC);
        if (moving instanceof Pawn && (eR == 0 || eR == 7) && board.isValidMove(sR, sC, eR, eC)) {
            promotionRequest.setValue(new PromotionRequest(sR, sC, eR, eC)); return;
        }
        if (board.movePiece(sR, sC, eR, eC)) finalizeMove();
        else { selectedPosition.setValue(null); hintPositions.setValue(new ArrayList<>()); }
    }

    public void completePromotion(int sR, int sC, int eR, int eC, char type) {
        board.movePiece(sR, sC, eR, eC, type); finalizeMove(); promotionRequest.setValue(null);
    }

    private void finalizeMove() {
        selectedPosition.setValue(null); hintPositions.setValue(new ArrayList<>());
        updateCapturedSignal.setValue(true); updateStatus();
        if ("BOT".equals(mode) && !board.isWhiteTurn() && !Boolean.TRUE.equals(gameFinished.getValue())) playBotMove();
    }

    private void handleQuizMove(int sR, int sC, int eR, int eC) {
        MoveRequest expected = quizLevel.getSolutionMoves().get(currentQuizMoveIndex);
        if (sR == expected.startRow && sC == expected.startCol && eR == expected.endRow && eC == expected.endCol) {
            stopTimer(); board.movePiece(sR, sC, eR, eC); currentQuizMoveIndex++; isHintActive = false;
            updateCapturedSignal.setValue(true); selectedPosition.setValue(null); hintPositions.setValue(new ArrayList<>());
            if (currentQuizMoveIndex < quizLevel.getSolutionMoves().size()) {
                statusText.setValue("Ottimo! Risposta del computer..."); playQuizComputerMove();
            } else {
                statusText.setValue("Livello Superato!"); statusColor.setValue(Color.GREEN);
                saveQuizProgress(); gameFinished.setValue(true);
            }
        } else {
            quizErrorCount++; int rem = quizLevel.getMaxAttempts() - quizErrorCount;
            if (rem <= 0) { stopTimer(); statusText.setValue("HAI PERSO!"); statusColor.setValue(Color.RED); gameFinished.setValue(true); }
            else { statusText.setValue("Mossa errata! Vite: " + rem); statusColor.setValue(Color.RED); }
            selectedPosition.setValue(null); hintPositions.setValue(new ArrayList<>());
        }
    }

    private void playQuizComputerMove() {
        isBotThinking.setValue(true);
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            MoveRequest move = quizLevel.getSolutionMoves().get(currentQuizMoveIndex);
            board.movePiece(move.startRow, move.startCol, move.endRow, move.endCol);
            currentQuizMoveIndex++; isBotThinking.setValue(false); updateCapturedSignal.setValue(true);
            updateStatus(); if (!Boolean.TRUE.equals(gameFinished.getValue())) startTimer();
        }, 1000);
    }

    private void playBotMove() {
        isBotThinking.setValue(true); statusText.setValue("Il Bot sta pensando...");
        repository.getBestMove(board.toFen(), 5).enqueue(new Callback<StockfishService.StockfishResponse>() {
            @Override public void onResponse(Call<StockfishService.StockfishResponse> call, Response<StockfishService.StockfishResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String m = response.body().bestmove.replace("bestmove ", "").trim();
                    if (m.length() >= 4) {
                        board.movePiece(8-Character.getNumericValue(m.charAt(1)), m.charAt(0)-'a', 8-Character.getNumericValue(m.charAt(3)), m.charAt(2)-'a', m.length()==5?m.charAt(4):'q');
                        isBotThinking.setValue(false); updateCapturedSignal.setValue(true); updateStatus(); return;
                    }
                }
                isBotThinking.setValue(false); updateStatus();
            }
            @Override public void onFailure(Call<StockfishService.StockfishResponse> call, Throwable t) { isBotThinking.setValue(false); updateStatus(); }
        });
    }

    private void updateStatus() {
        boolean w = board.isWhiteTurn(), inC = board.isKingInCheck(w);
        if (!board.hasAnyLegalMoves(w)) {
            gameFinished.setValue(true);
            if (inC) { statusText.setValue("🏆 SCACCO MATTO! " + (w ? "Vince il Nero" : "Vinci Tu!")); statusColor.setValue(Constants.GOLDENROD); }
            else { statusText.setValue("🤝 STALLO"); statusColor.setValue(Color.LTGRAY); }
        } else {
            if ("BOT".equals(mode)) {
                if (w) { statusText.setValue("Tocca a te (Bianco)" + (inC ? " - SCACCO!" : "")); statusColor.setValue(inC ? Color.RED : Color.WHITE); }
                else statusText.setValue("Il Bot sta pensando...");
            } else if ("QUIZ".equals(mode)) { statusText.setValue("Tocca a te! Trova la mossa vincente."); statusColor.setValue(Color.WHITE); }
            else { statusText.setValue("Tocca al " + (w ? "Bianco" : "Nero") + (inC ? " - SCACCO!" : "")); statusColor.setValue(inC ? Color.RED : Color.WHITE); }
        }
    }

    public void startTimer() {
        stopTimer(); timerActive.setValue(true);
        countDownTimer = new CountDownTimer(30000L, 100) {
            @Override public void onTick(long ms) { remainingTime.setValue(ms); }
            @Override public void onFinish() { remainingTime.setValue(0L); statusText.setValue("TEMPO SCADUTO!"); statusColor.setValue(Color.RED); gameFinished.setValue(true); }
        }.start();
    }

    public void stopTimer() { if (countDownTimer != null) countDownTimer.cancel(); timerActive.setValue(false); }

    public void saveQuizProgress() {
        long time = System.currentTimeMillis() - startTimeMillis;
        LevelProgress p = new LevelProgress(quizLevel.getId(), getCurrentUserId(), true, quizErrorCount, time, System.currentTimeMillis());
        repository.saveProgress(p);
        toastMessage.setValue("Progresso salvato!");
    }

    private String getCurrentUserId() {
        com.google.firebase.auth.FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return (user != null) ? user.getUid() : "guest";
    }

    public void showHint() {
        if (!"QUIZ".equals(mode) || Boolean.TRUE.equals(isBotThinking.getValue()) || currentQuizMoveIndex >= quizLevel.getSolutionMoves().size()) return;
        MoveRequest m = quizLevel.getSolutionMoves().get(currentQuizMoveIndex);
        selectedPosition.setValue(m.startRow * 8 + m.startCol);
        List<Integer> d = new ArrayList<>(); d.add(m.endRow * 8 + m.endCol);
        hintPositions.setValue(d); isHintActive = true; toastMessage.setValue("Suggerimento attivato!");
    }

    public void clearToast() { toastMessage.setValue(null); }
    @Override protected void onCleared() { super.onCleared(); stopTimer(); }

    public static class PromotionRequest {
        public int sR, sC, eR, eC;
        public PromotionRequest(int sR, int sC, int eR, int eC) { this.sR = sR; this.sC = sC; this.eR = eR; this.eC = eC; }
    }
}