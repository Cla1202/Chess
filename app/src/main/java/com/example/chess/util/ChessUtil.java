package com.example.chess.util;

import android.content.Context;
import com.example.chess.model.Piece;

public class ChessUtil {
    public static int getResIdForPiece(Context context, Piece piece) {
        if (piece == null) return 0;

        String prefix = piece.isWhite() ? Constants.PREFIX_WHITE : Constants.PREFIX_BLACK;
        String name = prefix + piece.getClass().getSimpleName().toLowerCase();

        return context.getResources().getIdentifier(
                name,
                Constants.DEF_TYPE_DRAWABLE,
                context.getPackageName()
        );
    }
}