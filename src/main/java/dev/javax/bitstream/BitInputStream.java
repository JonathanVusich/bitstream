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
     * @param numBits the number of bits that should be read, from 1 to 63 inclusive.
     * @return The bit value read represented by a long.
     * @throws IllegalArgumentException if {@code numBits} is outside 1 to 63.
     * @throws EOFException if the underlying source does not have enough bytes to fulfill the request.
     * @throws IOException if the underlying source throws an error during the read.
     */
    long readBits(final int numBits) throws IOException;

    /**
     * The most bits {@link #peekBits(int)} can look at, so they and any unread bits of the bytes taken for them
     * fit in the 64-bit buffer.
     */
    int MAX_PEEK_BITS = 57;

    /**
     * Consumes 0 bits up to the next 1 bit, as in FLAC's Rice codes, or up to the end of the input.
     * The 1 bit is left unread.
     *
     * @return the number of 0 bits consumed.
     * @throws IOException if the underlying source throws an error during the read.
     */
    int countZeros() throws IOException;

    /**
     * Consumes 1 bits up to the next 0 bit, or up to the end of the input. The 0 bit is left unread.
     *
     * @return the number of 1 bits consumed.
     * @throws IOException if the underlying source throws an error during the read.
     */
    int countOnes() throws IOException;

    /**
     * Returns the next bits without consuming them, as {@link #readBits(int)} would return them.
     * Huffman decoders peek at the longest code, look it up, then {@link #readBits(int) read} only its length.
     *
     * @param numBits the number of bits to look at, from 1 to {@link #MAX_PEEK_BITS} inclusive.
     * @return the next bits.
     * @throws IllegalArgumentException if {@code numBits} is outside 1 to {@link #MAX_PEEK_BITS}.
     * @throws EOFException if the underlying source does not have enough bytes to fulfill the request.
     * @throws IOException if the underlying source throws an error during the read.
     */
    long peekBits(final int numBits) throws IOException;

    /**
     * Discards bits until the next byte boundary.
     */
    void alignToByte();
}
