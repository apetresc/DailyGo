package com.apetresc.dailygo.render;

import android.graphics.Canvas;

import com.apetresc.sgfstream.BoardPosition;

public abstract class GobanRenderer {
    protected static final int[][] STAR_POINTS = new int[][] {
            {4, 4}, {4, 10}, {4, 16},
            {10, 4}, {10, 10}, {10, 16},
            {16, 4}, {16, 10}, {16, 16}
    };

    protected int surfaceWidth;
    protected int surfaceHeight;

    public GobanRenderer(int surfaceWidth, int surfaceHeight) {
        this.surfaceWidth = surfaceWidth;
        this.surfaceHeight = surfaceHeight;
    }

    private GobanRenderer() {
        // Not accessible
    }

    public void setSurfaceDimensions(int surfaceWidth, int surfaceHeight) {
        this.surfaceWidth = surfaceWidth;
        this.surfaceHeight = surfaceHeight;
    }

    public abstract void drawBoard(Canvas c);
    public abstract void drawStones(Canvas c, BoardPosition boardPosition);

    public void draw(Canvas c, BoardPosition boardPosition) {
        drawBoard(c);
        drawStones(c, boardPosition);
    }
}
