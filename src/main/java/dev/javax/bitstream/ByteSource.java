package dev.javax.bitstream;

import java.io.IOException;

/**
 * Byte source interface that enables reading bytes from classes that do not extend {@link java.io.InputStream}.
 * @author Jonathan Vusich
 */
public interface ByteSource extends AutoCloseable {

    /**
     * Writes bytes into the buffer provided + returns the number of bytes written as an int.
     * Must return -1 if no bytes were actually written.
     * @param buffer The buffer that must be written to.
     * @return the number of bytes written (or -1) if no bytes were written.
     * @throws IOException if the byte request cannot be fulfilled due to an exceptional condition
     */
    int read(byte[] buffer) throws IOException;
}
