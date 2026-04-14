package com.example.chess.ui.viewmodel;

import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

public class GameViewModel extends ViewModel {

    // Esempio: salviamo lo stato della scacchiera (es. un array di 64 posizioni)
    // E la lista delle mosse fatte
    private int[] boardState;
    private List<String> movesMade = new ArrayList<>();
    private boolean isWhiteTurn = true;

    // Quando il ViewModel viene creato per la prima volta, inizializziamo il gioco
    public GameViewModel() {
        // Qui metti la logica per preparare la scacchiera iniziale (i 32 pezzi al loro posto)
        boardState = new int[64];
        // initBoard(boardState);
    }

    // Metodi per leggere i dati
    public int[] getBoardState() { return boardState; }
    public List<String> getMovesMade() { return movesMade; }
    public boolean isWhiteTurn() { return isWhiteTurn; }

    // Metodo per fare una mossa e salvarla nel ViewModel
    public void makeMove(int fromIndex, int toIndex, String moveNotation) {
        // 1. Aggiorna l'array boardState spostando il pezzo
        // 2. Aggiungi la mossa alla cronologia
        movesMade.add(moveNotation);
        // 3. Cambia turno
        isWhiteTurn = !isWhiteTurn;
    }
}