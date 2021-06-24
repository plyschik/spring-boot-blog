package io.plyschik.springbootblog.exception;

public class PostNotFound extends Exception {
    public PostNotFound() {
        super("Post not found.");
    }

    public PostNotFound(String message) {
        super(message);
    }
}
