package ru.endlesscode.badger.utils;

import org.jetbrains.annotations.Nullable;

import java.io.*;

/**
 * Created by OsipXD on 16.03.2016
 * It is part of the Badger.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class FileUtils {
    /**
     * Export a resource embedded into a Jar file to the local file path.
     *
     * @param resourceName ie.: "file.ext"
     * @throws Exception
     */
    static public void exportResource(String resourceName) throws Exception {
        try (InputStream stream = Utils.class.getClass().getResourceAsStream("/" + resourceName); OutputStream resStreamOut = new FileOutputStream(new File("Badger", resourceName))) {
            if (stream == null) {
                throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
            }

            int readBytes;
            byte[] buffer = new byte[4096];
            while ((readBytes = stream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
        }
    }

    @Nullable
    public static File[] listOfImages(String dirPath) {
        return listOfImages(new File(dirPath));
    }

    @Nullable
    public static File[] listOfImages(File dir) {
        if (!dir.exists() || dir.isFile()) {
            return null;
        }

        return dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jpg") || name.endsWith(".JPG");
            }
        });
    }
}
