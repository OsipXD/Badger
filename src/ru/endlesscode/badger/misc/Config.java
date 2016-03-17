package ru.endlesscode.badger.misc;

import ru.endlesscode.badger.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Created by OsipXD on 14.03.2016
 * It is part of the Badger.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class Config {
    public static int PAGE_WIDTH;
    public static int PAGE_HEIGHT;
    public static int PAGE_SPACE;

    public static int BADGE_WIDTH;
    public static int BADGE_HEIGHT;

    public static int MAX_THREAD_NUM;
    public static int SEND_COMPRESSION;

    public static int PHOTO_X;
    public static int PHOTO_Y;
    public static float PHOTO_WIDTH;
    public static float PHOTO_HEIGHT;
    public static float RATIO;

    public static float CRITICAL_WIDTH;
    public static float CRITICAL_HEIGHT;

    public static int TEXT_AREA_X;
    public static int TEXT_AREA_Y;
    public static int TEXT_AREA_WIDTH;
    public static int TEXT_AREA_HEIGHT;

    public static String NAME_FONT;
    public static int NAME_MIN_SIZE;
    public static int NAME_MAX_SIZE;

    public static String QUOTE_FONT;
    public static float QUOTE_MIN_SIZE;
    public static float QUOTE_MAX_SIZE;

    public static void loadConfig() throws Exception {
        Properties config = new Properties();
        File configFile = new File("Badger", "badger.properties");
        if (!configFile.exists()) {
            FileUtils.exportResource("badger.properties");
        }

        try (FileInputStream fis = new FileInputStream(configFile)) {
            config.load(fis);
        }

        PAGE_WIDTH = Integer.parseInt(config.getProperty("page.width", "2480"));
        PAGE_HEIGHT = Integer.parseInt(config.getProperty("page.height", "3508"));
        PAGE_SPACE = Integer.parseInt(config.getProperty("page.space", "5"));

        BADGE_WIDTH = Integer.parseInt(config.getProperty("badge.width", "1063"));
        BADGE_HEIGHT = Integer.parseInt(config.getProperty("badge.height", "650"));

        MAX_THREAD_NUM = Integer.parseInt(config.getProperty("photo.recognition.threads", "10"));
        SEND_COMPRESSION = Integer.parseInt(config.getProperty("photo.recognition.compress", "3"));

        PHOTO_X = Integer.parseInt(config.getProperty("photo.x", "660"));
        PHOTO_Y = Integer.parseInt(config.getProperty("photo.y", "80"));
        PHOTO_WIDTH = Integer.parseInt(config.getProperty("photo.width", "320"));
        PHOTO_HEIGHT = Integer.parseInt(config.getProperty("photo.height", "490"));
        RATIO = PHOTO_WIDTH / PHOTO_HEIGHT;

        CRITICAL_WIDTH = Float.parseFloat(config.getProperty("photo.critical.width", "0.2"));
        CRITICAL_HEIGHT = Float.parseFloat(config.getProperty("photo.critical.height", "0.3"));

        TEXT_AREA_X = Integer.parseInt(config.getProperty("textarea.x", "75"));
        TEXT_AREA_Y = Integer.parseInt(config.getProperty("textarea.y", "285"));
        TEXT_AREA_WIDTH = Integer.parseInt(config.getProperty("textarea.width", "560"));
        TEXT_AREA_HEIGHT = Integer.parseInt(config.getProperty("textarea.height", "280"));

        NAME_FONT = config.getProperty("name.font", "Arial");
        NAME_MIN_SIZE = Integer.parseInt(config.getProperty("name.min", "50"));
        NAME_MAX_SIZE = Integer.parseInt(config.getProperty("name.max", "80"));
        QUOTE_FONT = config.getProperty("quote.font", "Arial");
        QUOTE_MIN_SIZE = Integer.parseInt(config.getProperty("quote.min", "50"));
        QUOTE_MAX_SIZE = Integer.parseInt(config.getProperty("quote.max", "80"));
    }
}
