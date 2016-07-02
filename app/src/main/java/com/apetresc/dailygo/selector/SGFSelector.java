package com.apetresc.dailygo.selector;

import com.apetresc.sgfstream.IncorrectFormatException;
import com.apetresc.sgfstream.SGF;

import java.io.IOException;

public interface SGFSelector {
    SGF getNextSGF() throws IOException, IncorrectFormatException;
}
