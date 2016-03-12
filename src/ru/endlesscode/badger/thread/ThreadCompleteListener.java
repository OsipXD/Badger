package ru.endlesscode.badger.thread;

/**
 * Created by OsipXD on 10.12.2015
 * It is part of the Badger.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
public interface ThreadCompleteListener {
    void notifyOfThreadComplete(final Thread thread);
}