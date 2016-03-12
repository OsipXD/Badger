package ru.endlesscode.badger;

import ru.endlesscode.badger.face.Face;
import ru.endlesscode.badger.thread.NotifyingThread;
import ru.endlesscode.badger.thread.ThreadCompleteListener;
import ru.endlesscode.badger.utils.Utils;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
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

    @Override
    public void notifyOfThreadComplete(Thread thread) {
        synchronized (Badger.class) {
            this.threads.remove(thread);
            this.threadNum--;
        }
    }
}
