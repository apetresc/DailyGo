package com.apetresc.dailygo.render;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;

import com.apetresc.sgfstream.BoardPosition;

public class UntexturedGoban2DRenderer extends Goban2DRenderer {

    public UntexturedGoban2DRenderer(int width, int height) {
        super(width, height);
    }

    @Override
    public void drawBoard(Canvas c) {
        paint.setColor(Color.rgb(200, 150, 73));
        c.drawRect(new Rect(
                        gobanXMargin,
                        gobanYMargin,
                        gobanXMargin + gobanWidth,
                        gobanYMargin + gobanHeight),
                paint);

        float[] gridPoints = new float[19 * 2 * 4];
        for (int i = 0; i < 19; i++) {
            gridPoints[i * 4] = gobanXMargin + gobanXPadding;
            gridPoints[i * 4 + 1] = gobanYMargin + (i + 1) * gobanYPadding;
            gridPoints[i * 4 + 2] = gobanXMargin + gobanWidth - gobanXPadding;
            gridPoints[i * 4 + 3] = gobanYMargin + (i + 1) * gobanYPadding;

            gridPoints[19 * 4 + i * 4] = gobanXMargin + (i + 1) * gobanXPadding;
            gridPoints[19 * 4 + i * 4 + 1] = gobanYMargin + gobanYPadding;
            gridPoints[19 * 4 + i * 4 + 2] = gobanXMargin + (i + 1) * gobanXPadding;
            gridPoints[19 * 4 + i * 4 + 3] = gobanYMargin + gobanHeight - gobanYPadding;
        }
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(3);
        c.drawLines(gridPoints, paint);

        for (int[] starPoint : STAR_POINTS) {
            c.drawCircle(gobanXMargin + starPoint[0] * gobanXPadding,
                    gobanYMargin + starPoint[1] * gobanYPadding,
                    gobanYPadding / 7,
                    paint);
        }
    }

    @Override
    public void drawStones(Canvas c, BoardPosition boardPosition) {
        for (int i = 0; i < boardPosition.getBoardSize(); i++) {
            for (int j = 0; j < boardPosition.getBoardSize(); j++) {
                if (boardPosition.getPoint(i, j) != 0) {
                    paint.setColor(Color.BLACK);
                    c.drawCircle(
                            gobanXMargin + (i + 1) * gobanXPadding,
                            gobanYMargin + (j + 1) * gobanYPadding,
                            gobanYPadding / 2,
                            paint);
                    if (boardPosition.getPoint(i, j) == 2) {
                        paint.setColor(Color.WHITE);
                        c.drawCircle(
                                gobanXMargin + (i + 1) * gobanXPadding,
                                gobanYMargin + (j + 1) * gobanYPadding,
                                gobanYPadding / 2 - 2,
                                paint);
                    }
                }
            }
        }

        int[] lastMove = boardPosition.getLastMove();
        paint.setColor(boardPosition.getPoint(lastMove[0], lastMove[1]) == 1 ? Color.WHITE : Color.BLACK);
        String lastMoveLabel = String.valueOf(boardPosition.getMoveNumber());
        int textSize = findLargestTextSizeWithinBounds(lastMoveLabel, gobanXPadding * 0.75f, gobanYPadding * 0.75f);
        Rect bounds = new Rect();
        paint.setTextSize(textSize);
        paint.getTextBounds(lastMoveLabel, 0, lastMoveLabel.length(), bounds);
        c.drawText(String.valueOf(boardPosition.getMoveNumber()),
                gobanXMargin + (lastMove[0] + 1) * gobanXPadding - bounds.width() / 2,
                gobanYMargin + (lastMove[1] + 1) * gobanYPadding + bounds.height() / 2,
                paint);
    }
}
