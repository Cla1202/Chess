package com.example.chess.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
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
        ImageView square;

        if (convertView == null) {
            square = new ImageView(context);
            square.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            square.setPadding(8, 8, 8, 8);
        } else {
            square = (ImageView) convertView;
        }

        int availableWidth = parent.getWidth();
        if (availableWidth == 0) {
            availableWidth = Math.min(
                    context.getResources().getDisplayMetrics().widthPixels,
                    context.getResources().getDisplayMetrics().heightPixels
            );
        }
        int size = availableWidth / 8;
        square.setLayoutParams(new AbsListView.LayoutParams(size, size));

        int row = position / 8;
        int col = position % 8;

        if ((row + col) % 2 == 0) {
            square.setBackgroundColor(Color.parseColor("#E0E0E0"));
        } else {
            square.setBackgroundColor(Color.parseColor("#8B0000"));
        }

        if (hintPositions != null && hintPositions.contains(position)) {
            square.setBackgroundColor(Color.parseColor("#80FFEB3B"));
        }
        else if (selectedPosition != null && selectedPosition == position) {
            square.setBackgroundColor(Color.parseColor("#F5F682"));
        }

        Piece piece = board.getPiece(row, col);
        if (piece != null) {
            square.setImageResource(getResIdForPiece(piece));
        } else {
            square.setImageResource(0);
        }

        return square;
    }

    private int getResIdForPiece(Piece piece) {
        String name = (piece.isWhite() ? "w_" : "b_") + piece.getClass().getSimpleName().toLowerCase();
        return context.getResources().getIdentifier(name, "drawable", context.getPackageName());
    }
}