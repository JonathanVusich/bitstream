package dev.javax.bitstream;

import java.io.EOFException;
import java.io.IOException;

/**
 * Byte source interface that enables reading bytes from classes that do not extend {@link java.io.InputStream}.
 * @author Jonathan Vusich
 */
public interface ByteSource extends AutoCloseable {

    /**
     * Reads the requested number of bytes into the buffer provided.
     * @param buffer The buffer that must be read to.
     * @param numBytes The number of bytes requested.
     * @throws EOFException if the byte source does not have enough bytes left to fulfill the request.
     * @throws IOException if the byte request cannot be fulfilled due to an exceptional condition.
     */
    void read(byte[] buffer, int numBytes) throws EOFException, IOException;
}
