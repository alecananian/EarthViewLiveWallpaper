package com.alecananian.earthviewlivewallpaper.events;

import android.net.NetworkInfo;

public class NetworkChangeEvent {

    public NetworkInfo.State networkState;
    public int connectivityType;

    public NetworkChangeEvent(NetworkInfo.State networkState, int connectivityType) {
        this.networkState = networkState;
        this.connectivityType = connectivityType;
    }

}
