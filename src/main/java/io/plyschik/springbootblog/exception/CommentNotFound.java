package io.plyschik.springbootblog.exception;

public class CommentNotFound extends Exception {
    public CommentNotFound() {
        super("Comment not found.");
    }

    public CommentNotFound(String message) {
        super(message);
    }
}
