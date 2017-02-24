package ru.endlesscode.badger.old;

import ru.endlesscode.badger.old.face.Face;
import ru.endlesscode.badger.old.misc.Config;
import ru.endlesscode.badger.old.misc.Log;
import ru.endlesscode.badger.old.misc.ProgressBar;
import ru.endlesscode.badger.old.thread.NotifyingThread;
import ru.endlesscode.badger.old.thread.ThreadCompleteListener;
import ru.endlesscode.badger.old.utils.FileUtils;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by OsipXD on 14.03.2016
 * It is part of the BadgerConsole.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class PhotoManager implements ThreadCompleteListener {
    private final String photoDir;
    private final List<Thread> threads = new ArrayList<>();
    private int threadNum = 0;
    private ProgressBar progressBar;

    public PhotoManager(String photoDir) {
        this.photoDir = photoDir;
    }

    public boolean run() {
        List<Entry> entryList = BadgerConsole.getEntryManager().getEntryList();

        File photosDir = new File("BadgerConsole", this.photoDir);
        File[] files = FileUtils.listOfImages(photosDir);

        if (files == null || files.length == 0) {
            System.out.println("Папка фотографий не существует или пуста!");
            return false;
        }

        progressBar = new ProgressBar("Обработка фотографий", files.length);
        progressBar.start();
        for (int i = 0; i < files.length; i++) {
            final File file = files[i];
            final Entry entry = entryList.get(i);

            try {
                while (this.threadNum >= Config.MAX_THREAD_NUM) {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            this.processPhoto(file, entry.getFileName());
        }

        return true;
    }

    private void processPhoto(final File file, final String name) {
        NotifyingThread newThread = new NotifyingThread() {
            @Override
            public void doRun() {
                try {
                    Face face = new Face(file);
                    ImageIO.write(face.getImage(), "jpg", new File("BadgerConsole/temp", (face.isDoubtful() ? "check/" : "") + name + ".jpg"));
                    Log.getLogger().info("Фотография обработана [" + file.getCanonicalPath() + "]");
                } catch (Exception e) {
                    Log.getLogger().warning("PhotoManager#processPhoto(): " + e.getMessage());

                    try {
                        Files.copy(file.toPath(), new File("BadgerConsole/temp/bad/" + name + ".jpg").toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }

                progressBar.increaseProgress();
            }
        };

        newThread.addListener(this);
        newThread.start();

        synchronized (BadgerConsole.class) {
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

    public void stopProgressBar(String message) {
        this.progressBar.pause(message);
    }
}
