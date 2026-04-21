package org.bitstream;

import java.io.IOException;
import java.nio.ByteOrder;

public interface ByteSink extends AutoCloseable {

    ByteOrder byteOrder();
    void write(byte[] bytes) throws IOException;
}
