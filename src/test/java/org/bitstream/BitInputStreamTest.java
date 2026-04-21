package org.bitstream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BitInputStreamTest {

    @Nested
    class Sanity {

        @Test
        void constructorValidation() {
            record NoByteOrder() implements ByteSource {
                @Override
                public ByteOrder byteOrder() {
                    return null;
                }

                @Override
                public long read() throws IOException {
                    return 0;
                }

                @Override
                public void close() throws Exception {

                }
            }

            assertThatThrownBy(() -> new BitInputStream(null, ByteOrder.BIG_ENDIAN)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new BitInputStream(new ByteArrayInputStream(new byte[] { 1 }), null)).isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> new BitInputStream(new NoByteOrder())).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new BitInputStream(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        void readBitsValidation() {
            final var bitInputStream = TestUtils.randomStream(0, ByteOrder.BIG_ENDIAN);

            assertThatThrownBy(() -> bitInputStream.readBits(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid number of bits requested!");
            assertThatThrownBy(() -> bitInputStream.readBits(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid number of bits requested!");
            assertThatThrownBy(() -> bitInputStream.readBits(64))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid number of bits requested!");
            assertThatThrownBy(() -> bitInputStream.readBits(65))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid number of bits requested!");
        }

        @Test
        void ranOutOfBytes() {
            final var bitInputStream = new BitInputStream(new ByteArrayInputStream(new byte[] {}), ByteOrder.BIG_ENDIAN);

            assertThatThrownBy(() -> bitInputStream.readBits(1))
                    .isInstanceOf(EOFException.class)
                    .hasMessage("No more bytes available!");
        }

        @Test
        void readBits() throws IOException {
            final var bytes = TestUtils.randomBytes(10);
            final var bitInputStream = TestUtils.wrap(bytes, ByteOrder.BIG_ENDIAN);

            for (int i = 0; i < 10; i++) {
                final var val = (byte) bitInputStream.readBits(8);
                assertThat(val).isEqualTo(bytes[i]);
            }
        }

        @Test
        void read60Bits() throws IOException {
            final var bytes = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0b00011000 };
            final var bitInputStream = TestUtils.wrap(bytes, ByteOrder.BIG_ENDIAN);

            // Bits in buffer is 0, need to get it to 56
            final var bitVal = bitInputStream.readBits(60);
            assertThat(bitVal).isEqualTo(1);
            final var remainingBits = bitInputStream.readBits(4);
            assertThat(remainingBits).isEqualTo(8);

            assertThatThrownBy(() -> bitInputStream.readBits(1)).isInstanceOf(EOFException.class);
        }

        @Test
        void allValidBitCombinations() throws IOException {
            final var bitInputStream = TestUtils.randomStream(512, ByteOrder.BIG_ENDIAN);

            for (int i = 1; i < 64; i++) {
                bitInputStream.readBits(i);
            }
        }

        @Test
        void validateShiftingWhenBufferIsEmpty() throws IOException {
            final var bytes = new byte[] {0, 0, 0, 0, 0, 0, 0, 0b00011111, 0b0001111 };
            final var bitInputStream = TestUtils.wrap(bytes, ByteOrder.BIG_ENDIAN);

            final var sixtyBits = bitInputStream.readBits(60);
            final var lastBits = bitInputStream.readBits(12);

            assertThat(sixtyBits).isEqualTo(1);
            assertThat(lastBits).isEqualTo(0b111100001111);
        }

        @Test
        void validateReadingExactNumberOfBitsFromBuffer() throws IOException {
            final var bytes = new byte[] {0, 0, 0, 0, 0, 0, 0, 0b00011111, 0b0001111 };
            final var bitInputStream = TestUtils.wrap(bytes, ByteOrder.BIG_ENDIAN);

            final var sixtyBits = bitInputStream.readBits(60);
            final var nextFourBits = bitInputStream.readBits(4);
            final var lastBits = bitInputStream.readBits(8);

            assertThat(sixtyBits).isEqualTo(1);
            assertThat(nextFourBits).isEqualTo(0b1111);
            assertThat(lastBits).isEqualTo(0b0001111);
        }

        @Test
        void readBitsAcrossBoundary() throws IOException {
            final var bytes = new byte[] { 4, 2 };
            final InputStream byteStream = new ByteArrayInputStream(bytes);

            final var bitInputStream = new BitInputStream(byteStream, ByteOrder.BIG_ENDIAN);

            final var startingBits = bitInputStream.readBits(6);
            final var bitsAcrossBoundary = bitInputStream.readBits(4);
            final var remainderBits = bitInputStream.readBits(6);

            assertThat(startingBits).isEqualTo(1);
            assertThat(bitsAcrossBoundary).isEqualTo(0);
            assertThat(remainderBits).isEqualTo(2);
        }

        @Test
        void readBitsInLittleEndian() throws IOException {
            final var bytes = new byte[] { 4, 2 };
            final InputStream byteStream = new ByteArrayInputStream(bytes);

            final var bitInputStream = new BitInputStream(byteStream, ByteOrder.LITTLE_ENDIAN);

            final var startingBits = bitInputStream.readBits(6);
            final var bitsAcrossBoundary = bitInputStream.readBits(4);
            final var remainderBits = bitInputStream.readBits(6);

            assertThat(startingBits).isEqualTo(1L << 63);
            assertThat(bitsAcrossBoundary).isEqualTo(0);
            assertThat(remainderBits).isEqualTo(1L << 62);
        }
    }
}