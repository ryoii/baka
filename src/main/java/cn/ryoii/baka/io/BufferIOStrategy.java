package cn.ryoii.baka.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class BufferIOStrategy implements IOStrategy {

    @Override
    public ByteBuffer read(FileChannel channel, long position, int size) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(size);
        channel.read(buffer, position);
        buffer.flip();
        return buffer;
    }

    @Override
    public int read(FileChannel channel, long position, ByteBuffer buffer) throws IOException {
        buffer.mark();
        int read = channel.read(buffer, position);
        buffer.reset();
        return read;
    }

    @Override
    public int read(FileChannel channel, ByteBuffer buffer) throws IOException {
        buffer.mark();
        int read = channel.read(buffer);
        buffer.reset();
        return read;
    }
}
