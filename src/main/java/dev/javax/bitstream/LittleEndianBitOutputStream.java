package dev.javax.bitstream;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import static dev.javax.bitstream.Utils.toLeBytes;

final class LittleEndianBitOutputStream implements BitOutputStream {

    private final ByteSink byteSink;

    private long buffer;
    private int bitsInBuffer;

    LittleEndianBitOutputStream(final ByteSink byteSink) {
        this.byteSink = Objects.requireNonNull(byteSink);
    }

    /**
     * The byte order of the input is assumed to be big-endian.
     * @param numBits
     * @param bits
     * @throws IOException
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
        // When new bits are written, they get written into the buffer RTL.
        //
        // Bits to add: 101100011100
        // 0000000000000000000000000000000000000000000000000000101100011100
        // Bits to add: 111111
        // 0000000000000000000000000000000000000000000000011111101100011100

        // Bits have to be cleaned in case there are extra bits in the input that are not declared.
        final int shift = Long.SIZE - numBits;
        final long cleanedBits = (bits << shift) >>> shift;

        // Compute any extra bits that will not fit in the buffer
        final var remainingBits = Long.SIZE - bitsInBuffer - numBits;

        if (remainingBits < 0) {
            // We do not have enough space in our buffer, so we must write a partial number of bits,
            // flush the buffer, then write the remaining bits.

            // Java by default will only consider the bottom 6 bits of a modulo operation on longs.
            // When we have 64 bits in our buffer exactly, this causes a problem as we shift by zero which is a full
            // write, not a partial write.
            //
            // This hacky trick does the following: creates a mask of 0s if we have 64 or more bits and a mask of 1s otherwise.
            // This will wipe all bits in the partial write bits IF the bit buffer is 64 (to avoid modulo overflow on the shift)
            // otherwise leave them all intact.
            final var writeMask = ((long) (bitsInBuffer - 64) >> 63);
            final var partialWrite = (cleanedBits << bitsInBuffer) & writeMask;
            final var bufferToWrite = partialWrite | this.buffer;

            byteSink.write(toLeBytes(bufferToWrite));

            // Write the remaining bits to the bit buffer
            this.buffer = cleanedBits >>> (Long.SIZE - bitsInBuffer);
            this.bitsInBuffer = -remainingBits;
            return;
        }
        // All bits will fit in the buffer
        this.buffer |= bits << bitsInBuffer;
        bitsInBuffer += numBits;
    }

    @Override
    public void flush() throws IOException {
        if (bitsInBuffer > 0) {
            final var byteArray = toLeBytes(buffer);
            var numBytes = bitsInBuffer / 8;
            final var raggedBits = bitsInBuffer % 8;
            // Read an extra byte to cover any extra ragged bits
            if (raggedBits > 0) {
                numBytes++;
            }
            final var bytesToWrite = Arrays.copyOfRange(byteArray, 0, numBytes);
            byteSink.write(bytesToWrite);

            buffer = 0;
            bitsInBuffer = 0;
        }
    }

    @Override
    public void close() throws Exception {
        flush();
    }
}
