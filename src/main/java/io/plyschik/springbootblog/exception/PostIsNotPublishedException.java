package io.plyschik.springbootblog.exception;

public class PostIsNotPublishedException extends RuntimeException {
    public PostIsNotPublishedException() {
        super("Post is not published.");
    }

    public PostIsNotPublishedException(String message) {
        super(message);
    }
}
