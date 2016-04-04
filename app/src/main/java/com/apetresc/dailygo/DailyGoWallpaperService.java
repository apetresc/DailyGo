package com.apetresc.dailygo;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

import com.apetresc.dailygo.com.apetresc.dailygo.render.GobanRenderer;
import com.apetresc.dailygo.com.apetresc.dailygo.render.UntexturedGoban2DRenderer;
import com.apetresc.dailygo.com.apetresc.dailygo.selector.PrioritySGFSelector;
import com.apetresc.dailygo.com.apetresc.dailygo.selector.ResourceSGFSelector;
import com.apetresc.dailygo.com.apetresc.dailygo.selector.SGFSelector;
import com.apetresc.dailygo.com.apetresc.dailygo.selector.StaticSGFSelector;
import com.apetresc.sgfstream.BoardPosition;
import com.apetresc.sgfstream.SGF;
import com.apetresc.sgfstream.SGFIterator;
import com.apetresc.sgfstream.SGFNode;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

        private SGFSelector sgfSelector = new PrioritySGFSelector(
                new ResourceSGFSelector(DailyGoWallpaperService.this.getApplicationContext()),
                new StaticSGFSelector(getResources().openRawResource(R.raw.simple))
        );
        private SGFIterator sgfIterator;
        private GobanRenderer gobanRenderer;
        private Calendar lastMovePlayed;
        private ScheduledFuture moveTimer;
        private int totalNumberOfMoves = 0;

        BoardPosition boardPosition = null;

        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        @Override
        public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {

        }

        public DailyGoWallpaperEngine() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DailyGoWallpaperService.this);
            prefs.registerOnSharedPreferenceChangeListener(this);
        }

        public void initiateSGF(SGF sgf) {
            sgfIterator = sgf.iterator();
            boardPosition = new BoardPosition(19);

            // Count number of moves to know how to schedule
            SGFIterator countMovesIterator = sgf.iterator();
            SGFNode lastNode = null;
            while (countMovesIterator.hasNext()) {
                lastNode = countMovesIterator.next();
            }
            totalNumberOfMoves = lastNode.getBoardPosition().getMoveNumber();
            Log.v("DailyGo", "Newly loaded SGF has " + totalNumberOfMoves + " moves.");

            if (moveTimer != null) {
                moveTimer.cancel(false);
            }
            moveTimer = scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    handler.post(drawRunner);
                }
            }, 0, (60 * 60 * 24) / totalNumberOfMoves, TimeUnit.SECONDS);

            handler.post(drawRunner);
        }

        public void catchUp() {
            Calendar c = Calendar.getInstance();
            if (lastMovePlayed == null || lastMovePlayed.get(Calendar.DAY_OF_YEAR) != c.get(Calendar.DAY_OF_YEAR)) {
                // It's time to switch SGFs!
                try {
                    initiateSGF(sgfSelector.getNextSGF());
                } catch (Exception e) {
                    Log.e("DailyGo", "Failed to move to next SGF", e);
                    try {
                        initiateSGF(new StaticSGFSelector(getResources().openRawResource(R.raw.simple)).getNextSGF());
                    } catch (Exception e2) {
                        Log.e("DailyGo", "Failed to load backup SGF. This should never happen.", e);
                    }
                }
            }
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
                lastMovePlayed = Calendar.getInstance();
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                handler.post(drawRunner);
            } else {
                handler.removeCallbacks(drawRunner);
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
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
