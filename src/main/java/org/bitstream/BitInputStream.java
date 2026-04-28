package org.bitstream;

import org.bitstream.adapter.InputStreamAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

public interface BitInputStream {


    static BitInputStream wrap(final InputStream inputStream, final ByteOrder byteOrder) {
        return wrap(new InputStreamAdapter(inputStream, byteOrder));
    }

    static BitInputStream wrap(final ByteSource byteSource) {
        if (byteSource.byteOrder() == ByteOrder.LITTLE_ENDIAN) {
            return new LittleEndianBitInputStream(byteSource);
        }
        return new BigEndianBitInputStream(byteSource);
    }

    long readBits(final int numBits) throws IOException;

    void readToByteBoundary() throws IOException;
}
