package dev.javax.bitstream.adapter;

import dev.javax.bitstream.ByteSource;

import java.io.IOException;
import java.io.InputStream;

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
    public void close() throws Exception {
        inputStream.close();
    }
}
