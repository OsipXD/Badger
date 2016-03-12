package ru.endlesscode.badger;

import ru.endlesscode.badger.face.Face;
import ru.endlesscode.badger.thread.NotifyingThread;
import ru.endlesscode.badger.thread.ThreadCompleteListener;
import ru.endlesscode.badger.utils.Utils;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Badger implements ThreadCompleteListener {
    public static final int MAX_THREAD_NUM = 10;

    private final String nameFile;
    private final String photoDir;

    private int threadNum = 0;
    private final List<Thread> threads = new ArrayList<>();

    public Badger(String nameFile, String photoDir) {
        this.nameFile = nameFile;
        this.photoDir = photoDir;
    }

    public static void main(String[] args) {
        if (setupBadger()) {
            return;
        }

        processPhotos();
    }

    public void run() {
        try (Scanner namesFile = new Scanner(new File(this.nameFile))) {
            List<String> nameList = new ArrayList<>();
//            while (namesFile.hasNextLine()) {
//                nameList.add(namesFile.nextLine());
//            }

            File photosDir = new File(this.photoDir);
            File[] files = photosDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".jpg") || name.endsWith(".JPG");
                }
            });

            if (files == null) {
                System.out.println("Папка фотографий пуста!");
                return;
            }

            for (File file1 : files) {
                nameList.add(Utils.generateRandomString(10));
            }

            if (nameList.size() != files.length) {
                System.out.println("Количество имен в списке (" + nameList.size() + ") не совпадает с количеством файлов в папке (" + files.length + ")!");
                return;
            }

            for (int i = 0; i < files.length; i++) {
                final File file = files[i];
                final String name = nameList.get(i);

                try {
                    while (this.threadNum >= MAX_THREAD_NUM) {
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                this.processPhoto(file, name);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void processPhoto(final File file, final String name) {
        NotifyingThread newThread = new NotifyingThread() {
            @Override
            public void doRun() {
                try {
                    Face face = new Face(file);
                    face.drawFaceBorder();
                    ImageIO.write(face.getImage(), "jpg", new File((face.isDoubtful() ? "result/check/" : "result/") + name + ".jpg"));
                } catch (Exception e) {
                    System.out.println("Ошибка обработки: " + e.getMessage());

                    try {
                        Files.copy(file.toPath(), new File("result/bad/" + name + ".jpg").toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        };

        newThread.addListener(this);
        newThread.start();

        synchronized (Badger.class) {
            this.threads.add(newThread);
            this.threadNum++;
        }
    }

    public void waitThreads() {
        for (Thread thread : new ArrayList<>(this.threads)) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void processPhotos() {
        long start = System.nanoTime();
        Badger badger = new Badger("names.txt", "photos1");
        badger.run();
        badger.waitThreads();
        System.out.println("Завершено ("+ (System.nanoTime() - start) / 10000000 / 100. + " s)");
    }

    public static boolean setupBadger() {
        File resultFolder = new File("Badger/result");
        File photosFolder = new File("Badger/photos");
        File namesFile = new File("Badger/input.txt");
        boolean reload = false;

        if (!resultFolder.exists()) {
            resultFolder.mkdirs();
            reload = true;
        }

        if (!photosFolder.exists()) {
            photosFolder.mkdirs();
            reload = true;
        }

        if (!namesFile.exists()) {
            try (PrintWriter writer = new PrintWriter(namesFile, "UTF-8")) {
                writer.println(
                        "# Здесь должны быть данные о людях в таком формате:\n" +
                        "#      [Фамилия Имя] (Отчество) : [Тип бейджа] : (Доп. текст) - (Личная цитата)\n" +
                        "# [] - Обязательное, () - Необязательное\n" +
                        "# Фамилия Имя Отчество: пишутся именно в таком порядке и с большей буквы\n" +
                        "# Типы бейджей: школьник, сотрудник, гость, некто (можно использовать сокращения, лавное чтобы начиналось на правильную букву)\n" +
                        "# Доп. текст: тут можно написать должность человека\n" +
                        "# Личная цитата: пишется после тире, цитата для этого ч-ка не рандомится\n" +
                        "#\n" +
                        "# Примеры правильных данных: \n" +
                        "#      Ктулху Владыка Миров : некто - Вот проснусь и всех зохаваю. И никакой Чак Норрис не помешает.\n" +
                        "#      Иванов Пётр : шк.\n" +
                        "#      Ройтберг Михаил Абрамович : создатель : Директор ЗПШ\n" +
                        "#\n" +
                        "# Строки, начинающиеся с '#' считаются комментариями и не учитываются\n"
                );
                reload = true;
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        if (reload) {
            System.out.println("Папка \"Badger\" подготовлена к работе.\n" +
                    "1. Закиньте фотографии в папку \"photos\"\n" +
                    "2. Внесите данные в файл \"input.txt\"\n" +
                    "3. Запустите программу заново!"
            );
        }
        return reload;
    }

    @Override
    public void notifyOfThreadComplete(Thread thread) {
        synchronized (Badger.class) {
            this.threads.remove(thread);
            this.threadNum--;
        }
    }
}
