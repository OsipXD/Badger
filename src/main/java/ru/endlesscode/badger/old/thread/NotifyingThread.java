package ru.endlesscode.badger.old.thread;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class NotifyingThread extends Thread {
    private final Set<ThreadCompleteListener> listeners = new CopyOnWriteArraySet<>();

    public final void addListener(final ThreadCompleteListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (ThreadCompleteListener listener : listeners) {
            listener.notifyOfThreadComplete(this);
        }
    }

    @Override
    public final void run() {
        try {
            doRun();
        } finally {
            notifyListeners();
        }
    }

    public abstract void doRun();
}