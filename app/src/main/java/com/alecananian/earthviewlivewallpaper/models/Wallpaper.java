package com.alecananian.earthviewlivewallpaper.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

public class Wallpaper implements Parcelable {

    private String dataUri;
    private LatLng geoPoint;
    private int zoomLevel;

    private String locality;
    private String adminArea1;
    private String adminArea2;
    private String country;

    public String attribution;

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        p.writeParcelable(geoPoint, flags);
        p.writeInt(zoomLevel);
        p.writeString(locality);
        p.writeString(adminArea1);
        p.writeString(adminArea2);
        p.writeString(country);
        p.writeString(attribution);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Wallpaper createFromParcel(Parcel in) {
            return new Wallpaper(in);
        }

        public Wallpaper[] newArray(int size) {
            return new Wallpaper[size];
        }
    };

    public Wallpaper(Parcel p) {
        geoPoint = p.readParcelable(LatLng.class.getClassLoader());
        zoomLevel = p.readInt();
        locality = p.readString();
        adminArea1 = p.readString();
        adminArea2 = p.readString();
        country = p.readString();
        attribution = p.readString();
    }

    public Wallpaper(JSONObject wallpaperData) {
        try {
            dataUri = wallpaperData.getString("dataUri");
            geoPoint = new LatLng(wallpaperData.getDouble("lat"), wallpaperData.getDouble("lng"));
            zoomLevel = wallpaperData.getInt("zoom");

            JSONObject geocode = wallpaperData.getJSONObject("geocode");
            if (!geocode.isNull("locality")) {
                locality = geocode.getString("locality");
            }

            if (!geocode.isNull("administrative_area_level_1")) {
                adminArea1 = geocode.getString("administrative_area_level_1");
            }

            if (!geocode.isNull("administrative_area_level_2")) {
                adminArea2 = geocode.getString("administrative_area_level_2");
            }

            if (!geocode.isNull("country")) {
                country = geocode.getString("country");
            }

            attribution = wallpaperData.getString("attribution");
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
                "?z=" + Integer.toString(zoomLevel);
    }

    public String getLocationString() {
        String location = "";

        if (locality != null) {
            location += locality;
        }

        if (adminArea1 != null) {
            location += (location.length() > 0 ? " " : "") + adminArea1;
        }

        if (adminArea2 != null) {
            location += (location.length() > 0 ? ", " : "") + adminArea2;
        }

        if (country != null) {
            location += ", " + country;
        }

        return location;
    }
}
