package cn.ryoii.baka.engine;

import cn.ryoii.baka.config.BakaConfig;
import cn.ryoii.baka.value.ByteString;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Set;

import static cn.ryoii.baka.engine.BakaFile.DATA_EXT;

class ReadWriteEngine extends AbstractEngine {

    private final long maxSize;

    ReadWriteEngine(File path, BakaConfig config) {
        super(path, config);
        maxSize = config.getMaxFileSize();
    }

    protected void putInternal(ByteString key, ByteString value) throws IOException {
        try {
            activeFile.lock();
            BakaHint hint = activeFile.writeEntry(key, value);
            indexManager.put(hint);
            long size = activeFile.size();
            if (size > maxSize) {
                switchAndMerge();
            }
        } finally {
            activeFile.unlock();
        }
    }

    protected BakaFile loadDataFile(File file) throws IOException {
        int fileId = Integer.parseInt(file.getName().substring(0, file.getName().indexOf(".")));
        FileChannel writeCh = null;
        if (file.canWrite()) writeCh = new FileOutputStream(file, true).getChannel();
        FileChannel readCh = new RandomAccessFile(file, "r").getChannel();
        BakaFile bakaFile = new BakaFile(fileId, file, writeCh, readCh);
        dataFiles.put(bakaFile.fileId, bakaFile);
        return bakaFile;
    }

    protected BakaFile createDataFile(int fileId) throws IOException {
        File file = new File(path, String.format("%09d%s", fileId, DATA_EXT));
        boolean ok = file.createNewFile();
        if (!ok) {
            throw new RuntimeException("cannot create data file: " + file.getAbsolutePath());
        }
        FileChannel writeCh = new FileOutputStream(file, true).getChannel();
        FileChannel readCh = new RandomAccessFile(file, "r").getChannel();
        return new BakaFile(fileId, file, writeCh, readCh);
    }

    protected void switchAndMerge() throws IOException {
        Set<Integer> originKeys = new HashSet<>(dataFiles.keySet());
        switchActive();
        merge();
        for (Integer originKey : originKeys) {
            BakaFile bakaFile = dataFiles.remove(originKey);
            bakaFile.delete();
        }
    }

    protected void switchActive() throws IOException {
        BakaFile oldActiveFile = activeFile;
        try {
            oldActiveFile.lock();
            BakaFile newActiveFile = createDataFile(nextFileId());
            dataFiles.put(newActiveFile.fileId, newActiveFile);

            newActiveFile.lock();
            activeFile = newActiveFile;

        } finally {
            oldActiveFile.archive();
            oldActiveFile.unlockEntirely();
        }
    }

    /**
     * rewrite all data into active file
     */
    protected void merge() throws IOException {
        BakaEntry entry = new BakaEntry(0, 0, 0, 0);
        for (BakaHint hint : indexManager) {
            ByteString value = dataFiles.get(hint.fileId).readValue(hint.valuePosition, hint.valueSize);

            entry.timestamp = hint.timestamp;
            entry.keySize = hint.keySize;
            entry.valueSize = hint.valueSize;
            entry.key = hint.key;
            entry.value = value;
            entry.crc = entry.calculateCrc32();
            long valuePos = activeFile.writeEntry(entry);

            hint.fileId = activeFile.fileId;
            hint.valuePosition = valuePos;
            activeFile.writeHint(hint);

            // switch whiling merging
            if (activeFile.size() > maxSize) {
                switchActive();
            }
        }

    }
}
