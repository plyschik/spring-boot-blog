package io.plyschik.springbootblog.exception;

public class CategoryAlreadyExists extends Exception {
    public CategoryAlreadyExists() {
        super("Category already exists.");
    }

    public CategoryAlreadyExists(String message) {
        super(message);
    }
}
