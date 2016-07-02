package com.apetresc.dailygo.selector;

import com.apetresc.sgfstream.IncorrectFormatException;
import com.apetresc.sgfstream.SGF;

import java.io.IOException;
import java.io.InputStream;

public class StaticSGFSelector implements SGFSelector {
    private final SGF sgf = new SGF();
    private IOException ioe = null;
    private IncorrectFormatException ife = null;

    public StaticSGFSelector(InputStream stream) {
        try {
            sgf.parseSGF(stream);
        } catch (IOException ioe) {
            this.ioe = ioe;
        } catch (IncorrectFormatException ife) {
            this.ife = ife;
        }
    }
    public SGF getNextSGF() throws IncorrectFormatException, IOException {
        if (ioe != null) {
            throw ioe;
        } else if (ife != null) {
            throw ife;
        } else {
            return sgf;
        }
    }
}
