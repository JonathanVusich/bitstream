package org.bitstream;

import java.io.IOException;
import java.nio.ByteOrder;

public interface ByteSource extends AutoCloseable {

    ByteOrder byteOrder();
    int read(byte[] buffer) throws IOException;
}
