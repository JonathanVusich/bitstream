package org.bitstream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BitOutputStreamTest {

    @Nested
    class Sanity {

        @Test
        void constructorValidation() {
            record NoByteOrder() implements ByteSink {
                public ByteOrder byteOrder() {
                    return null;
                }

                @Override
                public void write(final byte[] bytes) throws IOException {

                }

                @Override
                public void close() throws Exception {

                }
            }

            assertThatThrownBy(() -> BitOutputStream.wrap(null, ByteOrder.BIG_ENDIAN)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> BitOutputStream.wrap(new ByteArrayOutputStream(1), null)).isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> BitOutputStream.wrap(new NoByteOrder())).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> BitOutputStream.wrap(null)).isInstanceOf(NullPointerException.class);
        }


        @Test
        void readBitsValidation() {
            final var bitOutputStream = BitOutputStream.wrap(new ByteArrayOutputStream(), ByteOrder.BIG_ENDIAN);

            assertThatThrownBy(() -> bitOutputStream.writeBits(0, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid number of bits to be written!");
            assertThatThrownBy(() -> bitOutputStream.writeBits(0, 64))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid number of bits to be written!");
            assertThatThrownBy(() -> bitOutputStream.writeBits(0, 65))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid number of bits to be written!");
            assertThatThrownBy(() -> bitOutputStream.writeBits(0, -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid number of bits to be written!");
        }
    }

    @Nested
    class BigEndian {

        @Test
        void flush() throws IOException {
            final var outputStream = new ByteArrayOutputStream();
            final var bitOutputStream = BitOutputStream.wrap(outputStream, ByteOrder.BIG_ENDIAN);
            bitOutputStream.writeBits(1L, 1);
            bitOutputStream.flush();

            final var bytes = outputStream.toByteArray();
            assertThat(bytes).hasSize(1);
            assertThat(bytes[0]).isEqualTo((byte) 1);
        }
    }

    @Nested
    class LittleEndian {

        @Test
        void writeBits() throws IOException {
            final var outputStream = new ByteArrayOutputStream();
            final var bitOutputStream = BitOutputStream.wrap(outputStream, ByteOrder.LITTLE_ENDIAN);
            bitOutputStream.writeBits(0b10011001, 8);
            bitOutputStream.writeBits(0b01100110, 8);

            bitOutputStream.flush();

            assertThat(outputStream.toByteArray()).isEqualTo(new byte[] { 0b01100110, (byte) 0b10011001});

        }

        @Test
        void flush() throws IOException {
            final var outputStream = new ByteArrayOutputStream();
            final var bitOutputStream = BitOutputStream.wrap(outputStream, ByteOrder.LITTLE_ENDIAN);
            bitOutputStream.writeBits(1L, 1);
            bitOutputStream.flush();

            final var bytes = outputStream.toByteArray();
            assertThat(bytes).hasSize(1);
            assertThat(bytes[0]).isEqualTo((byte) 1);
        }
    }
}