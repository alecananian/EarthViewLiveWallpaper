package com.alecananian.earthviewlivewallpaper.asynctasks;

import android.os.AsyncTask;
import android.util.Log;

import com.alecananian.earthviewlivewallpaper.interfaces.SetWallpaperTaskInterface;
import com.alecananian.earthviewlivewallpaper.models.Wallpaper;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class SetWallpaperTask extends AsyncTask<String, Void, JSONObject> {

    private SetWallpaperTaskInterface listener;

    public SetWallpaperTask(SetWallpaperTaskInterface listener) {
        this.listener = listener;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        try {
            URL requestUrl = new URL("https://www.gstatic.com/prettyearth/" + params[0] + ".json");

            Log.i("SetWallpaperTask", "Requesting URL: " + requestUrl);
            HttpsURLConnection urlConnection = (HttpsURLConnection)requestUrl.openConnection();
            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());

            String response = IOUtils.toString(inputStream, "UTF-8");
            return new JSONObject(response);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        super.onPostExecute(result);

        if (result != null) {
            Log.i("SetWallpaperTask", "Finished with result: " + result.toString());
            this.listener.onSetWallpaperTaskComplete(new Wallpaper(result));
        } else {
            Log.i("SetWallpaperTask", "Finished with result: null");
        }
    }

}
