package cn.ryoii.util;

import cn.ryoii.baka.Baka;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.nio.file.Path;

public class ProjectSetupDbTester {

    public static Path dbPath = Path.of("db/test_" + System.currentTimeMillis());

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
