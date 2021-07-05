package io.plyschik.springbootblog.exception;

public class EmailAddressIsAlreadyTakenException extends RuntimeException {
    public EmailAddressIsAlreadyTakenException() {
        super("E-mail address is taken.");
    }

    public EmailAddressIsAlreadyTakenException(String message) {
        super(message);
    }
}
