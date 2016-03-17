package ru.endlesscode.badger.misc;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Created by OsipXD on 17.03.2016
 * It is part of the Badger.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class Log {
    private static Logger logger;

    public static void init() throws IOException {
        logger = Logger.getLogger("Badger");
        logger.setUseParentHandlers(false);
        //noinspection ResultOfMethodCallIgnored
        new File("Badger/logs.log").delete();
        FileHandler fh = new FileHandler("Badger/logs.log");
        fh.setFormatter(new LogFormatter());
        logger.addHandler(fh);
    }

    public static Logger getLogger() {
        return logger;
    }

    private static class LogFormatter extends Formatter {
        final DateFormat df = new SimpleDateFormat("[dd/MM/yyyy hh:mm:ss]");

        @Override
        public String format(LogRecord record) {
            StringBuilder builder = new StringBuilder();
            builder.append(df.format(new Date(record.getMillis())));
            builder.append(" [").append(record.getLevel()).append("] ");
            builder.append(formatMessage(record)).append("\n");
            return builder.toString();
        }
    }
}
