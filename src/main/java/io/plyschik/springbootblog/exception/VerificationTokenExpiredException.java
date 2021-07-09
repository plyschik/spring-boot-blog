package io.plyschik.springbootblog.exception;

public class VerificationTokenExpiredException extends RuntimeException {
    public VerificationTokenExpiredException() {
        super("Verification token is expired.");
    }

    public VerificationTokenExpiredException(String message) {
        super(message);
    }
}
