package dev.javax.bitstream;

import dev.javax.bitstream.adapter.InputStreamAdapter;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

/**
 * Reads bits from an underlying byte source.
 * Useful for low-level decoders that need to read bit patterns.
 * @author Jonathan Vusich
 */
public interface BitInputStream {

    /**
     * Returns a {@link BitInputStream} that will read bits from the {@link InputStream} in the desired byte order.
     * @param inputStream the stream that will be read from.
     * @param byteOrder the byte order of the underlying stream.
     * @return the bit input stream
     */
    static BitInputStream wrap(final InputStream inputStream, final ByteOrder byteOrder) {
        return wrap(new InputStreamAdapter(inputStream), byteOrder);
    }

    /**
     * Returns a {@link BitInputStream} that will read bits from the {@link ByteSource} in the desired byte order.
     * @param byteSource the byte source that will be read from.
     * @param byteOrder the byte order of the underlying source.
     * @return the bit input stream
     */
    static BitInputStream wrap(final ByteSource byteSource, final ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            return new LittleEndianBitInputStream(byteSource);
        }
        return new BigEndianBitInputStream(byteSource);
    }

    /**
     * Returns a long value with the specified number of bits read from the underlying byte source.
     *
     * @param numBits the number of bits that should be read in the range (1, 64) exclusive.
     * @return The bit value read represented by a long.
     * @throws EOFException if the underlying source does not have enough bytes to fulfill the request.
     * @throws IOException if the underlying source throws an error during the read.
     */
    long readBits(final int numBits) throws IOException;

    /**
     * Discards bits until the next byte boundary.
     */
    void alignToByte();
}
