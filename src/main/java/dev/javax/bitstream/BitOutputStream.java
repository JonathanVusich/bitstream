package dev.javax.bitstream;

import dev.javax.bitstream.adapter.OutputStreamAdapter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;

/**
 * Writes bits out to an underlying byte sink.
 * Useful for low-level encoders that rely on bit patterns to compactly encode data.
 * @author Jonathan Vusich
 */
public interface BitOutputStream extends AutoCloseable {

    /**
     * Returns a {@link BitOutputStream} that will write bits to the {@link OutputStream} in the desired byte order.
     * @param outputStream the output stream that will be written to.
     * @param byteOrder the byte order of the bits that will be written to the stream.
     * @return the bit output stream
     */
    static BitOutputStream wrap(OutputStream outputStream, ByteOrder byteOrder) {
        return wrap(new OutputStreamAdapter(outputStream), byteOrder);
    }

    /**
     * Returns a {@link BitOutputStream} that will write bits to the {@link ByteSink} in the desired byte order.
     * This is provided to enable other outputs other than output streams.
     * @param byteSink the byte sink that will be written to.
     * @param byteOrder the byte order of the bits that will be written to the sink.
     * @return the bit output stream
     */
    static BitOutputStream wrap(ByteSink byteSink, ByteOrder byteOrder) {
        if (byteOrder.equals(ByteOrder.LITTLE_ENDIAN)) {
            return new LittleEndianBitOutputStream(byteSink);
        }
        return new BigEndianBitOutputStream(byteSink);
    }

    /**
     * Writes the specified number of bits to the underlying byte sink.
     *
     * @param bits the raw bit value that should be written. Extra bits in this value will be discarded.
     * @param numBits the number of bits that should be read in the range (1, 64) exclusive.
     * @throws IOException if the underlying sink throws an error during the write.
     */
    void writeBits(long bits, int numBits) throws IOException;

    /**
     * Flushes any pending writes to the underlying byte sink. This must be done to ensure that
     * the underlying byte sink receives all bits.
     * This method is called when the stream is closed.
     * @throws IOException if the underlying sink throws an error during the write.
     */
    void flush() throws IOException;
}
