package dev.javax.bitstream;

import java.io.IOException;

/**
 * Byte sink interface that enables writing bytes to classes that do not extend {@link java.io.OutputStream}.
 * @author Jonathan Vusich
 */
public interface ByteSink extends AutoCloseable {

    /**
     * Consumes bytes from the buffer provided. The stream reuses the buffer once this returns,
     * so copy any bytes that must be kept.
     * @param bytes The bytes that must be consumed
     * @throws IOException if the byte consumption cannot be fulfilled due to an exceptional condition
     */
    void write(byte[] bytes) throws IOException;
}
