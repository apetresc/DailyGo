package com.apetresc.dailygo.fetcher;

import java.io.File;
import java.io.IOException;

public interface SGFFetcherService {
    public void grabSGFs(File directory) throws IOException;
}
