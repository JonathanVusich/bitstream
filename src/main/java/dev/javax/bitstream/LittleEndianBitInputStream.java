package dev.javax.bitstream;

import java.io.EOFException;
import java.io.IOException;
import java.util.Objects;

import static dev.javax.bitstream.Utils.fromLeBytes;

/**
 * Bit input stream that reads bits in little endian byte order from the underlying byte stream.
 * @author Jonathan Vusich
 */
final class LittleEndianBitInputStream implements BitInputStream {

    private final byte[] BUFFER = new byte[8];

    private final ByteSource byteSource;

    private long buffer;
    private int bitsInBuffer = 0;

    LittleEndianBitInputStream(final ByteSource bitSource) {
        this.byteSource = Objects.requireNonNull(bitSource);
    }

    /**
     * Returns a long value with the specified number of bits read in little endian byte order
     * from the underlying byte source.
     *
     * @param numBits the number of bits that should be read in the range (1, 64) exclusive.
     * @return The bit value read represented by a long.
     * @throws EOFException if the underlying source does not have enough bytes to fulfill the request.
     * @throws IOException if the underlying source throws an error during the read.
     */
    public long readBits(final int numBits) throws EOFException, IOException {
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
        // Bits read: 00100000
        // 0000000010011000000101000000100000001000000111000000000110000100

        final var shift = Long.SIZE - numBits;
        // Isolate the number of bits requested from the rightmost side.
        final var bits = (this.buffer << shift) >>> shift;

        // Compute remaining number of bits
        final var remainingBits = numBits - bitsInBuffer;

        if (remainingBits > 0) {
            // We do not have enough bits in our buffer.
            refill();

            // If the refill was unsuccessful, we are out of available input.
            if (bitsInBuffer < remainingBits) {
                throw new EOFException("No more bytes available!");
            }

            // Make a local copy of the buffer bits + right shift to drop extra bits
            final var extraShift = Long.SIZE - remainingBits;
            var extraBits = (this.buffer << extraShift) >>> (extraShift - (numBits - remainingBits));

            // Reduce the bitcount
            this.bitsInBuffer -= remainingBits;
            // Rotate the buffer to the right
            this.buffer >>>= remainingBits;

            return bits | extraBits;
        }

        // Reduce the bitcount
        this.bitsInBuffer -= numBits;
        // Rotate the buffer to the right
        this.buffer >>>= numBits;

        return bits;
    }

    /**
     * Discards bits until the next byte boundary.
     */
    @Override
    public void alignToByte() {
        final var raggedBits = bitsInBuffer % 8;
        if (raggedBits > 0) {
            bitsInBuffer -= raggedBits;
            buffer >>>= raggedBits;
        }
    }

    private void refill() throws IOException {
        final int numBytes = this.byteSource.read(BUFFER);
        buffer = fromLeBytes(BUFFER);
        bitsInBuffer = numBytes * Byte.SIZE;
    }
}
