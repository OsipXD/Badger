package ru.endlesscode.badger;

import ru.endlesscode.badger.face.Face;
import ru.endlesscode.badger.thread.NotifyingThread;
import ru.endlesscode.badger.thread.ThreadCompleteListener;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by OsipXD on 14.03.2016
 * It is part of the Badger.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class PhotoManager implements ThreadCompleteListener {
    public static final int MAX_THREAD_NUM = 10;

    private final String photoDir;

    private int threadNum = 0;
    private final List<Thread> threads = new ArrayList<>();

    public PhotoManager(String photoDir) {
        this.photoDir = photoDir;
    }

    public boolean run() {
        List<Entry> entryList = Badger.getEntryManager().getEntryList();

        File photosDir = new File("Badger", this.photoDir);
        File[] files = photosDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jpg") || name.endsWith(".JPG");
            }
        });

        if (files.length == 0) {
            System.out.println("Папка фотографий пуста!");
            return false;
        }

        if (entryList.size() != files.length) {
            System.out.println("Количество имен в списке (" + entryList.size() + ") не совпадает с количеством файлов в папке (" + files.length + ")!");
            return false;
        }

        for (int i = 0; i < files.length; i++) {
            final File file = files[i];
            final Entry entry = entryList.get(i);

            try {
                while (this.threadNum >= MAX_THREAD_NUM) {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            this.processPhoto(file, entry.getFileName());
        }

        return true;
    }

    public void processPhoto(final File file, final String name) {
        NotifyingThread newThread = new NotifyingThread() {
            @Override
            public void doRun() {
                String fileName = file.getName().substring(0, file.getName().lastIndexOf('.'));

                try {
                    Face face = new Face(file);
//                    face.drawFaceBorder();
                    //noinspection ResultOfMethodCallIgnored
                    new File("Badger/result/check").mkdirs();
                    ImageIO.write(face.getImage(), "jpg", new File("Badger/result", (face.isDoubtful() ? "check/" + fileName + "_" : "") + name + ".jpg"));
                } catch (Exception e) {
                    System.out.println("Ошибка обработки: " + e.getMessage());

                    try {
                        //noinspection ResultOfMethodCallIgnored
                        new File("Badger/result/bad").mkdirs();
                        Files.copy(file.toPath(), new File("Badger/result/bad/" + fileName + "_" + name + ".jpg").toPath(), StandardCopyOption.REPLACE_EXISTING);
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

    @Override
    public void notifyOfThreadComplete(Thread thread) {
        synchronized (this.getClass()) {
            this.threads.remove(thread);
            this.threadNum--;
        }
    }
}
