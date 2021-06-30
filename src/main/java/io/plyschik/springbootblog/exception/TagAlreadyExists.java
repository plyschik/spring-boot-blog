package io.plyschik.springbootblog.exception;

public class TagAlreadyExists extends Exception {
    public TagAlreadyExists() {
        super("Tag already exists.");
    }

    public TagAlreadyExists(String message) {
        super(message);
    }
}
