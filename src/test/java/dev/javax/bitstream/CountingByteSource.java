package dev.javax.bitstream;

import java.io.EOFException;

/**
 * Serves bytes from an array and counts the reads made on it, so tests can check a stream
 * doesn't go back to its source while it still holds the bits it needs.
 */
final class CountingByteSource implements ByteSource {

    private final byte[] bytes;

    private int position;

    private int readCount;

    CountingByteSource(final byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public void read(final byte[] buffer, final int numBytes) throws EOFException {
        readCount++;
        if (position + numBytes > bytes.length) {
            throw new EOFException("Not enough bytes available!");
        }
        System.arraycopy(bytes, position, buffer, 0, numBytes);
        position += numBytes;
    }

    int readCount() {
        return readCount;
    }

    @Override
    public void close() {
    }
}
