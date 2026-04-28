package com.example.chess.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.GridView;

import com.example.chess.model.Board;
import com.example.chess.model.Piece;

import java.util.ArrayList;
import java.util.List;

import com.example.chess.R;

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
    public int getCount() { return 64; }

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
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_chess_square, parent, false);

            // Calcolo dimensione quadrata
            int size = parent.getWidth() / 8;
            if (size == 0) size = context.getResources().getDisplayMetrics().widthPixels / 8;
            convertView.setLayoutParams(new GridView.LayoutParams(size, size));

            holder = new ViewHolder();
            holder.container = convertView.findViewById(R.id.squareContainer);
            holder.pieceImage = convertView.findViewById(R.id.pieceImage);
            holder.hintDot = convertView.findViewById(R.id.hintDot);
            holder.rankText = convertView.findViewById(R.id.rankText); // Aggiungi questi nel tuo XML
            holder.fileText = convertView.findViewById(R.id.fileText);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        int row = position / 8;
        int col = position % 8;

        // --- 1. COLORI BASE E COORDINATE ---
        boolean isLight = (row + col) % 2 == 0;
        int baseColor = Color.parseColor(isLight ? "#EEEEEE" : "#769656");
        int contrastColor = Color.parseColor(isLight ? "#769656" : "#EEEEEE");

        holder.container.setBackgroundColor(baseColor);

        // --- 2. GESTIONE SELEZIONE E PALLINI (HINTS) ---
        // Il pallino è un elemento separato, non cambia lo sfondo!
        holder.hintDot.setVisibility(hintPositions != null && hintPositions.contains(position) ? View.VISIBLE : View.GONE);

        // Solo la selezione cambia lo sfondo
        if (selectedPosition != null && selectedPosition == position) {
            holder.container.setBackgroundColor(Color.parseColor("#81B3D2"));
        }

        // --- 3. COORDINATE (Numeri e Lettere) ---
        holder.rankText.setVisibility(col == 0 ? View.VISIBLE : View.GONE);
        if (col == 0) {
            holder.rankText.setText(String.valueOf(8 - row));
            holder.rankText.setTextColor(contrastColor);
        }

        holder.fileText.setVisibility(row == 7 ? View.VISIBLE : View.GONE);
        if (row == 7) {
            holder.fileText.setText(String.valueOf((char) ('a' + col)));
            holder.fileText.setTextColor(contrastColor);
        }

        // --- 4. PEZZI ---
        Piece piece = board.getPiece(row, col);
        holder.pieceImage.setImageResource(piece != null ? getResIdForPiece(piece) : 0);

        return convertView;
    }

    private static class ViewHolder {
        FrameLayout container;
        ImageView pieceImage;
        View hintDot;
        TextView rankText; // Per i numeri 1-8
        TextView fileText; // Per le lettere a-h
    }

    private int getResIdForPiece(Piece piece) {
        String name = (piece.isWhite() ? "w_" : "b_") + piece.getClass().getSimpleName().toLowerCase();
        return context.getResources().getIdentifier(name, "drawable", context.getPackageName());
    }
}