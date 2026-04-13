package com.example.chess.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.example.chess.model.Board;
import com.example.chess.model.Piece;

import java.util.ArrayList;
import java.util.List;

public class ChessAdapter extends BaseAdapter {
    private Context context;
    private Board board;

    public ChessAdapter(Context context, Board board) {
        this.context = context;
        this.board = board;
    }

    @Override
    public int getCount() { return 64; } // 8x8

    @Override
    public Object getItem(int position) { return null; }

    @Override
    public long getItemId(int position) { return position; }


    private List<Integer> hintPositions = new ArrayList<>();

    public void setHints(List<Integer> positions) {
        this.hintPositions = positions;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView square;
        if (convertView == null) {
            square = new ImageView(context);
            int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            int size = screenWidth / 8;
            square.setLayoutParams(new ViewGroup.LayoutParams(size, size));
            square.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            square.setPadding(8, 8, 8, 8);
        } else {
            square = (ImageView) convertView;
        }

        int row = position / 8;
        int col = position % 8;

        // 1. IMPOSTA IL COLORE BASE DELLA SCACCHIERA
        if ((row + col) % 2 == 0) {
            square.setBackgroundColor(Color.parseColor("#E0E0E0")); // Chiaro
        } else {
            square.setBackgroundColor(Color.parseColor("#8B0000")); // Scuro
        }

        // 2. SOVRASCRIVI CON L'EVIDENZIAZIONE (SELEZIONE O CONSIGLIO)
        // Controlliamo prima se è una cella di consiglio (HINT)
        if (hintPositions != null && hintPositions.contains(position)) {
            // Colore giallo semitrasparente per il consiglio
            square.setBackgroundColor(Color.parseColor("#80FFEB3B"));
        }
        // Altrimenti controlliamo se è la cella selezionata dall'utente
        else if (selectedPosition != null && selectedPosition == position) {
            square.setBackgroundColor(Color.parseColor("#F5F682")); // Colore evidenziatore selezione
        }

        // 3. DISEGNA IL PEZZO
        Piece piece = board.getPiece(row, col);
        if (piece != null) {
            square.setImageResource(getResIdForPiece(piece));
        } else {
            square.setImageResource(0);
        }

        return square;
    }

    private int getResIdForPiece(Piece piece) {
        // Qui dovrai mappare i tuoi drawable (es: R.drawable.w_pawn)
        // Esempio logico:
        String name = (piece.isWhite() ? "w_" : "b_") + piece.getClass().getSimpleName().toLowerCase();
        return context.getResources().getIdentifier(name, "drawable", context.getPackageName());
    }

    private Integer selectedPosition = null;

    public void setSelectedPosition(Integer position) {
        this.selectedPosition = position;
    }
}