package com.example.chess.ui.viewmodel;

import androidx.lifecycle.ViewModel;

import com.example.chess.model.Board;
import com.example.chess.repository.ChessRepository;

public class GameViewModel extends ViewModel {

    // Il ViewModel ora custodisce l'OGGETTO vero e proprio della scacchiera
    private Board board;
    private ChessRepository repository;

    public GameViewModel() {
        repository = new ChessRepository();
    }

    // Questo è il cuore della magia:
    // Quando giri lo schermo, l'Activity chiede la scacchiera.
    // Il ViewModel gliela ridà intatta. Se è la prima volta che apri l'app, ne crea una nuova.
    public Board getBoard() {
        if (board == null) {
            board = repository.getNewGame();
        }
        return board;
    }

    public ChessRepository getRepository() {
        return repository;
    }
}