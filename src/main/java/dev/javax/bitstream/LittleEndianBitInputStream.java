package dev.javax.bitstream;

import java.io.EOFException;
import java.io.IOException;
import java.util.Objects;

import static dev.javax.bitstream.Utils.fromLeBytes;
import static dev.javax.bitstream.Utils.zero;

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
     * @throws IOException if the underlying source throws an error during the read.
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
        // Bits read: 00100000
        // 0000000010011000000101000000100000001000000111000000000110000100

        final var shift = Long.SIZE - numBits;
        // Isolate the number of bits requested from the rightmost side.
        final var bits = (this.buffer << shift) >>> shift;

        // Compute remaining number of bits
        final var remainingBits = numBits - bitsInBuffer;

        if (remainingBits > 0) {
            // We do not have enough bits in our buffer.
            refill(remainingBits);

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

    @Override
    public long peekBits(final int numBits) throws IOException {
        if (numBits > MAX_PEEK_BITS || numBits < 1) {
            throw new IllegalArgumentException("Invalid number of bits requested!");
        }
        if (numBits > bitsInBuffer) {
            topUp(numBits - bitsInBuffer);
        }
        final var shift = Long.SIZE - numBits;
        return (this.buffer << shift) >>> shift;
    }

    @Override
    public int countZeros() throws IOException {
        return leadingRunLength(0L);
    }

    @Override
    public int countOnes() throws IOException {
        return leadingRunLength(-1L);
    }

    /**
     * Consumes a run of the bits in {@code runBits}, leaving the bit that ends it unread.
     * Bits above the buffered ones are always 0, so flipping the buffer for a run of 1s
     * stops the count at the end of the buffered bits, and a run of 0s counts on past them.
     */
    private int leadingRunLength(final long runBits) throws IOException {
        var runLength = 0;
        while (Long.numberOfTrailingZeros(buffer ^ runBits) >= bitsInBuffer) {
            // Every buffered bit belongs to the run, so consume them and take one more byte.
            runLength += bitsInBuffer;
            bitsInBuffer = 0;
            try {
                refill(Byte.SIZE);
            } catch (final EOFException endOfInput) {
                return runLength;
            }
        }
        final var bufferedRun = Long.numberOfTrailingZeros(buffer ^ runBits);
        bitsInBuffer -= bufferedRun;
        buffer >>>= bufferedRun;
        return runLength + bufferedRun;
    }

    /**
     * Discards bits until the next byte boundary.
     */
    @Override
    public void alignToByte() {
        final var raggedBits = bitsInBuffer % 8;
        bitsInBuffer -= raggedBits;
        buffer >>>= raggedBits;
    }

    private void refill(final int remainingBits) throws IOException {
        // Zero out the buffer to avoid having past data clutter it
        zero(BUFFER);
        final int numBytes = Math.ceilDiv(remainingBits, 8);
        this.byteSource.read(BUFFER, numBytes);
        buffer = fromLeBytes(BUFFER);
        bitsInBuffer = numBytes * Byte.SIZE;
    }

    /**
     * Reads the bytes holding the missing bits in above the buffered bits, keeping those.
     */
    private void topUp(final int missingBits) throws IOException {
        zero(BUFFER);
        final int numBytes = Math.ceilDiv(missingBits, 8);
        this.byteSource.read(BUFFER, numBytes);
        final var newBits = fromLeBytes(BUFFER);
        this.buffer |= newBits << bitsInBuffer;
        bitsInBuffer += numBytes * Byte.SIZE;
    }
}
