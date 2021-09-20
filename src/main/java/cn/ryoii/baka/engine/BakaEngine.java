package cn.ryoii.baka.engine;

import cn.ryoii.baka.config.BakaConfig;
import cn.ryoii.baka.value.ByteString;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static cn.ryoii.baka.engine.BakaFile.DATA_EXT;

/**
 * Database engine implement
 */
public class BakaEngine extends ReadWriteEngine {

    public BakaEngine(File path, BakaConfig config) {
        super(path, config);
        if (!path.exists()) {
            boolean ok = path.mkdirs();
            if (!ok) {
                throw new RuntimeException("cannot create directory: " + path.getAbsolutePath());
            }
        }
    }

    /**
     * Load index from hint file or data file. Create new data file in empty directory.
     * Find active file according to file id.
     */
    public void init() throws IOException {
        File[] files = path.listFiles();
        if (files == null || Arrays.stream(files).noneMatch(it -> it.getName().endsWith(DATA_EXT))) {
            BakaFile dataFile = createDataFile(nextFileId());
            dataFiles.put(dataFile.fileId, dataFile);
            this.activeFile = dataFile;
            return;
        }

        for (File file : files) {
            if (file.isDirectory() || !file.canRead()) {
                continue;
            }

            if (file.getName().endsWith(DATA_EXT)) {
                BakaFile bakaFile = loadDataFile(file);
                bakaFile.lookupHint(indexManager::put);
            }
        }

        // find active
        int activeId = 0;
        for (Map.Entry<Integer, BakaFile> entry : dataFiles.entrySet()) {
            activeId = Math.max(activeId, entry.getKey());
        }
        activeFile = dataFiles.get(activeId);
        fileIdGenerator.set(activeId);
    }


    public void put(ByteString key, ByteString value) throws IOException {
        putInternal(key, value);
    }

    public ByteString get(ByteString key) throws IOException {
        BakaHint hint = indexManager.get(key);
        if (hint == null) {
            return null;
        }
        return dataFiles.get(hint.fileId).readValue(hint.valuePosition, hint.valueSize);
    }

}
