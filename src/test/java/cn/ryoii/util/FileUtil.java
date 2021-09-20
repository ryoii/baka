package cn.ryoii.util;

import java.io.File;

public class FileUtil {

    @SuppressWarnings("unused")
    public static void delete(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File value : files) {
                    delete(value);
                }
            }
        }
        boolean d = file.delete();
    }
}
