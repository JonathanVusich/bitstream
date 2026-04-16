package org.bitstream;

import java.io.IOException;
import java.nio.ByteOrder;

public interface BitSource {

    ByteOrder byteOrder();
    long read() throws IOException;
}
