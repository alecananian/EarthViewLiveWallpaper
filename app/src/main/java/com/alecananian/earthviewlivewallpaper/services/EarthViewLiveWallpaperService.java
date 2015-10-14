package com.alecananian.earthviewlivewallpaper.services;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import com.alecananian.earthviewlivewallpaper.R;
import com.alecananian.earthviewlivewallpaper.asynctasks.SetWallpaperTask;
import com.alecananian.earthviewlivewallpaper.interfaces.SetWallpaperTaskInterface;
import com.alecananian.earthviewlivewallpaper.models.Wallpaper;

import java.util.Random;

public class EarthViewLiveWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new EarthViewLiveWallpaperEngine();
    }

    private class EarthViewLiveWallpaperEngine extends Engine implements SetWallpaperTaskInterface, SharedPreferences.OnSharedPreferenceChangeListener {
        private final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        private final Handler handler = new Handler();
        private final Runnable runnableCallback = new Runnable() {
            @Override
            public void run() {
                fetchNewWallpaper();
            }
        };

        private final Paint paint = new Paint();

        private final int[] wallpaperIds = getResources().getIntArray(R.array.wallpaper_ids);
        private Wallpaper wallpaper;
        private Bitmap fullWallpaperImage;
        private Bitmap resizedWallpaperImage;
        private long lastWallpaperTapTimestamp = 0;

        public void onSetWallpaperTaskComplete(Wallpaper newWallpaper) {
            if (newWallpaper != null) {
                wallpaper = newWallpaper;
                draw();
                startRunnable();
            }
        }

        private void fetchNewWallpaper() {
            boolean shouldFetchWallpaper = true;
            if (wallpaper != null && preferences.getBoolean(getString(R.string.settings_key_wifi_only), false)) {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                shouldFetchWallpaper = (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected());
            }

            if (shouldFetchWallpaper) {
                int wallpaperId = wallpaperIds[new Random().nextInt(wallpaperIds.length)];
                new SetWallpaperTask(this).execute(Integer.toString(wallpaperId));
            } else {
                startRunnable();
            }
        }

        private void draw() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    if (wallpaper != null) {
                        byte[] wallpaperImageAsBytes = wallpaper.getImageAsBytes();
                        fullWallpaperImage = BitmapFactory.decodeByteArray(wallpaperImageAsBytes, 0, wallpaperImageAsBytes.length);

                        int canvasWidth = canvas.getWidth();
                        int canvasHeight = canvas.getHeight();
                        double imageAspectRatio = fullWallpaperImage.getWidth() / (double)fullWallpaperImage.getHeight();
                        int newWidth;
                        int newHeight;
                        if (canvasWidth < canvasHeight) {
                            newWidth = (int) Math.round(canvasHeight * imageAspectRatio);
                            newHeight = canvasHeight;
                        } else {
                            newWidth = canvasWidth;
                            newHeight = (int) Math.round(canvasWidth / imageAspectRatio);
                        }

                        resizedWallpaperImage = Bitmap.createScaledBitmap(fullWallpaperImage, newWidth, newHeight, false);

                        fullWallpaperImage.recycle();
                        fullWallpaperImage = null;

                        int xPos = (canvas.getWidth() - resizedWallpaperImage.getWidth()) / 2;
                        canvas.drawBitmap(resizedWallpaperImage, xPos, 0, paint);

                        resizedWallpaperImage.recycle();
                        resizedWallpaperImage = null;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }

        private void startRunnable() {
            cancelRunnable();
            handler.postDelayed(runnableCallback, Integer.parseInt(preferences.getString(getString(R.string.settings_key_runnable_interval), getString(R.string.settings_default_runnable_interval))));
        }

        private void cancelRunnable() {
            handler.removeCallbacks(runnableCallback);
        }

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            paint.setColor(Color.BLACK);
            preferences.registerOnSharedPreferenceChangeListener(this);
            fetchNewWallpaper();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();

            preferences.unregisterOnSharedPreferenceChangeListener(this);
            cancelRunnable();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            draw();
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            cancelRunnable();
        }

        @Override
        public Bundle onCommand(String action, int x, int y, int z, Bundle extras, boolean resultRequested) {
            if (action.equals(WallpaperManager.COMMAND_TAP)) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                boolean doubleTapEnabled = preferences.getBoolean(getString(R.string.settings_key_launch_maps), false);
                if (doubleTapEnabled) {
                    if (lastWallpaperTapTimestamp != 0 && System.currentTimeMillis() - lastWallpaperTapTimestamp <= 500) {
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(wallpaper.getMapIntentUri()));
                        mapIntent.setPackage("com.google.android.apps.maps");
                        mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(mapIntent);
                    }

                    lastWallpaperTapTimestamp = System.currentTimeMillis();
                }
            }

            return super.onCommand(action, x, y, z, extras, resultRequested);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.settings_key_runnable_interval))) {
                cancelRunnable();
                fetchNewWallpaper();
            }
        }
    }

}
