package com.apetresc.dailygo;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
            super.onSurfaceChanged(holder, format, width, height);
        }

        private void draw() {
            Log.d("DailyGo", "In draw!");
            final SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;

            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    paint.setColor(Color.WHITE);
                    paint.setTextSize(50);
                    canvas.drawText(sgf.toString(), 550, 550, paint);
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }
}
