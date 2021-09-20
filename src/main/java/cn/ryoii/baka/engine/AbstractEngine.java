package cn.ryoii.baka.engine;

import cn.ryoii.baka.config.BakaConfig;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

abstract class AbstractEngine implements AutoCloseable {

    protected final File path;
    protected final BakaConfig config;
    protected BakaFile activeFile;
    protected final AtomicInteger fileIdGenerator = new AtomicInteger(0);
    protected final Map<Integer, BakaFile> dataFiles = new HashMap<>();
    // TODO: build index manager from factory mode
    protected final IndexManager indexManager = new HashIndexManager();

    AbstractEngine(File path, BakaConfig config) {
        this.path = path;
        this.config = config;
    }

    protected int nextFileId() {
        return fileIdGenerator.incrementAndGet();
    }

    @Override
    public void close() {
        for (Map.Entry<Integer, BakaFile> entry : dataFiles.entrySet()) {
            BakaFile bakaFile = entry.getValue();
            bakaFile.close();
        }
    }
}
