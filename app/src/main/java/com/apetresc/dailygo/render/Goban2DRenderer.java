package com.apetresc.dailygo.render;

import android.graphics.Paint;
import android.graphics.Rect;

public abstract class Goban2DRenderer extends GobanRenderer {
    protected int minDimension;
    protected int gobanWidth;
    protected int gobanXMargin;
    protected int gobanXPadding;
    protected int gobanHeight;
    protected int gobanYMargin;
    protected int gobanYPadding;

    protected Paint paint = new Paint();


    public Goban2DRenderer(int width, int height) {
        super(width, height);
        recomputeLayout();
    }

    @Override
    public void setSurfaceDimensions(int surfaceWidth, int surfaceHeight) {
        super.setSurfaceDimensions(surfaceWidth, surfaceHeight);
        recomputeLayout();
    }

    protected void recomputeLayout() {
        minDimension = Math.min(this.surfaceHeight, this.surfaceWidth);
        gobanWidth = minDimension;
        gobanXMargin = (this.surfaceWidth - gobanWidth) / 2;
        gobanXPadding = gobanWidth / 20;
        gobanHeight = minDimension;
        gobanYMargin = (this.surfaceHeight - gobanHeight) / 2;
        gobanYPadding = gobanHeight / 20;
    }

    protected static int findLargestTextSizeWithinBounds(String s, float maxHeight, float maxWidth) {
        Paint paint = new Paint();
        int size = 1;
        Rect bounds = new Rect();
        do {
            size++;
            paint.setTextSize(size);
            paint.getTextBounds(s, 0, s.length(), bounds);
        } while (bounds.width() <= maxWidth && bounds.height() <= maxHeight);

        return size - 1;
    }

}
