package dev.javax.bitstream;

import java.io.IOException;
import java.nio.ByteOrder;

public interface ByteSink extends AutoCloseable {

    void write(byte[] bytes) throws IOException;
}
