package io.plyschik.springbootblog.exception;

public class VerificationTokenNotFoundException extends RuntimeException {
    public VerificationTokenNotFoundException() {
        super("Verification token not found.");
    }

    public VerificationTokenNotFoundException(String message) {
        super(message);
    }
}
