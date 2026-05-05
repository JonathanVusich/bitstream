package org.bitstream;

import org.bitstream.adapter.OutputStreamAdapter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;

public interface BitOutputStream extends AutoCloseable {

    static BitOutputStream wrap(OutputStream outputStream, ByteOrder byteOrder) {
        return wrap(new OutputStreamAdapter(outputStream, byteOrder));
    }

    static BitOutputStream wrap(ByteSink byteSink) {
        if (byteSink.byteOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            return new LittleEndianBitOutputStream(byteSink);
        }
        return new BigEndianBitOutputStream(byteSink);
    }

    void writeBits(long bits, int numBits) throws IOException;
    void flush() throws IOException;
}
