package com.example.chess.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView; // <-- Questo import è nuovo e fondamentale!
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.example.chess.model.Board;
import com.example.chess.model.Piece;

import java.util.ArrayList;
import java.util.List;

public class ChessAdapter extends BaseAdapter {
    private Context context;
    private Board board;
    private Integer selectedPosition = null;
    private List<Integer> hintPositions = new ArrayList<>();

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

    public void setHints(List<Integer> positions) {
        this.hintPositions = positions;
        notifyDataSetChanged();
    }

    public void setSelectedPosition(Integer position) {
        this.selectedPosition = position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView square;

        // 1. CREA LA CASELLA (O RICICLALA SE ESISTE GIÀ)
        if (convertView == null) {
            square = new ImageView(context);
            square.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            square.setPadding(8, 8, 8, 8);
        } else {
            square = (ImageView) convertView;
        }

        // --- IL TRUCCO PER NON TAGLIARE LE RIGHE ---
        // Prendiamo la larghezza esatta della scacchiera (non dello schermo!)
        int availableWidth = parent.getWidth();

        // Sicurezza d'emergenza: nei primissimi millisecondi di caricamento la larghezza potrebbe essere 0.
        // Se succede, prendiamo il lato più corto dello schermo per non far crashare l'app.
        if (availableWidth == 0) {
            availableWidth = Math.min(
                    context.getResources().getDisplayMetrics().widthPixels,
                    context.getResources().getDisplayMetrics().heightPixels
            );
        }

        // Dividiamo per 8 per avere la dimensione perfetta di ogni casellina
        int size = availableWidth / 8;

        // Forziamo la singola casella ad essere un quadrato inossidabile
        square.setLayoutParams(new AbsListView.LayoutParams(size, size));
        // -------------------------------------------

        int row = position / 8;
        int col = position % 8;

        // 2. IMPOSTA IL COLORE BASE DELLA SCACCHIERA
        if ((row + col) % 2 == 0) {
            square.setBackgroundColor(Color.parseColor("#E0E0E0")); // Chiaro
        } else {
            square.setBackgroundColor(Color.parseColor("#8B0000")); // Scuro
        }

        // 3. SOVRASCRIVI CON L'EVIDENZIAZIONE (SELEZIONE O CONSIGLIO)
        if (hintPositions != null && hintPositions.contains(position)) {
            square.setBackgroundColor(Color.parseColor("#80FFEB3B")); // Giallo consiglio
        }
        else if (selectedPosition != null && selectedPosition == position) {
            square.setBackgroundColor(Color.parseColor("#F5F682")); // Evidenziatore selezione
        }

        // 4. DISEGNA IL PEZZO
        Piece piece = board.getPiece(row, col);
        if (piece != null) {
            square.setImageResource(getResIdForPiece(piece));
        } else {
            square.setImageResource(0); // Casella vuota
        }

        return square;
    }

    private int getResIdForPiece(Piece piece) {
        String name = (piece.isWhite() ? "w_" : "b_") + piece.getClass().getSimpleName().toLowerCase();
        return context.getResources().getIdentifier(name, "drawable", context.getPackageName());
    }
}