package ru.endlesscode.badger.misc;

import ru.endlesscode.badger.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Created by OsipXD on 14.03.2016
 * It is part of the Badger.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class Config {
    private static Properties config = new Properties();

    public static void loadConfig() throws Exception {
        File configFile = new File("Badger", "badger.properties");
        if (!configFile.exists()) {
            Utils.exportResource("badger.properties");
        }

        try (FileInputStream fis = new FileInputStream(configFile)) {
            config.load(fis);
        }
    }

    public static Properties getConfig() {
        if (config == null) {
            try {
                loadConfig();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return config;
    }


}
