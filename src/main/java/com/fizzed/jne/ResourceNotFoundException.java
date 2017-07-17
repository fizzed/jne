package com.fizzed.jne;

import java.io.IOException;

public class ResourceNotFoundException extends IOException {

    public ResourceNotFoundException(String msg) {
        super(msg);
    }
}
