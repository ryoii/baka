package cn.ryoii.baka.test;

import cn.ryoii.baka.Baka;
import cn.ryoii.baka.config.BakaConfig;
import cn.ryoii.util.FileUtil;
import org.junit.jupiter.api.*;

import java.nio.file.Path;
import java.util.Objects;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestMerge  {

    public static Path path = Path.of("db/test_merge");
    private static Baka db;

    @BeforeAll
    public static void setup() {
        BakaConfig config = new BakaConfig();
        // 20k max file size
        config.setMaxFileSize(20 * 1024);
        db = Baka.open(path.toFile(), config);
    }

    @AfterAll
    public static void tearDown() {
        db.close();
        FileUtil.delete(path.toFile());
    }

    @Test
    @Order(1)
    public void merge() {
        int num = 1000;
        int offset = Integer.MAX_VALUE >> 1;
        for (int i = 0; i < num; i++) {
            String v = String.valueOf(offset + i);
            db.put(v, v);
        }
        // one readonly db file, one hint file and one active file
        Assertions.assertEquals(3, Objects.requireNonNull(path.toFile().listFiles()).length);
    }

    @Test
    @Order(2)
    public void reload() {
        int num = 1000;
        int offset = Integer.MAX_VALUE >> 1;
        for (int i = 0; i < num; i++) {
            String v = String.valueOf(offset + i);
            String res = db.get(v);
            Assertions.assertEquals(v, res);
        }
    }
}
