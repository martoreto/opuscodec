package com.score.rahasak.utils;

public class OpusError extends RuntimeException {
    public OpusError() {
    }

    public OpusError(String message) {
        super(message);
    }

    static int throwIfError(int error) {
        if (error < 0) {
            throw new OpusError("Error from codec: " + error);
        }
        return error;
    }
}
