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
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.apetresc.dailygo.com.apetresc.dailygo.render.GobanRenderer;
import com.apetresc.dailygo.com.apetresc.dailygo.render.UntexturedGoban2DRenderer;
import com.apetresc.sgfstream.BoardPosition;
import com.apetresc.sgfstream.SGF;
import com.apetresc.sgfstream.IncorrectFormatException;
import com.apetresc.sgfstream.SGFIterator;
import com.apetresc.sgfstream.SGFNode;

public class DailyGoWallpaperService extends WallpaperService {

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
        private GobanRenderer gobanRenderer;
        private int totalNumberOfMoves = 0;

        private boolean visible = true;

        BoardPosition boardPosition = null;

        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

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

            // Count number of moves to know how to schedule
            SGFIterator countMovesIterator = sgf.iterator();
            SGFNode lastNode = null;
            while (countMovesIterator.hasNext()) {
                lastNode = countMovesIterator.next();
            }
            totalNumberOfMoves = lastNode.getBoardPosition().getMoveNumber();
            Log.v("DailyGo", "Newly loaded SGF has " + totalNumberOfMoves + " moves.");

            scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    handler.post(drawRunner);
                }
            }, 0, (60 * 60 * 24) / totalNumberOfMoves, TimeUnit.SECONDS);
            handler.post(drawRunner);
        }

        public void catchUp() {
            Calendar c = Calendar.getInstance();
            long now = c.getTimeInMillis();
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            long passed = (now - c.getTimeInMillis()) / 1000;

            double percentageOfDay = passed / (double) (60 * 60 * 24);
            int shouldBeAtMove = (int) Math.ceil(percentageOfDay * totalNumberOfMoves);
            while (boardPosition.getMoveNumber() < shouldBeAtMove) {
                boardPosition.applyNode(sgfIterator.next());
            }
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
            if (gobanRenderer == null) {
                gobanRenderer = new UntexturedGoban2DRenderer(width, height);
            } else {
                gobanRenderer.setSurfaceDimensions(width, height);
            }
            super.onSurfaceChanged(holder, format, width, height);
        }

        private void draw() {
            final SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;

            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    catchUp();
                    gobanRenderer.draw(canvas, boardPosition);
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }
}
