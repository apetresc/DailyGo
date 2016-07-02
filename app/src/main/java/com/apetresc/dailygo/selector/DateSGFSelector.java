package com.apetresc.dailygo.selector;

import android.util.Log;

import com.apetresc.sgfstream.SGF;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;

public class DateSGFSelector implements SGFSelector {
    private final File directory;

    public DateSGFSelector(File directory) {
        this.directory = directory;
    }

    public SGF getNextSGF() {
        Date today = new Date();
        File todaysSgf = new File(directory, String.format("%d-%d-%d.sgf",
                today.getYear() + 1900, today.getMonth(), today.getDate()));
        if (todaysSgf.exists() && todaysSgf.canRead()) {
            SGF sgf = new SGF();
            try {
                sgf.parseSGF(new FileInputStream(todaysSgf));
            } catch (Exception e) {
                Log.v("DailyGo", "Failed to open SGF file " + todaysSgf, e);
                return null;
            }
            return sgf;
        }
        return null;
    }

}
