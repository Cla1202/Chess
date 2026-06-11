package com.example.chess.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.GridView;

import androidx.core.content.ContextCompat;

import com.example.chess.R;
import com.example.chess.model.Board;
import com.example.chess.model.Piece;
import com.example.chess.util.Constants;
import java.util.ArrayList;
import java.util.List;

public class ChessAdapter extends BaseAdapter {
    private Context context;
    private Board board;
    private Integer selected = null;
    private List<Integer> hints = new ArrayList<>();
    private SharedPreferences prefs;
    private boolean flipped = false;

    public ChessAdapter(Context context, Board board) {
        this.context = context; this.board = board;
        this.prefs = context.getSharedPreferences(Constants.SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);
    }
    @Override public int getCount() { return 64; }
    @Override public Object getItem(int p) { return null; }
    @Override public long getItemId(int p) { return p; }
    public void setHints(List<Integer> h) { this.hints = h; notifyDataSetChanged(); }
    public void setSelectedPosition(Integer p) { this.selected = p; }

    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
        notifyDataSetChanged();
    }

    /** Converte posizione griglia <-> posizione logica (rotazione 180°, è il suo stesso inverso) */
    public int mapPosition(int p) {
        return flipped ? 63 - p : p;
    }

    @Override
    public View getView(int p, View convertView, ViewGroup parent) {
        ViewHolder h;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_chess_square, parent, false);
            int s = parent.getWidth() / 8;
            if (s == 0) s = context.getResources().getDisplayMetrics().widthPixels / 8;
            convertView.setLayoutParams(new GridView.LayoutParams(s, s));
            h = new ViewHolder(); h.container = convertView.findViewById(R.id.squareContainer);
            h.piece = convertView.findViewById(R.id.pieceImage); h.dot = convertView.findViewById(R.id.hintDot);
            h.rank = convertView.findViewById(R.id.rankText); h.file = convertView.findViewById(R.id.fileText);
            convertView.setTag(h);
        } else h = (ViewHolder) convertView.getTag();

        int row = p / 8, col = p % 8;
        int logical = mapPosition(p);
        int lRow = logical / 8, lCol = logical % 8;
        
        // Uso le costanti per il tema
        String theme = prefs.getString(Constants.KEY_BOARD_THEME, Constants.THEME_GREEN);
        String l = Constants.THEME_CLASSIC_LIGHT, d = Constants.THEME_CLASSIC_DARK;
        
        if (Constants.THEME_WOOD.equals(theme)) {
            l = Constants.THEME_WOOD_LIGHT; d = Constants.THEME_WOOD_DARK;
        } else if (Constants.THEME_OCEAN.equals(theme)) {
            l = Constants.THEME_OCEAN_LIGHT; d = Constants.THEME_OCEAN_DARK;
        } else if (Constants.THEME_GREY.equals(theme)) {
            l = Constants.THEME_GREY_LIGHT; d = Constants.THEME_GREY_DARK;
        }

        boolean isL = (row + col) % 2 == 0;
        h.container.setBackgroundColor(Color.parseColor(isL ? l : d));
        h.dot.setVisibility(hints.contains(logical) ? View.VISIBLE : View.GONE);
        if (selected != null && selected == logical) h.container.setBackgroundColor(ContextCompat.getColor(context, R.color.steel_blue));

        h.rank.setVisibility(col == 0 ? View.VISIBLE : View.GONE);
        if (col == 0) { h.rank.setText("" + (8 - lRow)); h.rank.setTextColor(Color.parseColor(isL ? d : l)); }
        h.file.setVisibility(row == 7 ? View.VISIBLE : View.GONE);
        if (row == 7) { h.file.setText("" + (char)('a' + lCol)); h.file.setTextColor(Color.parseColor(isL ? d : l)); }

        Piece piece = board.getPiece(lRow, lCol);
        if (piece != null) h.piece.setImageResource(getResId(piece)); else h.piece.setImageResource(0);
        return convertView;
    }

    private int getResId(Piece piece) {
        // Uso le costanti per lo stile dei pezzi
        String style = prefs.getString(Constants.KEY_PIECE_STYLE, Constants.STYLE_NEO);
        String pref = "";
        
        if (Constants.STYLE_NEO.equals(style)) {
            pref = "neo_";
        } else if (Constants.STYLE_MODERN.equals(style)) {
            pref = "mod_";
        } else if (Constants.STYLE_ALPHA.equals(style)) {
            pref = "alpha_";
        }
        
        String name = pref + (piece.isWhite() ? Constants.PREFIX_WHITE : Constants.PREFIX_BLACK) + piece.getClass().getSimpleName().toLowerCase();
        int id = context.getResources().getIdentifier(name, Constants.DEF_TYPE_DRAWABLE, context.getPackageName());
        
        if (id == 0) {
            id = context.getResources().getIdentifier((piece.isWhite() ? Constants.PREFIX_WHITE : Constants.PREFIX_BLACK) + piece.getClass().getSimpleName().toLowerCase(), Constants.DEF_TYPE_DRAWABLE, context.getPackageName());
        }
        return id;
    }

    private static class ViewHolder { FrameLayout container; ImageView piece; View dot; TextView rank, file; }
}