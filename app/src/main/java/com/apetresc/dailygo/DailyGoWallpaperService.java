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

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.apetresc.sgfstream.SGF;
import com.apetresc.sgfstream.IncorrectFormatException;

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
                sgf.parseSGF(new BufferedReader(new InputStreamReader(
                        getResources().openRawResource(R.raw.simple)
                )));
            } catch (IncorrectFormatException ife) {
                Log.e("DailyGo", "Failed to parse SGF", ife);
            }

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

        private void drawStones(Canvas canvas, int[][] boardPosition) {
            for (int i = 0; i < boardPosition.length; i++) {
                for (int j = 0; j < boardPosition[i].length; j++) {
                    if (boardPosition[i][j] != 0) {
                        paint.setColor(boardPosition[i][j] == 1 ? Color.BLACK : Color.WHITE);
                        canvas.drawCircle(
                                gobanXMargin + i * gobanXPadding,
                                gobanYMargin + j * gobanYPadding,
                                gobanYPadding,
                                paint);
                    }
                }
            }
        }

        private void draw() {
            Log.d("DailyGo", "In draw!");
            final SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;

            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    drawBoard(canvas);
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }
}
