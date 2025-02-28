package dev.noodle.models;

import java.io.File;
import java.net.URLDecoder;

public class CreateSQLiteDB {
    public static String getJarDir() {
        String path = CreateSQLiteDB.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPath;
        try {
            decodedPath = URLDecoder.decode(path, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return "";
        }
        File file = new File(decodedPath);
        return file.getParent();
    }
}
