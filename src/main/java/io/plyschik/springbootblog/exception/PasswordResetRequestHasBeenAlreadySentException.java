package io.plyschik.springbootblog.exception;

public class PasswordResetRequestHasBeenAlreadySentException extends RuntimeException {
    public PasswordResetRequestHasBeenAlreadySentException() {
        super("Password reset request has been already sent.");
    }

    public PasswordResetRequestHasBeenAlreadySentException(String message) {
        super(message);
    }
}
