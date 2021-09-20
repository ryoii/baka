package cn.ryoii.util;

import cn.ryoii.baka.Baka;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

public class TmpSetupDbTester {

    @TempDir
    public static Path dbPath;
    protected static Baka db;

    @BeforeAll
    public static void setupDb() {
        db = Baka.open(dbPath.toFile());
    }

    @AfterAll
    public static void cleanDb() {
        db.close();
    }
}
