package dev.javax.bitstream;

import java.io.IOException;

public interface ByteSource extends AutoCloseable {

    int read(byte[] buffer) throws IOException;
}
