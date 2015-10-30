package com.alecananian.earthviewlivewallpaper.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.alecananian.earthviewlivewallpaper.events.NetworkChangeEvent;

import de.greenrobot.event.EventBus;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private NetworkInfo.State networkState;
    private int connectivityType;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            if (networkState != networkInfo.getState() || connectivityType != networkInfo.getType()) {
                networkState = networkInfo.getState();
                connectivityType = networkInfo.getType();
                EventBus.getDefault().post(new NetworkChangeEvent(networkState, connectivityType));
            }
        }
    }
}