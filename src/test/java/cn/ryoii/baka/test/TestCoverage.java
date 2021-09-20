package cn.ryoii.baka.test;

import cn.ryoii.baka.Baka;
import cn.ryoii.util.TmpSetupDbTester;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestCoverage extends TmpSetupDbTester {

    @Test
    @Order(1)
    public void writeFirst() {
        db.put("1", "1");
    }

    @Test
    @Order(2)
    public void readFirst() {
        String s = db.get("1");
        Assertions.assertEquals("1", s);
    }

    @Test
    @Order(10)
    public void closeAndRebuild() {
        db.close();
        db = Baka.open(dbPath.toString());
    }

    @Test
    @Order(21)
    public void readFirstAfterRebuild() {
        readFirst();
    }

    @Test
    @Order(22)
    public void writeSecond() {
        db.put("2", "2");
    }

    @Test
    @Order(23)
    public void readFully() {
        String one = db.get("1");
        String two = db.get("2");
        String three = db.get("3");
        Assertions.assertEquals("1", one);
        Assertions.assertEquals("2", two);
        Assertions.assertNull(three);
    }

}
