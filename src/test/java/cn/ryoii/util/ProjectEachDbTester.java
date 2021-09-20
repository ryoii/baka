package cn.ryoii.util;

import cn.ryoii.baka.Baka;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Path;

public class ProjectEachDbTester {

    public static Path dbPath = Path.of("db/test_" + System.currentTimeMillis());
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
