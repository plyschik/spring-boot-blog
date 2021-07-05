package io.plyschik.springbootblog.exception;

public class TagAlreadyExistsException extends RuntimeException {
    public TagAlreadyExistsException() {
        super("Tag already exists.");
    }

    public TagAlreadyExistsException(String message) {
        super(message);
    }
}
