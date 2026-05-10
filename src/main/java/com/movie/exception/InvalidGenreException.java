package com.movie.exception;

public class InvalidGenreException extends RuntimeException {
    public InvalidGenreException(String message) {
        super(message);
    }
}
