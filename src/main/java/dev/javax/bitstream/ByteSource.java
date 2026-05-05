package dev.javax.bitstream;

import java.io.IOException;
import java.nio.ByteOrder;

public interface ByteSource extends AutoCloseable {

    int read(byte[] buffer) throws IOException;
}
