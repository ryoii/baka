package cn.ryoii.baka.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public interface IOStrategy {

    ByteBuffer read(FileChannel channel, long position, int size) throws IOException;

    int read(FileChannel channel, long position, ByteBuffer buffer) throws IOException;

    int read(FileChannel channel, ByteBuffer buffer) throws IOException;
}
