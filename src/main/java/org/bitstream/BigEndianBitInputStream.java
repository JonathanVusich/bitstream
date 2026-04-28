package org.bitstream;

import java.io.EOFException;
import java.io.IOException;
import java.util.Objects;

import static org.bitstream.Utils.fromBeBytes;


final class BigEndianBitInputStream implements BitInputStream {

    private final byte[] BUFFER = new byte[8];

    private final ByteSource byteSource;

    private long buffer;
    private int bitsInBuffer = 0;

    public BigEndianBitInputStream(final ByteSource bitSource) {
        this.byteSource = Objects.requireNonNull(bitSource);
    }

    /**
     * @param numBits the number of bits that should be read in the range (1, 64) exclusive.
     * @return a long value that returns the read bits in big endian format (the default for the JVM).
     * @throws IOException
     */
    public long readBits(final int numBits) throws IOException {
        // Can only represent discrete sizes of up to 63 without losing information due to the sign bit
        // If you need 64 bits you may as well just read a long using a different stream implementation.
        if (numBits > 63 || numBits < 1) {
            throw new IllegalArgumentException("Invalid number of bits requested!");
        }

        // Buffer holds 64 bits.
        // 1001100000010100000010000000100000011100000000011000010000100000
        //
        // Bits to read: 8
        // 1001100000010100000010000000100000011100000000011000010000100000
        // Bits read: 10011000
        // 0001010000001000000010000001110000000001100001000010000000000000

        // Make a local copy of the buffer bits + shift right
        // so that the highest bit is in the correct position for the output.
        final var bits = this.buffer >>> (Long.SIZE - numBits);
        // Compute remaining number of bits
        final var remainingBits = numBits - bitsInBuffer;

        if (remainingBits > 0) {
            // We do not have enough bits in our buffer, so we must reset + read a new buffer.
            refill();

            // If the refill was unsuccessful, we are out of available input.
            if (bitsInBuffer < remainingBits) {
                throw new EOFException("No more bytes available!");
            }

            // Make a local copy of the buffer bits + right shift to drop extra bits
            var extraBits = this.buffer >>> (Long.SIZE - remainingBits);

            // Reduce the bitcount
            this.bitsInBuffer -= remainingBits;
            // Rotate the buffer to the left
            this.buffer <<= remainingBits;

            return bits | extraBits;
        }
        // Reduce the bitcount
        this.bitsInBuffer -= numBits;
        // Rotate the buffer to the left
        this.buffer <<= numBits;

        return bits;
    }

    @Override
    public void readToByteBoundary() throws IOException {
        final var raggedBits = bitsInBuffer % 8;
        if (raggedBits > 0) {
            readBits(8 - raggedBits);
        }
    }

    private void refill() throws IOException {
        final int numBytes = this.byteSource.read(BUFFER);
        this.buffer = fromBeBytes(BUFFER);
        bitsInBuffer = numBytes * Byte.SIZE;
    }
}
