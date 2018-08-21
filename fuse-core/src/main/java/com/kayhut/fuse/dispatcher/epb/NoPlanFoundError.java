package com.kayhut.fuse.dispatcher.epb;

public class NoPlanFoundError extends RuntimeException {
    public NoPlanFoundError() {}

    public NoPlanFoundError(String message) {
        super(message);
    }

    public NoPlanFoundError(String message, Throwable cause) {
        super(message, cause);
    }
}
