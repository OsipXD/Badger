package ru.endlesscode.badger.old.misc;

/**
 * Created by OsipXD on 16.03.2016
 * It is part of the Badger.
 * All rights reserved 2014 - 2016 © «EndlessCode Group»
 */
public class Timer {
    private long start;

    public void start() {
        this.start = System.nanoTime();
    }

    public double stop() {
        return (System.nanoTime() - start) / 10000000 / 100.;
    }
}
