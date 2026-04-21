package org.bitstream.adapter;

import org.bitstream.ByteSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

public final class InputStreamAdapter implements ByteSource {

    private final InputStream inputStream;
    private final ByteOrder byteOrder;

    public InputStreamAdapter(final InputStream inputStream, final ByteOrder byteOrder) {
        this.inputStream = inputStream;
        this.byteOrder = byteOrder;
    }

    @Override
    public ByteOrder byteOrder() {
        return byteOrder;
    }

    @Override
    public long read() throws IOException {
        return inputStream.read();
    }

    @Override
    public void close() throws Exception {
        inputStream.close();
    }
}
