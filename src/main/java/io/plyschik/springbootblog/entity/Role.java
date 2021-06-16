package io.plyschik.springbootblog.entity;

public enum Role {
    USER, MODERATOR, ADMINISTRATOR;

    @Override
    public String toString() {
        return "ROLE_" + name();
    }
}
