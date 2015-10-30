package com.alecananian.earthviewlivewallpaper.services;

import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.alecananian.earthviewlivewallpaper.R;
import com.alecananian.earthviewlivewallpaper.activities.WallpaperPreferenceActivity;
import com.alecananian.earthviewlivewallpaper.asynctasks.SetWallpaperTask;
import com.alecananian.earthviewlivewallpaper.events.EarthViewEvent;
import com.alecananian.earthviewlivewallpaper.events.EarthViewEventType;
import com.alecananian.earthviewlivewallpaper.events.NetworkChangeEvent;
import com.alecananian.earthviewlivewallpaper.events.WallpaperEvent;
import com.alecananian.earthviewlivewallpaper.interfaces.SetWallpaperTaskInterface;
import com.alecananian.earthviewlivewallpaper.models.Wallpaper;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import de.greenrobot.event.EventBus;

public class EarthViewLiveWallpaperService extends WallpaperService {

    static final String kGalleryFolderName = "Earth View Live Wallpaper";

    @Override
    public Engine onCreateEngine() {
        return new EarthViewLiveWallpaperEngine();
    }

    public class EarthViewLiveWallpaperEngine extends Engine implements SetWallpaperTaskInterface, SharedPreferences.OnSharedPreferenceChangeListener {
        private final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        private final Handler handler = new Handler();
        private final Runnable runnableCallback = new Runnable() {
            @Override
            public void run() {
                fetchNewWallpaper();
            }
        };

        private final Paint paint = new Paint();
        private boolean isVisible;

        private boolean isFetchingWallpaper = false;
        private long lastWallpaperFetchTimestamp = 0;

        private Wallpaper wallpaper;
        private final int[] wallpaperIds = getResources().getIntArray(R.array.wallpaper_ids);
        private Bitmap fullWallpaperImage;
        private Bitmap resizedWallpaperImage;
        private long lastWallpaperTapTimestamp = 0;

        public void onSetWallpaperTaskComplete(Wallpaper newWallpaper) {
            isFetchingWallpaper = false;
            lastWallpaperFetchTimestamp = System.currentTimeMillis();

            if (newWallpaper != null) {
                wallpaper = newWallpaper;
                redrawWallpaper();
                startRunnable();
            }
        }

        public void onSetWallpaperTaskError() {
            isFetchingWallpaper = false;
        }

        private void fetchNewWallpaper() {
            if (!isFetchingWallpaper) {
                boolean shouldFetchWallpaper = true;
                if (wallpaper != null && preferences.getBoolean(getString(R.string.settings_key_wifi_only), false)) {
                    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                    shouldFetchWallpaper = (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected());
                }

                if (shouldFetchWallpaper) {
                    isFetchingWallpaper = true;
                    int wallpaperId = wallpaperIds[new Random().nextInt(wallpaperIds.length)];
                    new SetWallpaperTask(this).execute(Integer.toString(wallpaperId));
                }
            }
        }

        private void redrawWallpaper() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    if (wallpaper != null) {
                        if (fullWallpaperImage != null) {
                            fullWallpaperImage.recycle();
                            fullWallpaperImage = null;
                        }

                        byte[] wallpaperImageAsBytes = wallpaper.getImageAsBytes();
                        fullWallpaperImage = BitmapFactory.decodeByteArray(wallpaperImageAsBytes, 0, wallpaperImageAsBytes.length);

                        int canvasWidth = canvas.getWidth();
                        int canvasHeight = canvas.getHeight();
                        double imageAspectRatio = fullWallpaperImage.getWidth() / (double)fullWallpaperImage.getHeight();
                        int newWidth;
                        int newHeight;
                        if (canvasWidth < canvasHeight) {
                            newHeight = canvasHeight;
                            newWidth = (int) Math.round(newHeight * imageAspectRatio);
                        } else {
                            newWidth = canvasWidth;
                            newHeight = (int) Math.round(newWidth / imageAspectRatio);
                        }

                        resizedWallpaperImage = Bitmap.createScaledBitmap(fullWallpaperImage, newWidth, newHeight, false);

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
            fetchNewWallpaper();

            preferences.registerOnSharedPreferenceChangeListener(this);
            EventBus.getDefault().register(this);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();

            isVisible = false;
            cancelRunnable();

            preferences.unregisterOnSharedPreferenceChangeListener(this);
            EventBus.getDefault().unregister(this);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (wallpaper != null) {
                redrawWallpaper();
            } else {
                fetchNewWallpaper();
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);

            isVisible = false;
            cancelRunnable();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            isVisible = visible;
        }

