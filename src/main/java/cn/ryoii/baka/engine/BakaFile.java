package cn.ryoii.baka.engine;

import cn.ryoii.baka.exception.ReadEntryFailedException;
import cn.ryoii.baka.exception.ReadHintFailedException;
import cn.ryoii.baka.io.BufferIOStrategy;
import cn.ryoii.baka.io.IOStrategy;
import cn.ryoii.baka.value.ByteString;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

class BakaFile implements AutoCloseable {

    final static String DATA_EXT = ".db";
    final static String HINT_EXT = ".hint";

    private final IOStrategy ioStrategy = new BufferIOStrategy();
    private final ReentrantLock lock = new ReentrantLock();

    private final FileChannel readCh;
    private FileChannel writeCh;
    private FileChannel hintCh;

    private AtomicLong writePosition;
    private ByteBuffer wHeader = ByteBuffer.allocateDirect(BakaEntry.DATA_HEADER_SIZE);

    final File file;
    final int fileId;

    BakaFile(int fileId, File file, FileChannel writeCh, FileChannel readCh) throws IOException {
        this.fileId = fileId;
        this.file = file;
        this.writeCh = writeCh;
        this.readCh = readCh;

        if (writeCh != null) {
            writePosition = new AtomicLong(writeCh.size());
        } else {
            writePosition = null;
        }
    }

    void lock() {
        lock.lock();
    }

    void unlock() {
        lock.unlock();
    }

    void unlockEntirely() {
        while (lock.isLocked()) {
            lock.unlock();
        }
    }

    long size() {
        return writePosition.get();
    }

    void archive() {
        try {
            wHeader = null;
            writePosition = null;
            if (writeCh != null) {
                writeCh.close();
                writeCh = null;
            }
            if (hintCh != null) {
                hintCh.close();
                hintCh = null;
            }
            if (!file.setReadOnly()) {
                System.out.println("set file readonly failed");
            }
        } catch (Exception ignore) {
        }
    }

    ByteString readValue(long position, int valueSize) throws IOException {
        ByteBuffer value = ioStrategy.read(readCh, position, valueSize);
        return ByteString.copyFrom(value);
    }

    BakaHint writeEntry(ByteString key, ByteString value) throws IOException {
        BakaEntry entry = new BakaEntry(key, value);
        long valuePos = writeEntry(entry);
        return new BakaHint(entry, fileId, valuePos);
    }

    long writeEntry(BakaEntry entry) throws IOException {
        wHeader.clear();
        wHeader.putInt(entry.crc);
        wHeader.putInt(entry.timestamp);
        wHeader.putInt(entry.keySize);
        wHeader.putInt(entry.valueSize);
        wHeader.flip();

        long entrySize = BakaEntry.DATA_HEADER_SIZE + entry.keySize + entry.valueSize;
        long valuePos = writePosition.addAndGet(entrySize) - entry.valueSize;

        writeCh.write(new ByteBuffer[]{wHeader, entry.key.asReadOnlyByteBuffer(), entry.value.asReadOnlyByteBuffer()});

        return valuePos;
    }

    void writeHint(BakaHint hint) throws IOException {
        if (hintCh == null) openHintChannel();

        ByteBuffer buffer = ByteBuffer.allocate(BakaHint.HINT_HEADER_SIZE + hint.keySize);
        buffer.putInt(hint.keySize);
        buffer.putInt(hint.valueSize);
        buffer.putLong(hint.valuePosition);
        buffer.putInt(hint.timestamp);
        buffer.put(hint.key.asReadOnlyByteBuffer());
        buffer.flip();

        hintCh.write(buffer);
    }

    FileChannel openHintChannel() throws FileNotFoundException {
        if (hintCh != null && hintCh.isOpen()) {
            return hintCh;
        }

        hintCh = new RandomAccessFile(hintFile(), "rw").getChannel();
        return hintCh;
    }

    private File hintFile() {
        return new File(this.file.getParent(), this.file.getName().replace(DATA_EXT, HINT_EXT));
    }

    void lookupHint(Consumer<BakaHint> consumer) {
        File hintFile = hintFile();
        if (!hintFile.exists() || !hintFile.isFile()) {
            lookupFromData(consumer);
            return;
        }

        try (FileChannel hintCh = openHintChannel()) {
            ByteBuffer header = ByteBuffer.allocate(BakaHint.HINT_HEADER_SIZE);

            while (true) {
                header.clear();
                BakaHint hint = readHintFromChannel(hintCh, header);
                if (hint == null) {
                    break;
                }

                consumer.accept(hint);
            }

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private BakaHint readHintFromChannel(FileChannel channel, ByteBuffer header) throws IOException {
        long position = channel.position();
        int size = ioStrategy.read(channel, header);

        // complete
        if (size == -1) {
            return null;
        }

        if (header.remaining() != BakaHint.HINT_HEADER_SIZE) {
            throw new ReadHintFailedException(fileId, position);
        }

        int keySize = header.getInt();
        int valueSize = header.getInt();
        long valuePosition = header.getLong();
        int timestamp = header.getInt();

        ByteBuffer key = ByteBuffer.allocate(keySize);
        ioStrategy.read(channel, key);
        if (key.remaining() != keySize) {
            throw new ReadHintFailedException(fileId, position);
        }

        return new BakaHint(fileId, keySize, valueSize, valuePosition, timestamp, ByteString.copyFrom(key));
    }

    private void lookupFromData(Consumer<BakaHint> consumer) {
        try {
            long position = 0;

            while (true) {
                BakaEntry entry = readEntry(position);
                if (entry == null) {
                    break;
                }

                long valuePos = position + BakaEntry.DATA_HEADER_SIZE + entry.keySize;
                BakaHint hint = new BakaHint(fileId, entry.keySize, entry.valueSize, valuePos, entry.timestamp, entry.key);
                position += (BakaEntry.DATA_HEADER_SIZE + entry.keySize + entry.valueSize);

                consumer.accept(hint);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private BakaEntry readEntry(long position) throws IOException {
        ByteBuffer header = ByteBuffer.allocate(BakaEntry.DATA_HEADER_SIZE);

        int read = ioStrategy.read(readCh, position, header);
        if (read == -1) {
            return null;
        }

        boolean readFailed = header.remaining() != BakaEntry.DATA_HEADER_SIZE;

        if (readFailed) {
            throw new ReadEntryFailedException(fileId, position);
        }

        int crc = header.getInt();
        int timestamp = header.getInt();
        int keySize = header.getInt();
        int valueSize = header.getInt();

        ByteBuffer kv = ioStrategy.read(readCh, position + BakaEntry.DATA_HEADER_SIZE, keySize);
        if (kv.remaining() != keySize) {
            throw new ReadEntryFailedException(fileId, position);
        }

        ByteString key = ByteString.copyFrom(kv, keySize);

        return new BakaEntry(crc, timestamp, keySize, valueSize, key, null);
    }

    @Override
    public void close() {
        try {
            readCh.close();
        } catch (Exception ignore) {
        }

        try {
            if (writeCh != null) writeCh.close();
        } catch (Exception ignore) {
        }
    }

    @SuppressWarnings("unused")
    public void delete() {
        close();
        boolean d = file.delete();
        File hintFile = hintFile();
        if (hintFile.exists()) {
            boolean dd= hintFile.delete();
        }
    }
}
