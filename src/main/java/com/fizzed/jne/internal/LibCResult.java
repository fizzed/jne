package com.fizzed.jne.internal;

import com.fizzed.jne.LibC;
import com.fizzed.jne.SemanticVersion;

public class LibCResult {
    private final LibC libC;
    private final SemanticVersion version;

    public LibCResult(LibC libC, SemanticVersion version) {
        this.libC = libC;
        this.version = version;
    }

    public LibC getLibC() {
        return libC;
    }

    public SemanticVersion getVersion() {
        return version;
    }
}
