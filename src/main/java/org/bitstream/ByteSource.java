package org.bitstream;

import java.io.IOException;
import java.nio.ByteOrder;

public interface ByteSource extends AutoCloseable {

    ByteOrder byteOrder();
    long read() throws IOException;
}
