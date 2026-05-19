package dev.javax.bitstream.adapter;

import dev.javax.bitstream.ByteSource;

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
    public int read(byte[] bytes) throws IOException {
        return inputStream.read(bytes);
    }

    @Override
    public void close() {
        // We do NOT close the underlying stream as this may not be the behavior desired.
    }
}
