package cn.ryoii.baka;

import cn.ryoii.baka.config.BakaConfig;
import cn.ryoii.baka.engine.BakaEngine;
import cn.ryoii.baka.value.ByteString;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Baka implements AutoCloseable {

    private final static Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private BakaEngine engine;

    public static Baka open(String path) {
        return open(new File(path));
    }

    public static Baka open(File path) {
        return open(path, new BakaConfig());
    }

    public static Baka open(String path, BakaConfig config) {
        return open(new File(path), config);
    }

    public static Baka open(File path, BakaConfig config) {
        Baka baka = new Baka();
        baka.engine = new BakaEngine(path, config);
        try {
            baka.engine.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baka;
    }


    public void put(String key, String value) {
        try {
            engine.put(ByteString.copyFrom(key, DEFAULT_CHARSET), ByteString.copyFrom(value, DEFAULT_CHARSET));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String get(String key) {
        try {
            ByteString b = engine.get(ByteString.copyFrom(key, DEFAULT_CHARSET));
            if (b != null) {
                return b.toString(DEFAULT_CHARSET);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void close() {
        engine.close();
    }
}
