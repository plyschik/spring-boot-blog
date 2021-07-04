package io.plyschik.springbootblog.exception;

public class UserNotFound extends Exception {
    public UserNotFound() {
        super("User not found.");
    }

    public UserNotFound(String message) {
        super(message);
    }
}
