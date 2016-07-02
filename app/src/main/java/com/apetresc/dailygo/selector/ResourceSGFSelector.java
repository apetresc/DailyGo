package com.apetresc.dailygo.selector;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.apetresc.sgfstream.IncorrectFormatException;
import com.apetresc.sgfstream.SGF;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

public class ResourceSGFSelector implements SGFSelector {
    private static final String SGF_ASSET_DIRECTORY = "sgf";

    private final AssetManager assetManager;
    private final List<String> resourceIds = new ArrayList<>();
    private final Iterator<String> resourceIterator;
    private final Random fixedRandomSeed = new Random(507L);

    public ResourceSGFSelector(Context context) {
        assetManager = context.getAssets();
        try {
            resourceIds.addAll(Arrays.asList(assetManager.list(SGF_ASSET_DIRECTORY)));
        } catch (IOException ioe) { }
        Collections.shuffle(resourceIds, fixedRandomSeed);
        resourceIterator = resourceIds.iterator();
    }

    public SGF getNextSGF() throws IncorrectFormatException, IOException {
        String resourcePath = null;
        try {
            resourcePath = String.format("%s/%s", SGF_ASSET_DIRECTORY, resourceIterator.next());
            SGF sgf = new SGF();
            sgf.parseSGF(assetManager.open(resourcePath));
            return sgf;
        } catch (NoSuchElementException nsee) {
            Log.e("DailyGo", "ResourceSGFSelector ran out of SGFs in resources.");
            return null;
        } catch (IOException | IncorrectFormatException e) {
            Log.e("DailyGo", "Failed to parse resource SGF at path " + resourcePath, e);
            return null;
        }
    }
}
