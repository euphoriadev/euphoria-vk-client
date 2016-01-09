package ru.euphoriadev.vk.api;

import java.io.IOException;

public class WrongResponseCodeException extends IOException {
    private static final long serialVersionUID = 1L;

    public WrongResponseCodeException(String message) {
        super(message);
    }

}
