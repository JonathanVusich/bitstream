package org.bitstream;

import org.bitstream.adapter.OutputStreamAdapter;
import org.bitstream.converter.EndianConverter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Objects;

import static org.bitstream.Utils.buildConverter;
import static org.bitstream.Utils.toBytes;

public final class BitOutputStream implements AutoCloseable {

    private final ByteSink byteSink;
    // TODO: Change the bit shifts per endianness
    private final EndianConverter converter;

    private long buffer;
    private int bitsInBuffer;

    public BitOutputStream(final OutputStream outputStream, final ByteOrder byteOrder) {
        this.byteSink = new OutputStreamAdapter(Objects.requireNonNull(outputStream), Objects.requireNonNull(byteOrder));
        this.converter = buildConverter(Objects.requireNonNull(byteOrder));
    }

    public BitOutputStream(final ByteSink byteSink) {
        this.byteSink = Objects.requireNonNull(byteSink);
        this.converter = buildConverter(Objects.requireNonNull(byteSink.byteOrder()));
    }

    /**
     * The byte order of the input is assumed to be big-endian.
     * @param numBits
     * @param bits
     * @throws IOException
     */
    public void writeBits(final int numBits, final long bits) throws IOException {
        // Can only represent discrete sizes of up to 63 without losing information due to the sign bit
        // If you need 64 bits you may as well just read a long using a different stream implementation.
        if (numBits > 63 || numBits < 1) {
            throw new IllegalArgumentException("Invalid number of bits to be written!");
        }
        // Discard extra bits so that extra bits not declared do not pollute our buffer.
        final var bitShift = Long.SIZE - numBits;
        final var cleanedInput = (bits << (bitShift)) >>> bitShift;

        // Convert cleaned input to the correct endianness
        final var convertedBits = converter.convert(cleanedInput);

        // Compute any extra bits that will not fit in the buffer
        final var extraBits = Long.SIZE - bitsInBuffer;

        if (extraBits < numBits) {
            // We do not have enough space in our buffer, so we must fill + flush the buffer, then write the remaining bits.
            final var partialWrite = convertedBits << (Long.SIZE - extraBits);
            final var bufferToWrite = partialWrite | this.buffer;

            byteSink.write(toBytes(bufferToWrite));

            // Write the remaining bits to the bit buffer
            this.buffer |= convertedBits >>> extraBits;
            this.bitsInBuffer = extraBits;
        }
        // All bits will fit in the buffer
        this.buffer |= convertedBits << (Long.SIZE - bitsInBuffer - numBits);
        bitsInBuffer += numBits;
    }

    public void flush() throws IOException {
        if (bitsInBuffer > 0) {
            final var numBytes = bitsInBuffer / 8;
            final var byteArray = toBytes(buffer);
            if (numBytes > 6) {
                byteSink.write(byteArray);
                return;
            }
            byteSink.write(Arrays.copyOfRange(byteArray, 8 - numBytes - 1,8));
        }
    }

    @Override
    public void close() throws Exception {
        flush();
    }
}
