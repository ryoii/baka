package cn.ryoii.baka.value;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class ByteString {

    private final byte[] bytes;
    private int hash;

    private ByteString(byte[] bytes) {
        this.bytes = bytes;
    }

    public static ByteString copyFrom(byte[] bytes) {
        return new ByteString(bytes);
    }

    public static ByteString copyFrom(String text, Charset charset) {
        return new ByteString(text.getBytes(charset));
    }

    public static ByteString copyFrom(ByteBuffer buffer) {
        return copyFrom(buffer, buffer.remaining());
    }

    public static ByteString copyFrom(ByteBuffer buffer, int size) {
        byte[] bytes = new byte[size];
        buffer.get(bytes);
        return copyFrom(bytes);
    }

    public int size() {
        return bytes.length;
    }

    public ByteBuffer asReadOnlyByteBuffer() {
        return ByteBuffer.wrap(bytes).asReadOnlyBuffer();
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            int h = bytes.length;
            for (byte b : bytes) {
                h = h * 31 + b;
            }
            if (h == 0) h = 1;
            hash = h;
        }
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof ByteString)) return false;
        ByteString o = (ByteString) other;

        if (o.size() != size()) return false;
        if (size() == 0) return true;

        if (o.hash != 0 && hash != 0 && o.hash != hash) return false;
        return equalsRange(o);
    }

    private boolean equalsRange(ByteString o) {
        for (int i = 0; i < size(); i++) {
            if (o.bytes[i] != bytes[i]) return false;
        }
        return true;
    }


    @Override
    public String toString() {
        return "ByteString{" +
                "bytes=" + Arrays.toString(bytes) +
                ", hash=" + hash +
                '}';
    }

    public String toString(Charset charset) {
        return new String(bytes, 0, size(), charset);
    }
}
