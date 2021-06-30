package io.plyschik.springbootblog.exception;

public class TagNotFound extends Exception {
    public TagNotFound() {
        super("Tag not found.");
    }

    public TagNotFound(String message) {
        super(message);
    }
}
