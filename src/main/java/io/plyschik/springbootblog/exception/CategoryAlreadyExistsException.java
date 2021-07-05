package io.plyschik.springbootblog.exception;

public class CategoryAlreadyExistsException extends RuntimeException {
    public CategoryAlreadyExistsException() {
        super("Category already exists.");
    }

    public CategoryAlreadyExistsException(String message) {
        super(message);
    }
}
