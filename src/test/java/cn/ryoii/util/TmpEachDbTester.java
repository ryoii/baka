package cn.ryoii.util;

import cn.ryoii.baka.Baka;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

public class TmpEachDbTester {

    @TempDir
    public static Path dbPath;
    protected Baka db;

    @BeforeEach
    public void setupDb() {
        db = Baka.open(dbPath.toFile());
    }

    @AfterEach
    public void cleanDb() {
        db.close();
    }
}
