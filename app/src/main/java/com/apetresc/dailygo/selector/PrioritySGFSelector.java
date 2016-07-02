package com.apetresc.dailygo.selector;

import com.apetresc.sgfstream.IncorrectFormatException;
import com.apetresc.sgfstream.SGF;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PrioritySGFSelector implements SGFSelector {
    private final List<SGFSelector> selectors;

    public PrioritySGFSelector(SGFSelector... selectors) {
        this.selectors = Arrays.asList(selectors);
    }

    public SGF getNextSGF() throws IncorrectFormatException, IOException {
        for (SGFSelector selector : selectors) {
            SGF sgf = selector.getNextSGF();
            if (sgf != null) {
                return sgf;
            }
        }
        return null;
    }
}
