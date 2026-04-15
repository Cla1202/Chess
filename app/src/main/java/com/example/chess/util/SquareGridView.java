package com.example.chess.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class SquareGridView extends GridView {

    public SquareGridView(Context context) {
        super(context);
    }

    public SquareGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // Questo è il cuore della magia: intercettiamo il momento in cui Android calcola le misure
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Leggiamo quanto spazio Android ci sta dando in larghezza e altezza
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        // Troviamo il lato più corto disponibile
        int size = Math.min(width, height);

        // Creiamo una nuova regola rigida: "Devi essere grande esattamente 'size' pixel sia in larghezza che in altezza"
        int squareMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);

        // Passiamo questa nuova regola quadrata ad Android
        super.onMeasure(squareMeasureSpec, squareMeasureSpec);
    }
}