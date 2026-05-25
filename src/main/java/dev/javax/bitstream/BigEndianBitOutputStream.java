package dev.javax.bitstream;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import static dev.javax.bitstream.Utils.toBeBytes;

/**
 * Bit output stream that writes bits in big endian byte order to the underlying byte sink.
 * @author Jonathan Vusich
 */
final class BigEndianBitOutputStream implements BitOutputStream {

    private final ByteSink byteSink;

    private long buffer;
    private int bitsInBuffer;

    BigEndianBitOutputStream(final ByteSink byteSink) {
        this.byteSink = Objects.requireNonNull(byteSink);
    }

    /**
     * Writes the specified number of bits in big endian byte order to the underlying byte sink.
     *
     * @param bits the raw bit value that should be written. Extra bits in this value will be discarded.
     * @param numBits the number of bits that should be read in the range (1, 64) exclusive.
     * @throws IOException if the underlying sink throws an error during the write.
     */
    @Override
    public void writeBits(final long bits, final int numBits) throws IOException {
        // Can only represent discrete sizes of up to 63 without losing information due to the sign bit
        // If you need 64 bits you may as well just read a long using a different stream implementation.
        if (numBits > 63 || numBits < 1) {
            throw new IllegalArgumentException("Invalid number of bits to be written!");
        }

        // Buffer holds 64 bits.
        // 0000000000000000000000000000000000000000000000000000000000000000
        // When new bits are written, they get written into the buffer LTR.
        //
        // Bits to add: 101100011100
        // 1011000111000000000000000000000000000000000000000000000000000000
        // Bits to add: 111111
        // 1011000111001111100000000000000000000000000000000000000000000000

        // Bits have to be cleaned in case there are extra bits in the input that are not declared.
        final int shift = Long.SIZE - numBits;
        final long cleanedBits = (bits << shift) >>> shift;

        // Compute any extra bits that will not fit in the buffer
        final var remainingBits = Long.SIZE - bitsInBuffer - numBits;

        if (remainingBits < 0) {
            // We do not have enough space in our buffer, so we must write a partial number of bits,
            // flush the buffer, then write the remaining bits.
            final var partialWrite = (cleanedBits >>> -remainingBits);
            this.buffer |= partialWrite;

            byteSink.write(toBeBytes(buffer));

            // Write the remaining bits to the bit buffer
            this.buffer = cleanedBits << (Long.SIZE + remainingBits);
            this.bitsInBuffer = -remainingBits;
            return;
        }
        // All bits will fit in the buffer
        this.buffer |= cleanedBits << (Long.SIZE - bitsInBuffer - numBits);
        bitsInBuffer += numBits;
    }

    /**
     * Flushes any pending writes to the underlying byte sink. This must be done to ensure that
     * the underlying byte sink receives all bits.
     * This method is called when the stream is closed.
     * @throws IOException if the underlying sink throws an error during the write.
     */
    @Override
    public void flush() throws IOException {
        if (bitsInBuffer > 0) {
            final var byteArray = toBeBytes(buffer);
            var numBytes = bitsInBuffer / 8;
            final var raggedBits = bitsInBuffer % 8;
            // Read an extra byte to cover any extra ragged bits
            if (raggedBits > 0) {
                numBytes++;
            }
            final var bytesToWrite = Arrays.copyOfRange(byteArray, 0, numBytes);
            byteSink.write(bytesToWrite);

            // Reset the buffer + bit count
            buffer = 0;
            bitsInBuffer = 0;
        }
    }

    /**
     * Closes the stream by flushing any pending writes to the underlying byte sink.
     * @throws IOException if the underlying sink throws an error during the write.
     */
    @Override
    public void close() throws Exception {
        flush();
    }
}
