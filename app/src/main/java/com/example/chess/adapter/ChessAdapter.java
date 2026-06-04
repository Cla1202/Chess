package com.example.chess.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.chess.util.Constants;

public class ChessAdapter extends BaseAdapter {

    private Context context;
    private Board board;
    private Integer selectedPosition = null;
    private List<Integer> hintPositions = new ArrayList<>();
    private SharedPreferences prefs;

    public ChessAdapter(Context context, Board board) {
        this.context = context;
        this.board = board;
        this.prefs = context.getSharedPreferences("ChessSettings", Context.MODE_PRIVATE);
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

        // --- 1. COLORI BASE E COORDINATE (Caricati dalle impostazioni) ---
        String theme = prefs.getString("board_theme", "Verde Classico");
        String colorLight = Constants.THEME_CLASSIC_LIGHT;
        String colorDark = Constants.THEME_CLASSIC_DARK;

        switch (theme) {
            case "Legno Scuro":
                colorLight = Constants.THEME_WOOD_LIGHT;
                colorDark = Constants.THEME_WOOD_DARK;
                break;
            case "Blu Oceano":
                colorLight = Constants.THEME_OCEAN_LIGHT;
                colorDark = Constants.THEME_OCEAN_DARK;
                break;
            case "Grigio Moderno":
                colorLight = Constants.THEME_GREY_LIGHT;
                colorDark = Constants.THEME_GREY_DARK;
                break;
        }

        boolean isLight = (row + col) % 2 == 0;
        int baseColor = Color.parseColor(isLight ? colorLight : colorDark);
        int contrastColor = Color.parseColor(isLight ? colorDark : colorLight);

        holder.container.setBackgroundColor(baseColor);

        // --- 2. GESTIONE SELEZIONE E PALLINI (HINTS) ---
        // Il pallino è un elemento separato, non cambia lo sfondo!
        holder.hintDot.setVisibility(hintPositions != null && hintPositions.contains(position) ? View.VISIBLE : View.GONE);

        // Solo la selezione cambia lo sfondo
        if (selectedPosition != null && selectedPosition == position) {
            holder.container.setBackgroundColor(Color.parseColor(Constants.STEEL_BLUE));
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

        // --- 4. PEZZI (Caricamento asset basato sullo stile scelto) ---
        Piece piece = board.getPiece(row, col);
        if (piece != null) {
            holder.pieceImage.setImageResource(getResIdForPiece(piece));
        } else {
            holder.pieceImage.setImageResource(0);
        }

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
        String style = prefs.getString("piece_style", "Classico");
        String stylePrefix;

        switch (style) {
            case "Neo":
                stylePrefix = "neo_";
                break;
            case "Moderno":
                stylePrefix = "mod_";
                break;
            case "Alfa":
                stylePrefix = "alpha_";
                break;
            default:
                stylePrefix = "";
                break;
        }

        String colorPrefix = piece.isWhite() ? Constants.PREFIX_WHITE : Constants.PREFIX_BLACK;
        String pieceName = piece.getClass().getSimpleName().toLowerCase();
        
        // Prova a cercare lo stile specifico
        String fullName = stylePrefix + colorPrefix + pieceName;
        int resId = context.getResources().getIdentifier(fullName, "drawable", context.getPackageName());
        
        // Fallback se l'immagine dello stile non esiste
        if (resId == 0) {
            fullName = colorPrefix + pieceName;
            resId = context.getResources().getIdentifier(fullName, "drawable", context.getPackageName());
        }

        return resId;
    }
}