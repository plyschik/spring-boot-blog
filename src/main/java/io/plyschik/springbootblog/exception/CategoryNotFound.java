package io.plyschik.springbootblog.exception;

public class CategoryNotFound extends Exception {
    public CategoryNotFound() {
        super("Category not found.");
    }

    public CategoryNotFound(String message) {
        super(message);
    }
}
