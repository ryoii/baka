package cn.ryoii.baka.engine;

import cn.ryoii.baka.value.ByteString;

import java.util.zip.CRC32;

class BakaEntry {

    /* ************************************************************* *
     *   crc | timestamp | key size | value size | (key) | (value)   *
     *    4  |     4     |    4     |      4     |       |           *
     * ************************************************************* */
    public static final int DATA_HEADER_SIZE = 16;

    int crc;
    int timestamp;
    int keySize;
    int valueSize;

    ByteString key;
    ByteString value;

    BakaEntry(int crc, int timestamp, int keySize, int valueSize) {
        this.crc = crc;
        this.timestamp = timestamp;
        this.keySize = keySize;
        this.valueSize = valueSize;
    }

    BakaEntry(ByteString key, ByteString value) {
        this.timestamp = currentTimeStamp();
        this.keySize = key.size();
        this.valueSize = value.size();
        this.key = key;
        this.value = value;

        this.crc = calculateCrc32();
    }

    BakaEntry(int crc, int timestamp, int keySize, int valueSize, ByteString key, ByteString value) {
        this(crc, timestamp, keySize, valueSize);
        this.key = key;
        this.value = value;
    }

    private int currentTimeStamp() {
        return (int) (System.currentTimeMillis() / 1000L);
    }

    int calculateCrc32() {
        CRC32 crc32 = new CRC32();
        crc32.update(timestamp);
        crc32.update(keySize);
        crc32.update(valueSize);
        crc32.update(key.asReadOnlyByteBuffer());
        crc32.update(value.asReadOnlyByteBuffer());
        return (int) crc32.getValue();
    }
}
