package com.example.chess.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.example.chess.model.Board;
import com.example.chess.model.Piece;

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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView square;
        if (convertView == null) {
            square = new ImageView(context);
            // Imposta altezza uguale alla larghezza (proporzione quadrata)
            // Usa i parametri del display per forzare una dimensione visibile
            int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            int size = screenWidth / 8;
            square.setLayoutParams(new ViewGroup.LayoutParams(size, size));
            square.setLayoutParams(new ViewGroup.LayoutParams(size, size));
            square.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            square.setPadding(8, 8, 8, 8);
        } else {
            square = (ImageView) convertView;
        }

        // Dentro getView, dopo aver impostato il colore chiaro/scuro alternato:
        if (selectedPosition != null && selectedPosition == position) {
            square.setBackgroundColor(Color.parseColor("#F5F682")); // Colore evidenziatore
        }

        // Calcola coordinate x,y dalla posizione 0-63
        int row = position / 8;
        int col = position % 8;

        // Colore della casella (alternato)
        if ((row + col) % 2 == 0) {
            square.setBackgroundColor(Color.parseColor("#EEEED2")); // Chiaro
        } else {
            square.setBackgroundColor(Color.parseColor("#769656")); // Scuro
        }

        // Disegna il pezzo se presente
        Piece piece = board.getPiece(row, col);
        if (piece != null) {
            square.setImageResource(getResIdForPiece(piece));
        } else {
            square.setImageResource(0); // Vuoto
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