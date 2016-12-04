package com.apetresc.dailygo.fetcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkStateChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null) {
            if (info.isConnected()) {
                //start service
                //Intent intent = new Intent(context, MyService.class);
                //context.startService(intent);
            }
            else {
                //stop service
                //Intent intent = new Intent(context, MyService.class);
                //context.stopService(intent);
            }
        }
    }
}
