package dev.javax.bitstream.adapter;

import dev.javax.bitstream.ByteSink;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public final class OutputStreamAdapter implements ByteSink {

    private final OutputStream outputStream;

    public OutputStreamAdapter(final OutputStream outputStream) {
        this.outputStream = Objects.requireNonNull(outputStream);
    }

    @Override
    public void write(final byte[] bytes) throws IOException {
        outputStream.write(bytes);
    }

    @Override
    public void close() throws Exception {
        outputStream.close();
    }
}
