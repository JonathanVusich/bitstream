package org.bitstream;

import org.bitstream.adapter.InputStreamAdapter;
import org.bitstream.converter.EndianAwareReader;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.Objects;

import static org.bitstream.Utils.buildReader;

public final class BitInputStream {

    private final ByteSource byteSource;
    private final EndianAwareReader converter;

    private long buffer;
    private int bitsInBuffer = 0;

    public BitInputStream(final InputStream inputStream, final ByteOrder byteOrder) {
        this.byteSource = new InputStreamAdapter(Objects.requireNonNull(inputStream), Objects.requireNonNull(byteOrder));
        this.converter = buildReader(byteSource.byteOrder());
    }

    public BitInputStream(final ByteSource bitSource) {
        this.byteSource = Objects.requireNonNull(bitSource);
        this.converter = buildReader(Objects.requireNonNull(bitSource.byteOrder()));
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

        // Make a local copy of the buffer bits + make space for remaining bits
        final var bits = this.buffer >>> (Long.SIZE - numBits);

        // Compute remaining number of bits
        final var remainingBits = numBits - bitsInBuffer;

        if (remainingBits > 0) {
            // We do not have enough bits in our buffer, so we must reset + read a new buffer.
            this.buffer = 0;
            this.bitsInBuffer = 0;

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

            final var result = bits | extraBits;

            return converter.read(result);
        }
        // Reduce the bitcount
        this.bitsInBuffer -= numBits;
        // Rotate the buffer to the left
        this.buffer <<= numBits;

        return converter.read(bits);
    }

    private void refill() throws IOException {
        for (int i = 0; i < 8; i++) {
            final long byteValue = this.byteSource.read();
            if (byteValue == -1) {
                // We are out of bytes, cannot continue filling
                break;
            }
            final var leftShift = Long.SIZE - bitsInBuffer - Byte.SIZE;
            final var shiftedVal = byteValue << leftShift;
            buffer |= shiftedVal;
            bitsInBuffer += 8;
        }
    }
}
