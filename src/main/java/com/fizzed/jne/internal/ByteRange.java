package com.fizzed.jne.internal;

public class ByteRange {

    private final long index;
    private final long length;

    public ByteRange(long index, long length) {
        this.index = index;
        this.length = length;
    }

    public long getIndex() {
        return index;
    }
    public long getLength() {
        return length;
    }

}