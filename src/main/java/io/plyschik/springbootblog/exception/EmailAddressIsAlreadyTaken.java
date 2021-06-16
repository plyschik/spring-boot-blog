package io.plyschik.springbootblog.exception;

public class EmailAddressIsAlreadyTaken extends Exception {
    public EmailAddressIsAlreadyTaken() {
        super("E-mail address is taken.");
    }

    public EmailAddressIsAlreadyTaken(String message) {
        super(message);
    }
}