        @Override
        public Bundle onCommand(String action, int x, int y, int z, Bundle extras, boolean resultRequested) {
            if (action.equals(WallpaperManager.COMMAND_TAP) && isVisible) {
                if (lastWallpaperTapTimestamp != 0 && System.currentTimeMillis() - lastWallpaperTapTimestamp <= 500) {
                    Intent settingsIntent = new Intent(getApplicationContext(), WallpaperPreferenceActivity.class);
                    settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    settingsIntent.putExtra("wallpaper", wallpaper);
                    startActivity(settingsIntent);
                }

                lastWallpaperTapTimestamp = System.currentTimeMillis();
            }

            return super.onCommand(action, x, y, z, extras, resultRequested);
        }

        /**
         * Called when <code>EventBus</code> receives a <code>WallpaperEvent</code>
         * @param event <code>WallpaperEvent</code> that was received
         */
        public void onEvent(WallpaperEvent event) {
            switch (event.eventType) {
                case LAUNCH_MAPS:
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(wallpaper.getMapIntentUri()));
                    mapIntent.setPackage("com.google.android.apps.maps");
                    mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(mapIntent);
                    break;

                case FETCH_NEW:
                    fetchNewWallpaper();
                    break;
            }
        }

        /**
         * Called when <code>EventBus</code> receives an <code>EarthViewEvent</code>
         * @param event <code>EarthViewEvent</code> that was received
         */
        public void onEvent(EarthViewEvent event) {
            if (event.eventType == EarthViewEventType.DOWNLOAD && wallpaper != null && fullWallpaperImage != null) {
                String galleryPath = File.separator + kGalleryFolderName;

                File galleryDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + galleryPath);
                boolean dirExists = galleryDirectory.exists();

                if (!dirExists) {
                    dirExists = galleryDirectory.mkdirs();
                }

                if (dirExists) {
                    try {
                        String fileName = wallpaper.getLocationString().replace(", ", "-") + ".jpg";

                        File file = new File(galleryDirectory, fileName);
                        FileOutputStream fOut = new FileOutputStream(file);

                        fullWallpaperImage.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                        fOut.flush();
                        fOut.close();

                        ContentResolver contentResolver = getContentResolver();
                        ContentValues values = new ContentValues(5);

                        values.put(MediaStore.Images.Media.TITLE, wallpaper.getLocationString());
                        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                        values.put(MediaStore.Images.Media.LATITUDE, wallpaper.getLatitude());
                        values.put(MediaStore.Images.Media.LONGITUDE, wallpaper.getLongitude());
                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                        values.put(MediaStore.Images.Media.ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                        values.put(MediaStore.Images.Media.DATA, file.getPath());
                        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                        Toast.makeText(getApplicationContext(), "Image saved to " + galleryPath + File.separator + fileName, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.i("Download Exception", e.toString());
                    }
                }
            }
        }

        /**
         * Called when <code>EventBus</code> receives a <code>NetworkChangeEvent</code>
         * @param event <code>NetworkChangeEvent</code> that was received
         */
        public void onEvent(NetworkChangeEvent event) {
            int runnableInterval = Integer.parseInt(preferences.getString(getString(R.string.settings_key_runnable_interval), getString(R.string.settings_default_runnable_interval)));
            if (event.networkState == NetworkInfo.State.CONNECTED && System.currentTimeMillis() - lastWallpaperFetchTimestamp > runnableInterval) {
                fetchNewWallpaper();
            }
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
