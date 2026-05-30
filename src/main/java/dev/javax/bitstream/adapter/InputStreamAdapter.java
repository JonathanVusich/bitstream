package dev.javax.bitstream.adapter;

import dev.javax.bitstream.ByteSource;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Simple adapter class to enable input streams to implement ByteSource for a single
 * unified interface in the stream implementations.
 */
public final class InputStreamAdapter implements ByteSource {

    private final InputStream inputStream;

    public InputStreamAdapter(final InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void read(byte[] bytes, int numBytes) throws IOException {
        if (inputStream.read(bytes, 0, numBytes) != numBytes) {
            throw new EOFException("Not enough bytes available!");
        }
    }

    @Override
    public void close() {
        // We do NOT close the underlying stream as this may not be the behavior desired.
    }
}
