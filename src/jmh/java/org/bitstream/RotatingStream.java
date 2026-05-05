package org.bitstream;

import org.apache.commons.lang3.NotImplementedException;

import java.io.InputStream;
import java.util.random.RandomGenerator;

class RotatingStream extends InputStream {

    private static final int UNIQUE_BYTES = 128;
    private static final int BYTES_PER_LONG = 8;
    private final byte[] bytes = new byte[BYTES_PER_LONG * UNIQUE_BYTES];
    private int pos = 0;

    RotatingStream() {
        RandomGenerator.getDefault().nextBytes(bytes);
    }

    @Override
    public int read() {
        if (pos >= bytes.length) {
            pos = 0;
        }
        return bytes[pos++] & 0xFF;
    }

    @Override
    public int read(byte[] buffer) {
        if (pos == UNIQUE_BYTES) {
            pos = 0;
        }
        System.arraycopy(bytes, pos * BYTES_PER_LONG, buffer, 0, BYTES_PER_LONG);
        return BYTES_PER_LONG;
    }
}
