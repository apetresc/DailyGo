package com.apetresc.dailygo;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.apetresc.sgfstream.BoardPosition;
import com.apetresc.sgfstream.SGF;
import com.apetresc.sgfstream.IncorrectFormatException;
import com.apetresc.sgfstream.SGFIterator;

public class DailyGoWallpaperService extends WallpaperService {
    private static final int[][] STAR_POINTS = new int[][] {
        {4, 4}, {4, 10}, {4, 16},
        {10, 4}, {10, 10}, {10, 16},
        {16, 4}, {16, 10}, {16, 16}
    };
    @Override
    public Engine onCreateEngine() {
        return new DailyGoWallpaperEngine();
    }

    private class DailyGoWallpaperEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener {
        private final Handler handler = new Handler();
        private final Runnable drawRunner = new Runnable() {
            @Override
            public void run() {
                draw();
            }
        };

        private Paint paint = new Paint();
        private SGF sgf = new SGF();
        private SGFIterator sgfIterator;
        private boolean visible = true;
        private int width;
        private int height;

        private int minDimension;
        private int gobanWidth;
        private int gobanXMargin;
        private int gobanXPadding;
        private int gobanHeight;
        private int gobanYMargin;
        private int gobanYPadding;

        BoardPosition boardPosition = null;

        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        private void recomputeLayout() {
            minDimension = Math.min(this.height, this.width);
            gobanWidth = minDimension;
            gobanXMargin = (this.width - gobanWidth) / 2;
            gobanXPadding = gobanWidth / 20;
            gobanHeight = minDimension;
            gobanYMargin = (this.height - gobanHeight) / 2;
            gobanYPadding = gobanHeight / 20;
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {

        }

        public DailyGoWallpaperEngine() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DailyGoWallpaperService.this);
            prefs.registerOnSharedPreferenceChangeListener(this);

            try {
                sgf.parseSGF(getResources().openRawResource(R.raw.simple));
                sgfIterator = sgf.iterator();
                boardPosition = new BoardPosition(19);
            } catch (IncorrectFormatException ife) {
                Log.e("DailyGo", "Failed to parse SGF", ife);
            } catch (IOException ioe) {
                Log.e("DailyGo", "Failed to load SGF from stream", ioe);
            }

            scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    int currentMoveNumber = boardPosition.getMoveNumber();
                    do {
                        boardPosition.applyNode(sgfIterator.next());
                    } while (currentMoveNumber == boardPosition.getMoveNumber());
                    handler.post(drawRunner);
                }
            }, 1, 1, TimeUnit.SECONDS);
            handler.post(drawRunner);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            if (visible) {
                handler.post(drawRunner);
            } else {
                handler.removeCallbacks(drawRunner);
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            this.visible = false;
            handler.removeCallbacks(drawRunner);
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(DailyGoWallpaperService.this);
            pref.unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format,
                                     int width, int height) {
            this.width = width;
            this.height = height;
            recomputeLayout();
            super.onSurfaceChanged(holder, format, width, height);
        }

        private void drawBoard(Canvas canvas) {
            paint.setColor(Color.rgb(200, 150, 73));
            canvas.drawRect(new Rect(
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
            canvas.drawLines(gridPoints, paint);

            for (int i = 0; i < STAR_POINTS.length; i++) {
                canvas.drawCircle(gobanXMargin + STAR_POINTS[i][0] * gobanXPadding,
                        gobanYMargin + STAR_POINTS[i][1] * gobanYPadding,
                        gobanYPadding / 7,
                        paint);
            }

        }

        private void drawStones(Canvas canvas) {
            for (int i = 0; i < boardPosition.getBoardSize(); i++) {
                for (int j = 0; j < boardPosition.getBoardSize(); j++) {
                    if (boardPosition.getPoint(i, j) != 0) {
                        paint.setColor(Color.BLACK);
                        canvas.drawCircle(
                                gobanXMargin + (i + 1) * gobanXPadding,
                                gobanYMargin + (j + 1) * gobanYPadding,
                                gobanYPadding / 2,
                                paint);
                        if (boardPosition.getPoint(i, j) == 2) {
                            paint.setColor(Color.WHITE);
                            canvas.drawCircle(
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
            canvas.drawText(String.valueOf(boardPosition.getMoveNumber()),
                    gobanXMargin + (lastMove[0] + 1) * gobanXPadding - bounds.width() / 2,
                    gobanYMargin + (lastMove[1] + 1) * gobanYPadding + bounds.height() / 2,
                    paint);
        }

        private int findLargestTextSizeWithinBounds(String s, float maxHeight, float maxWidth) {
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

        private void draw() {
            Log.d("DailyGo", "Drawing move #" + boardPosition.getMoveNumber());
            final SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;

            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    drawBoard(canvas);
                    drawStones(canvas);
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }
}
