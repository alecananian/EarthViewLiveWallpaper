package com.alecananian.earthviewlivewallpaper.asynctasks;

import android.os.AsyncTask;

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

            HttpsURLConnection urlConnection = (HttpsURLConnection)requestUrl.openConnection();
            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());

            String response = IOUtils.toString(inputStream, "UTF-8");
            return new JSONObject(response);
        } catch (Exception e) {
            e.printStackTrace();

            this.listener.onSetWallpaperTaskError();
        }

        return null;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        super.onPostExecute(result);

        if (result != null) {
            this.listener.onSetWallpaperTaskComplete(new Wallpaper(result));
        }
    }

}
