package com.apetresc.dailygo.fetcher;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;


public class ManifestFetcherService extends IntentService implements SGFFetcherService {
    public ManifestFetcherService() { super("ManifestFetcherService"); }

    private static final String MANIFEST_URL = "http://dailygo.s3.amazonaws.com/manifest.json";

    @Override
    public void grabSGFs(File directory) throws IOException {
        JsonObjectRequest manifestRequest = new JsonObjectRequest(
                MANIFEST_URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject manifest) {
                Log.d("DailyGo", "Got an SGF manifest: " + manifest);
            }
        }, null);

        Volley.newRequestQueue(getApplication().getApplicationContext()).add(manifestRequest);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("DailyGo", "In ManifestFetcherService.onHandleIntent()");
    }
}
