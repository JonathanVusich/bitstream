package org.bitstream.adapter;

import org.bitstream.ByteSink;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.Objects;

public final class OutputStreamAdapter implements ByteSink {

    private final OutputStream outputStream;
    private final ByteOrder byteOrder;

    public OutputStreamAdapter(final OutputStream outputStream, final ByteOrder byteOrder) {
        this.outputStream = Objects.requireNonNull(outputStream);
        this.byteOrder = Objects.requireNonNull(byteOrder);
    }

    @Override
    public ByteOrder byteOrder() {
        return byteOrder;
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
