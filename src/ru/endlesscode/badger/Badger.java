package ru.endlesscode.badger;

import ru.endlesscode.badger.misc.Config;
import ru.endlesscode.badger.utils.Utils;

import java.io.File;
import java.io.IOException;

public class Badger {
    private static EntryManager entryManager;

    public static void main(String[] args) {
        // Первичная подготовка
        try {
            setupBadger();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        entryManager = new EntryManager("input.txt");

        // Обработка фотографий
        processPhotos();
        BadgePainter.initFonts();

        try {
            BadgePainter.drawBadge(entryManager.getEntryList().get(2));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setupBadger() throws Exception {
        File resultFolder = new File("Badger/result/confirmed");
        File photosFolder = new File("Badger/photos");

        if (!resultFolder.exists()) {
            resultFolder.mkdirs();
        }

        if (!photosFolder.exists()) {
            photosFolder.mkdirs();
        }

        Config.loadConfig();
        Utils.exportResource("input.txt");

        System.out.println("Папка \"Badger\" подготовлена к работе.\n" +
                "1. Закиньте фотографии в папку \"photos\"\n" +
                "2. Внесите данные в файл \"input.txt\"\n" +
                "3. Выставьте нужные настройки в файле \"badger.properties\""
        );

        Badger.waitEnter();
        //TODO: Проверка выполнения
    }

    public static void processPhotos() {
        PhotoManager photoManager = new PhotoManager("photos");
        long start = System.nanoTime();
        boolean photosDone = photoManager.run();
        while (!photosDone) {
            Badger.waitEnter();
            photosDone = photoManager.run();
        }

        photoManager.waitThreads();

        System.out.println("Фотографии вырезаны ("+ (System.nanoTime() - start) / 10000000 / 100. + " s)\n" +
                "1. Проверьте правильность вырезанных фотографий\n" +
                "2. Проверьте сомнительные фоторафии в папке \"check\" и \"bad\"\n" +
                "3. Вырежте вручную неудачные фотографии\n" +
                "4. Перенесите удачные фотографии в папку \"confirmed\""
        );

        Badger.waitEnter();

        //TODO: Проверка выполнения
    }

    public static void waitEnter() {
        System.out.println("[Нажмите Enter чтобы продолжить]");
        try {
            //noinspection ResultOfMethodCallIgnored
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static EntryManager getEntryManager() {
        return entryManager;
    }
}
