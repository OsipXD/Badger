package ru.endlesscode.badger;

import ru.endlesscode.badger.misc.Config;
import ru.endlesscode.badger.misc.Log;
import ru.endlesscode.badger.misc.Timer;
import ru.endlesscode.badger.utils.FileUtils;

import java.io.File;
import java.io.IOException;

class Badger {

    private static EntryManager entryManager;
    private static QuoteManager quoteManager;

    public static void main(String[] args) {
        // Первичная подготовка
        try {
            setupBadger();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Обработка фотографий
        processPhotos();
        BadgePainter.initFonts();

        // Сборка бейджиков
        BadgePainter.drawAllBadges();

        // Составление листов для печати
        BadgePainter.packBadgesToPrint();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void setupBadger() throws Exception {
        new File("Badger/temp/bad").mkdirs();
        new File("Badger/temp/badges").mkdirs();
        new File("Badger/temp/check").mkdirs();
        new File("Badger/res/fonts").mkdirs();
        new File("Badger/result").mkdirs();

        Log.init();

        File photosDir = new File("Badger/photos");
        photosDir.mkdirs();
        File imagesDir = new File("Badger/res/images");
        imagesDir.mkdirs();

        Config.loadConfig();
        if (!new File("Badger", "input.txt").exists()) {
            FileUtils.exportResource("input.txt");
        }
        if (!new File("Badger", "quotes.txt").exists()) {
            FileUtils.exportResource("quotes.txt");
        }

        System.out.println("Папка \"Badger\" подготовлена к работе.\n" +
                "1. Закиньте фотографии в папку \"photos\"\n" +
                "2. Внесите данные в файл \"input.txt\"\n" +
                "2. Внесите цитаты в файл \"quotes.txt\"\n" +
                "4. Выставьте нужные настройки в файле \"badger.properties\"\n" +
                "5. Поместите файлы оформления в папку \"res\""
        );

        boolean error;
        do {
            error = false;
            Badger.waitEnter();
            for (Entry.EntryType type : Entry.EntryType.values()) {
                File typeFile = new File(imagesDir, type.name().toLowerCase() + ".png");
                if (!typeFile.exists()) {
                    System.out.println("Ошибка: файл \"" + typeFile.getAbsolutePath() + "\" не найден!");
                    error = true;
                }
            }
        } while (error);

        entryManager = new EntryManager("input.txt");
        File[] photos = FileUtils.listOfImages(photosDir);
        assert photos != null;
        while (photos.length != entryManager.getEntryList().size()) {
            System.out.println("Количество имен в списке (" + entryManager.getEntryList().size() + ") не совпадает с количеством фотографий в папке (" + photos.length + ")!");
            Badger.waitEnter();
            entryManager.parse();
            photos = FileUtils.listOfImages(photosDir);
            if (photos == null) {
                photos = new File[0];
            }
        }

        quoteManager = new QuoteManager("quotes.txt");
        Config.loadConfig();
    }

    private static void processPhotos() {
        PhotoManager photoManager = new PhotoManager("photos");
        Timer timer = new Timer();
        timer.start();
        boolean photosDone = photoManager.run();
        while (!photosDone) {
            Badger.waitEnter();
            photosDone = photoManager.run();
        }

        photoManager.waitThreads();
        photoManager.stopProgressBar("Фотографии вырезаны (" + timer.stop() + " s)");

        System.out.println(
                "1. Проверьте правильность вырезанных фотографий\n" +
                        "2. Проверьте сомнительные фоторафии в папке \"check\" и \"bad\"\n" +
                        "3. Вырежте вручную неудачные фотографии\n" +
                        "4. Перенесите готовые фотографии в корень папки \"temp\""
        );

        File[] badFiles;
        File[] checkFiles;
        do {
            Badger.waitEnter();
            badFiles = FileUtils.listOfImages("Badger/temp/bad");
            checkFiles = FileUtils.listOfImages("Badger/temp/check");

            if ((badFiles == null || badFiles.length == 0) && (checkFiles == null || checkFiles.length == 0)) {
                break;
            }

            System.out.println("Ошибка: по итогам человеческой обработки папки \"bad\" и \"check\" должны стать пустыми!");
        } while (true);

        File[] validFiles = FileUtils.listOfImages("Badger/temp");
        while (validFiles == null || validFiles.length != entryManager.getEntryList().size()) {
            System.out.println("Ошибка: количество фотографий в папке \"temp\" не соответствует количеству записанных людей");
            Badger.waitEnter();
            validFiles = FileUtils.listOfImages("Badger/temp");
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void waitEnter() {
        System.out.println("[Нажмите Enter чтобы продолжить]");
        try {
            for (int i = 0; i < System.in.available(); i++) {
                System.in.read();
            }
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static EntryManager getEntryManager() {
        return entryManager;
    }

    public static QuoteManager getQuoteManager() {
        return quoteManager;
    }
}
