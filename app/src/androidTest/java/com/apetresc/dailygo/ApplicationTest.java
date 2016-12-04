package com.apetresc.dailygo;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.apetresc.dailygo.fetcher.ManifestFetcherService;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    @Test
    public void testManifestFetcher() throws IOException {
        ManifestFetcherService manifestFetcherService = new ManifestFetcherService();
        manifestFetcherService.grabSGFs(new File("src/main/assets/wtf"));
    }
}