package ru.endlesscode.badger.face;

/**
 * Created by OsipXD on 10.12.2015
 * It is part of the Badger.
 * All rights reserved 2014 - 2015 © «EndlessCode Group»
 */
@SuppressWarnings("unused")
class FaceNotFoundException extends Exception {
    public FaceNotFoundException() {
        super();
    }

    public FaceNotFoundException(String message) {
        super(message);
    }

    public FaceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public FaceNotFoundException(Throwable cause) {
        super(cause);
    }
}
