package ru.endlesscode.badger.misc;

/**
 * Created by OsipXD on 17.03.2016
 * It is part of the Badger.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class ProgressBar extends Thread {
    private final String message;
    private final float maxProgress;
    volatile private int progress;

    public ProgressBar(String message, int maxProgress) {
        this.message = message;
        this.maxProgress = maxProgress;
        this.progress = 0;
    }

    public void increaseProgress() {
        progress++;
    }

    @Override
    public void run() {
        int progress = -1;
        try {
            while (!isInterrupted()) {
                if (progress != this.progress) {
                    float percent = this.progress / this.maxProgress;
                    String out = message + " [";
                    for (int i = 0; i < 20; i++) {
                        out += i < 20 * percent ? "#" : " ";
                    }
                    out += "] " + (int) (percent * 100) + "%";
                    System.out.print("\r" + out);
                    progress = this.progress;
                }

                sleep(100);
            }
        } catch (InterruptedException ignored) {
        }
    }

    public void pause(String message) {
        this.interrupt();
        System.out.println("\r" + message);
    }
}
