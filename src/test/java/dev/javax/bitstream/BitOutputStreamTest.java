package dev.javax.bitstream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BitOutputStreamTest {

    @Nested
    class Sanity {

        public static Stream<Arguments> byteOrders() {
            return Stream.of(Arguments.of(ByteOrder.BIG_ENDIAN), Arguments.of(ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        void constructorValidation() {
            assertThatThrownBy(() -> BitOutputStream.wrap((ByteSink) null, ByteOrder.BIG_ENDIAN)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> BitOutputStream.wrap(new ByteArrayOutputStream(1), null)).isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> BitOutputStream.wrap((ByteSink) null, null)).isInstanceOf(NullPointerException.class);
        }


        @ParameterizedTest
        @MethodSource("byteOrders")
        void readBitsValidation(ByteOrder byteOrder) {
            final var bitOutputStream = BitOutputStream.wrap(new ByteArrayOutputStream(), byteOrder);

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
        void writeBits() throws IOException {
            final var outputStream = new ByteArrayOutputStream();
            final var bitOutputStream = BitOutputStream.wrap(outputStream, ByteOrder.BIG_ENDIAN);
            bitOutputStream.writeBits(0b10011001, 8);
            bitOutputStream.writeBits(0b01100110, 8);

            bitOutputStream.writeBits(0, 48);

            // Validate that the buffer is full but not flushed
            assertThat(outputStream.toByteArray()).hasSize(0);

            bitOutputStream.flush();

            assertThat(outputStream.toByteArray()).isEqualTo(new byte[] { (byte) 0b10011001, 0b01100110, 0, 0, 0, 0, 0, 0 });

        }

        @Test
        void flush() throws IOException {
            final var timesWritten = new AtomicInteger(0);
            final var outputStream = new ByteArrayOutputStream() {
                @Override
                public void write(final byte[] bytes) throws IOException {
                    timesWritten.incrementAndGet();
                    super.write(bytes);
                }
            };

            final var bitOutputStream = BitOutputStream.wrap(outputStream, ByteOrder.BIG_ENDIAN);
            bitOutputStream.writeBits(1L, 1);

            bitOutputStream.flush();

            final var bytes = outputStream.toByteArray();
            assertThat(bytes).hasSize(1);
            assertThat(bytes[0]).isEqualTo((byte) -128);
            assertThat(timesWritten.get()).isEqualTo(1);

            bitOutputStream.flush();

            assertThat(timesWritten.get()).isEqualTo(1);
        }

        @Test
        void readBitsValidation() {
            final var bitOutputStream = BitOutputStream.wrap(new ByteArrayOutputStream(), ByteOrder.LITTLE_ENDIAN);

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

        @Test
        void close() throws Exception {
            final var outputStream = new ByteArrayOutputStream();

            final var bitOutputStream = BitOutputStream.wrap(outputStream, ByteOrder.BIG_ENDIAN);
            bitOutputStream.writeBits(1L, 1);

            bitOutputStream.close();

            final var bytes = outputStream.toByteArray();
            assertThat(bytes).hasSize(1);
            assertThat(bytes[0]).isEqualTo((byte) -128);
        }

        @Test
        void closeWithEmptyBuffer() throws Exception {
            final var writeCalls = new AtomicInteger(0);
            final var outputStream = new ByteArrayOutputStream() {
                @Override
                public void write(final byte[] bytes) throws IOException {
                    writeCalls.incrementAndGet();
                    super.write(bytes);
                }
            };

            final var bitOutputStream = BitOutputStream.wrap(outputStream, ByteOrder.BIG_ENDIAN);
            bitOutputStream.writeBits(1L, 1);

            bitOutputStream.close();

            final var bytes = outputStream.toByteArray();
            assertThat(bytes).hasSize(1);
            assertThat(bytes[0]).isEqualTo((byte) -128);

            bitOutputStream.close();

            // Did not touch the output stream on the second close
            assertThat(writeCalls.get()).isEqualTo(1);
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

            bitOutputStream.writeBits(0, 48);

            // Validate that the buffer is full but not flushed
            assertThat(outputStream.toByteArray()).hasSize(0);

            bitOutputStream.flush();

            assertThat(outputStream.toByteArray()).isEqualTo(new byte[] { (byte) 0b10011001, 0b01100110, 0, 0, 0, 0, 0, 0 });

        }

        @Test
        void flush() throws IOException {
            final var timesWritten = new AtomicInteger(0);
            final var outputStream = new ByteArrayOutputStream() {
                @Override
                public void write(final byte[] bytes) throws IOException {
                    timesWritten.incrementAndGet();
                    super.write(bytes);
                }
            };

            final var bitOutputStream = BitOutputStream.wrap(outputStream, ByteOrder.LITTLE_ENDIAN);
            bitOutputStream.writeBits(1L, 1);

            bitOutputStream.flush();

            final var bytes = outputStream.toByteArray();
            assertThat(bytes).hasSize(1);
            assertThat(bytes[0]).isEqualTo((byte) 1);
            assertThat(timesWritten.get()).isEqualTo(1);

            bitOutputStream.flush();

            assertThat(timesWritten.get()).isEqualTo(1);
        }

        @Test
        void close() throws Exception {
            final var outputStream = new ByteArrayOutputStream();

            final var bitOutputStream = BitOutputStream.wrap(outputStream, ByteOrder.LITTLE_ENDIAN);
            bitOutputStream.writeBits(1L, 1);

            bitOutputStream.close();

            final var bytes = outputStream.toByteArray();
            assertThat(bytes).hasSize(1);
            assertThat(bytes[0]).isEqualTo((byte) 1);
        }

        @Test
        void closeWithEmptyBuffer() throws Exception {
            final var writeCalls = new AtomicInteger(0);
            final var outputStream = new ByteArrayOutputStream() {
                @Override
                public void write(final byte[] bytes) throws IOException {
                    writeCalls.incrementAndGet();
                    super.write(bytes);
                }
            };

            final var bitOutputStream = BitOutputStream.wrap(outputStream, ByteOrder.LITTLE_ENDIAN);
            bitOutputStream.writeBits(1L, 1);

            bitOutputStream.close();

            final var bytes = outputStream.toByteArray();
            assertThat(bytes).hasSize(1);
            assertThat(bytes[0]).isEqualTo((byte) 1);

            bitOutputStream.close();

            // Did not touch the output stream on the second close
            assertThat(writeCalls.get()).isEqualTo(1);
        }
    }
}