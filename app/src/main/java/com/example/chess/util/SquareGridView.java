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

    // This is the heart of the magic: we intercept the moment when Android calculates the measurements
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Read how much space Android is allocating for width and height
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        // Find the smallest dimension available
        int size = Math.min(width, height);

        // Create a new strict rule: "You must be exactly 'size' pixels in both width and height"
        int squareMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);

        // Pass this new square rule to Android
        super.onMeasure(squareMeasureSpec, squareMeasureSpec);
    }
}