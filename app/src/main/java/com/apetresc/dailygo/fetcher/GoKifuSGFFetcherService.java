package com.apetresc.dailygo.fetcher;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.File;

public class GoKifuSGFFetcherService extends IntentService implements SGFFetcherService {
    public GoKifuSGFFetcherService() {
        super("GoKifuSGFFetcherService");
    }

    @Override
    public void grabSGFs(File path) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null) {
            if (info.isConnected()) {
                //start service
            }
            else {
                //stop service
            }
        }
    }
}
