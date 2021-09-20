package cn.ryoii.baka.engine;

import cn.ryoii.baka.value.ByteString;

class BakaHint {

    /* ************************************************************** *
     *   key size | value size | value position | timestamp | (key)   *
     *      4     |     4      |       8        |     4     |         *
     * ************************************************************** */
    public static final int HINT_HEADER_SIZE = 20;

    int fileId;
    int keySize;
    int valueSize;
    long valuePosition;
    int timestamp;

    ByteString key;

    BakaHint(int fileId, int keySize, int valueSize, long valuePosition, int timestamp, ByteString key) {
        this.fileId = fileId;
        this.keySize = keySize;
        this.valueSize = valueSize;
        this.valuePosition = valuePosition;
        this.timestamp = timestamp;
        this.key = key;
    }

    BakaHint(BakaEntry entry, int fileId, long valuePosition) {
        this.fileId = fileId;
        this.keySize = entry.keySize;
        this.valueSize = entry.valueSize;
        this.valuePosition = valuePosition;
        this.timestamp = entry.timestamp;
        this.key = entry.key;
    }
}
