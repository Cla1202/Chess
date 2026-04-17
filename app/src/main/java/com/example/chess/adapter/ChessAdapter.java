package com.example.chess.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

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

    // --- STRUTTURA OTTIMIZZATA PER LE CASELLE ---
    private static class ViewHolder {
        FrameLayout container;
        ImageView pieceImage;
        TextView rankText; // Numeri 1-8
        TextView fileText; // Lettere a-h
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();

            // 1. Il contenitore principale della casella
            holder.container = new FrameLayout(context);

            // 2. L'immagine del pezzo
            holder.pieceImage = new ImageView(context);
            holder.pieceImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            holder.pieceImage.setPadding(8, 8, 8, 8);
            holder.container.addView(holder.pieceImage, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            // 3. Il testo del Numero (In alto a sinistra)
            holder.rankText = new TextView(context);
            FrameLayout.LayoutParams rankParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rankParams.gravity = Gravity.TOP | Gravity.START;
            holder.rankText.setLayoutParams(rankParams);
            holder.rankText.setPadding(6, 2, 0, 0); // Piccolo margine
            holder.rankText.setTextSize(12);
            holder.rankText.setTypeface(null, Typeface.BOLD);
            holder.container.addView(holder.rankText);

            // 4. Il testo della Lettera (In basso a destra)
            holder.fileText = new TextView(context);
            FrameLayout.LayoutParams fileParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            fileParams.gravity = Gravity.BOTTOM | Gravity.END;
            holder.fileText.setLayoutParams(fileParams);
            holder.fileText.setPadding(0, 0, 6, 2); // Piccolo margine
            holder.fileText.setTextSize(12);
            holder.fileText.setTypeface(null, Typeface.BOLD);
            holder.container.addView(holder.fileText);

            // Salviamo il blocco per riutilizzarlo
            holder.container.setTag(holder);
            convertView = holder.container;
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // --- DIMENSIONAMENTO DELLA CASELLA ---
        int availableWidth = parent.getWidth();
        if (availableWidth == 0) {
            availableWidth = Math.min(
                    context.getResources().getDisplayMetrics().widthPixels,
                    context.getResources().getDisplayMetrics().heightPixels
            );
        }
        int size = availableWidth / 8;
        holder.container.setLayoutParams(new AbsListView.LayoutParams(size, size));

        // --- CALCOLO RIGHE E COLONNE ---
        int row = position / 8;
        int col = position % 8;

        // --- COLORI DELLA SCACCHIERA ---
        boolean isLightSquare = (row + col) % 2 == 0;
        if (isLightSquare) {
            holder.container.setBackgroundColor(Color.parseColor("#E0E0E0")); // Chiaro
        } else {
            holder.container.setBackgroundColor(Color.parseColor("#8B0000")); // Scuro (Rosso scacchi)
        }

        // Il trucco da maestri: il testo ha il colore OPPOSTO a quello della casella
        int textColor = isLightSquare ? Color.parseColor("#8B0000") : Color.parseColor("#E0E0E0");

        // --- IMPOSTAZIONE NUMERI (1-8) SOLO SULLA PRIMA COLONNA ---
        if (col == 0) {
            holder.rankText.setVisibility(View.VISIBLE);
            holder.rankText.setText(String.valueOf(8 - row));
            holder.rankText.setTextColor(textColor);
        } else {
            holder.rankText.setVisibility(View.GONE);
        }

        // --- IMPOSTAZIONE LETTERE (a-h) SOLO SULL'ULTIMA RIGA ---
        if (row == 7) {
            holder.fileText.setVisibility(View.VISIBLE);
            char fileLetter = (char) ('a' + col);
            holder.fileText.setText(String.valueOf(fileLetter));
            holder.fileText.setTextColor(textColor);
        } else {
            holder.fileText.setVisibility(View.GONE);
        }

        // --- GESTIONE DEI COLORI DI SELEZIONE E AIUTO ---
        if (hintPositions != null && hintPositions.contains(position)) {
            holder.container.setBackgroundColor(Color.parseColor("#80FFEB3B"));
        } else if (selectedPosition != null && selectedPosition == position) {
            holder.container.setBackgroundColor(Color.parseColor("#F5F682"));
        }

        // --- INSERIMENTO DEL PEZZO ---
        Piece piece = board.getPiece(row, col);
        if (piece != null) {
            holder.pieceImage.setImageResource(getResIdForPiece(piece));
        } else {
            holder.pieceImage.setImageResource(0); // Casella vuota
        }

        return convertView;
    }

    private int getResIdForPiece(Piece piece) {
        String name = (piece.isWhite() ? "w_" : "b_") + piece.getClass().getSimpleName().toLowerCase();
        return context.getResources().getIdentifier(name, "drawable", context.getPackageName());
    }
}