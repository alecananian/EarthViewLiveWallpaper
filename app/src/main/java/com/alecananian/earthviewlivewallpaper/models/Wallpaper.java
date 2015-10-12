package com.alecananian.earthviewlivewallpaper.models;

import android.util.Base64;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class Wallpaper {

    private String dataUri;
    private LatLng geoPoint;
    private int zoomLevel;

    public Wallpaper(JSONObject wallpaperData) {
        try {
            dataUri = wallpaperData.getString("dataUri");
            geoPoint = new LatLng(wallpaperData.getDouble("lat"), wallpaperData.getDouble("lng"));
            zoomLevel = wallpaperData.getInt("zoom");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public byte[] getImageAsBytes() {
        return Base64.decode(dataUri.replace("data:image/jpeg;base64,", "").getBytes(), 0);
    }

    public String getMapIntentUri() {
        return "geo:" +
                Double.toString(geoPoint.latitude) + "," + Double.toString(geoPoint.longitude) +
                "?z=" + Integer.toString(zoomLevel) +
                "&t=h";
    }

}
