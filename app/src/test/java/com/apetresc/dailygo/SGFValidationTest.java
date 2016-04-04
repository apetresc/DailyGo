package com.apetresc.dailygo;

import com.apetresc.sgfstream.SGF;
import com.apetresc.sgfstream.SGFIterator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class SGFValidationTest {
    private static final String SGF_ASSET_DIRECTORY = "src/main/assets/sgf";

    @Parameterized.Parameters
    public static Collection<String> data() {
        List<String> paths = new ArrayList<>();
        for (String sgfFile : new File(SGF_ASSET_DIRECTORY).list()) {
            paths.add(String.format("%s/%s", SGF_ASSET_DIRECTORY, sgfFile));
        }
        return paths;
    }

    private final String path;

    public SGFValidationTest(String path) {
        this.path = path;
    }

    @Test
    public void parse_isValid() throws Exception {
        SGF sgf = new SGF();
        sgf.parseSGF(new FileInputStream(path));

        SGFIterator iterator = sgf.iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next().getBoardPosition());
        }
    }
}