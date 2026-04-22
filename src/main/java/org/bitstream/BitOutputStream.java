package org.bitstream;

import org.bitstream.adapter.OutputStreamAdapter;
import org.bitstream.converter.EndianAwareWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Objects;

import static org.bitstream.Utils.*;

public final class BitOutputStream implements AutoCloseable {

    private final ByteSink byteSink;
    // TODO: Change the bit shifts per endianness
    private final EndianAwareWriter endianWriter;

    private long buffer;
    private int bitsInBuffer;

    public BitOutputStream(final OutputStream outputStream, final ByteOrder byteOrder) {
        this.byteSink = new OutputStreamAdapter(Objects.requireNonNull(outputStream), Objects.requireNonNull(byteOrder));
        this.endianWriter = buildWriter(Objects.requireNonNull(byteOrder));
    }

    public BitOutputStream(final ByteSink byteSink) {
        this.byteSink = Objects.requireNonNull(byteSink);
        this.endianWriter = buildWriter(Objects.requireNonNull(byteSink.byteOrder()));
    }

    /**
     * The byte order of the input is assumed to be big-endian.
     * @param numBits
     * @param bits
     * @throws IOException
     */
    public void writeBits(final long bits, final int numBits) throws IOException {
        // Can only represent discrete sizes of up to 63 without losing information due to the sign bit
        // If you need 64 bits you may as well just read a long using a different stream implementation.
        if (numBits > 63 || numBits < 1) {
            throw new IllegalArgumentException("Invalid number of bits to be written!");
        }

        final var convertedBits = endianWriter.write(bits, numBits);

        // Compute any extra bits that will not fit in the buffer
        final var remainingBits = Long.SIZE - bitsInBuffer - numBits;

        if (remainingBits < 0) {
            // We do not have enough space in our buffer, so we must fill + flush the buffer, then write the remaining bits.
            final var partialWrite = bits >>> -remainingBits;
            final var bufferToWrite = partialWrite | this.buffer;

            byteSink.write(toBytes(bufferToWrite));

            // Write the remaining bits to the bit buffer
            this.buffer = bits << (Long.SIZE + remainingBits);
            this.bitsInBuffer = -remainingBits;
            return;
        }
        // All bits will fit in the buffer
        this.buffer |= convertedBits << remainingBits;
        bitsInBuffer += numBits;
    }

    public void flush() throws IOException {
        if (bitsInBuffer > 0) {
            var numBytes = bitsInBuffer / 8;
            final var raggedBits = bitsInBuffer % 8;
            // Read an extra byte to cover any extra ragged bits
            if (raggedBits > 0) {
                numBytes++;
            }
            final var byteArray = toBytes(buffer);
            final var bytesToWrite = Arrays.copyOfRange(byteArray, 0, numBytes);
            byteSink.write(bytesToWrite);
        }
    }

    @Override
    public void close() throws Exception {
        flush();
    }
}
