package org.bitstream;

import org.bitstream.adapter.InputStreamAdapter;
import org.bitstream.converter.EndianConverter;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.Objects;

public final class BitInputStream {

    private final BitSource bitSource;
    private final EndianConverter converter;

    private long buffer;
    private int bitsInBuffer = 0;

    public BitInputStream(final InputStream inputStream, final ByteOrder byteOrder) {
        this.bitSource = new InputStreamAdapter(Objects.requireNonNull(inputStream), Objects.requireNonNull(byteOrder));
        this.converter = buildConverter(bitSource.byteOrder());
    }

    public BitInputStream(final BitSource bitSource) {
        this.bitSource = Objects.requireNonNull(bitSource);
        this.converter = buildConverter(Objects.requireNonNull(bitSource.byteOrder()));
    }

    private EndianConverter buildConverter(final ByteOrder byteOrder) {
        // If there is a byte order difference, reverse the bits that are read.
        if (ByteOrder.BIG_ENDIAN != byteOrder) {
            return Long::reverse;
        }
        // Else we just return as is
        return identity -> identity;
    }

    public long readBits(final int numBits) throws IOException {
        // Can only represent discrete sizes of up to 63 without losing information due to the sign bit
        // If you need 64 bits you may as well just read a long using a different stream implementation.
        if (numBits > 63 || numBits < 1) {
            throw new IllegalArgumentException("Invalid number of bits requested!");
        }
        // We have all the bits we need
        final var bits = this.buffer;
        if (bitsInBuffer < numBits) {
            // Compute remaining number of bits
            final var remainingBits = numBits - bitsInBuffer;

            // Empty the buffer
            this.bitsInBuffer = 0;
            this.buffer <<= numBits;

            refill();

            if (bitsInBuffer < remainingBits) {
                throw new EOFException("No more bytes available!");
            }

            var remainingCopy = this.buffer;
            // Right shift the copy to drop extra bits;
            remainingCopy >>>= Long.SIZE - remainingBits;
            // Left shift the copy back to where it belongs
            remainingCopy <<= Long.SIZE - (numBits - remainingBits);

            // Reduce the bitcount
            this.bitsInBuffer -= remainingBits;
            // Rotate the buffer to the left
            this.buffer <<= remainingBits;

            final var result = bits | remainingCopy;
            return converter.convert(result);
        }
        final var rightShift = Long.SIZE - numBits;
        // Right shift the copy to drop extra bits
        final var copyWithoutExtraBits = bits >>> rightShift;

        // Reduce the bitcount
        this.bitsInBuffer -= numBits;
        // Rotate the buffer to the left
        this.buffer <<= numBits;

        return converter.convert(copyWithoutExtraBits);
    }

    private void refill() throws IOException {
        // Refill after dropping read bits
        while (bitsInBuffer <= Long.SIZE - 8) {
            // We have space for another byte
            final long byteValue = this.bitSource.read();
            if (byteValue == -1) {
                // We are out of bytes
                break;
            }
            final var leftShift = Long.SIZE - bitsInBuffer - 8;
            final var shiftedVal = byteValue << leftShift;
            buffer |= shiftedVal;
            bitsInBuffer += 8;
        }
    }
}
