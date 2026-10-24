package dev.javax.bitstream;

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

            assertThatThrownBy(() -> new BigEndianBitInputStream(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        void readBitsValidation() {
            final var bitInputStream = TestUtils.randomStream(ByteOrder.BIG_ENDIAN);

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
        void peekBitsValidation() {
            final var bitInputStream = TestUtils.randomStream(ByteOrder.BIG_ENDIAN);

            assertThatThrownBy(() -> bitInputStream.peekBits(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid number of bits requested!");
            assertThatThrownBy(() -> bitInputStream.peekBits(58))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid number of bits requested!");
        }

        @Test
        void ranOutOfBytes() {
            final var bitInputStream = BitInputStream.wrap(new ByteArrayInputStream(new byte[]{}), ByteOrder.BIG_ENDIAN);

            assertThatThrownBy(() -> bitInputStream.readBits(1))
                    .isInstanceOf(EOFException.class)
                    .hasMessage("Not enough bytes available!");
        }
    }

    @Nested
    class BigEndian {

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
            final var bitInputStream = TestUtils.randomStream(ByteOrder.BIG_ENDIAN);

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
            final var bitInputStream = BitInputStream.wrap(byteStream, ByteOrder.BIG_ENDIAN);

            final var startingBits = bitInputStream.readBits(6);
            final var bitsAcrossBoundary = bitInputStream.readBits(4);
            final var remainderBits = bitInputStream.readBits(6);

            assertThat(startingBits).isEqualTo(1);
            assertThat(bitsAcrossBoundary).isEqualTo(0);
            assertThat(remainderBits).isEqualTo(2);
        }

        @Test
        void alignToByte() throws IOException {
            final var bytes = new byte[] { 0b1111111, 0 };

            final InputStream byteStream = new ByteArrayInputStream(bytes);
            final var bitInputStream = BitInputStream.wrap(byteStream, ByteOrder.BIG_ENDIAN);

            final var firstBits = bitInputStream.readBits(6);
            assertThat(firstBits).isEqualTo(31);
            bitInputStream.alignToByte();

            final var lastBits = bitInputStream.readBits(8);
            assertThat(lastBits).isEqualTo(0);
            bitInputStream.alignToByte();

            assertThatThrownBy(() -> bitInputStream.readBits(1)).isInstanceOf(EOFException.class);
        }

        @Test
        void countingConsumesTheRunButNotTheBitEndingIt() throws IOException {
            // 0001 01 10 then 1010 0101
            final var bitInputStream = TestUtils.wrap(new byte[] { 0b00010110, (byte) 0b10100101 }, ByteOrder.BIG_ENDIAN);

            assertThat(bitInputStream.countOnes()).isEqualTo(0);
            assertThat(bitInputStream.countZeros()).isEqualTo(3);
            assertThat(bitInputStream.readBits(1)).isEqualTo(1);
            assertThat(bitInputStream.countZeros()).isEqualTo(1);
            assertThat(bitInputStream.readBits(1)).isEqualTo(1);
            assertThat(bitInputStream.countOnes()).isEqualTo(1);
            assertThat(bitInputStream.readBits(1)).isEqualTo(0);
            assertThat(bitInputStream.readBits(8)).isEqualTo(0b10100101);
        }

        @Test
        void countZerosLongerThanTheBuffer() throws IOException {
            // One unread bit, then 56 and 7 more 0 bits: 64 in all, then a 1
            final var bytes = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0b00000001 };
            final var bitInputStream = TestUtils.wrap(bytes, ByteOrder.BIG_ENDIAN);

            bitInputStream.readBits(7);

            assertThat(bitInputStream.countZeros()).isEqualTo(64);
            assertThat(bitInputStream.readBits(1)).isEqualTo(1);
        }

        @Test
        void countOnesLongerThanTheBuffer() throws IOException {
            final var bytes = new byte[] { -1, -1, -1, -1, -1, -1, -1, -1, (byte) 0b11111110 };
            final var bitInputStream = TestUtils.wrap(bytes, ByteOrder.BIG_ENDIAN);

            bitInputStream.readBits(7);

            assertThat(bitInputStream.countOnes()).isEqualTo(64);
            assertThat(bitInputStream.readBits(1)).isEqualTo(0);
        }

        @Test
        void countingTakesOnlyTheBytesItNeeds() throws IOException {
            final var byteStream = new ByteArrayInputStream(new byte[] { 0b00000001, 0b01010101 });
            final var bitInputStream = BitInputStream.wrap(byteStream, ByteOrder.BIG_ENDIAN);

            assertThat(bitInputStream.countZeros()).isEqualTo(7);
            assertThat(byteStream.available()).isEqualTo(1);
        }

        @Test
        void countingRunningToTheEndOfTheInput() throws IOException {
            final var bitInputStream = TestUtils.wrap(new byte[] { 0, 0 }, ByteOrder.BIG_ENDIAN);

            bitInputStream.readBits(3);

            assertThat(bitInputStream.countZeros()).isEqualTo(13);
            assertThatThrownBy(() -> bitInputStream.readBits(1)).isInstanceOf(EOFException.class);
        }

        @Test
        void peekBitsDoesNotConsume() throws IOException {
            final var bytes = new byte[] { (byte) 0b10110011, 0b01010101, (byte) 0b11110000 };
            final var bitInputStream = TestUtils.wrap(bytes, ByteOrder.BIG_ENDIAN);

            assertThat(bitInputStream.peekBits(4)).isEqualTo(0b1011);
            assertThat(bitInputStream.peekBits(4)).isEqualTo(0b1011);
            bitInputStream.readBits(3);
            assertThat(bitInputStream.readBits(5)).isEqualTo(0b10011);
            assertThat(bitInputStream.peekBits(12)).isEqualTo(0b010101011111);
            assertThat(bitInputStream.readBits(16)).isEqualTo(0b0101010111110000);
        }

        @Test
        void peekBitsAfterAPartialRead() throws IOException {
            final var bytes = new byte[] { (byte) 0b10110011, 0b01010101, (byte) 0b11110000 };
            final var bitInputStream = TestUtils.wrap(bytes, ByteOrder.BIG_ENDIAN);

            bitInputStream.readBits(5);

            assertThat(bitInputStream.peekBits(10)).isEqualTo(0b0110101010);
            assertThat(bitInputStream.readBits(19)).isEqualTo(0b0110101010111110000);
        }

        @Test
        void peekBitsMatchesReadBitsAtEveryLength() throws IOException {
            final var bitInputStream = TestUtils.wrap(TestUtils.randomBytes(256), ByteOrder.BIG_ENDIAN);

            for (int numBits = 1; numBits <= 57; numBits++) {
                final var peekedBits = bitInputStream.peekBits(numBits);
                assertThat(bitInputStream.readBits(numBits)).isEqualTo(peekedBits);
            }
        }

        @Test
        void peekBitsTakesOnlyTheBytesItNeeds() throws IOException {
            final var byteStream = new ByteArrayInputStream(new byte[] { 1, 2, 3 });
            final var bitInputStream = BitInputStream.wrap(byteStream, ByteOrder.BIG_ENDIAN);

            bitInputStream.peekBits(9);

            assertThat(byteStream.available()).isEqualTo(1);
        }

        @Test
        void peekBitsAlreadyBufferedAtTheEndOfTheStream() throws IOException {
            final var bitInputStream = TestUtils.wrap(new byte[] { (byte) 0xAB }, ByteOrder.BIG_ENDIAN);

            assertThat(bitInputStream.readBits(4)).isEqualTo(0xA);
            assertThat(bitInputStream.peekBits(4)).isEqualTo(0xB);
            assertThat(bitInputStream.readBits(4)).isEqualTo(0xB);
        }

        @Test
        void peekBitsIgnoresBytesReadEarlier() throws IOException {
            final var bytes = new byte[] { -1, -1, -1, -1, -1, -1, -1, -1, 0, 0 };
            final var bitInputStream = TestUtils.wrap(bytes, ByteOrder.BIG_ENDIAN);

            bitInputStream.readBits(63);
            bitInputStream.readBits(1);

            assertThat(bitInputStream.peekBits(1)).isEqualTo(0);
            assertThat(bitInputStream.readBits(16)).isEqualTo(0);
        }

        @Test
        void alignToByteAfterPeekBits() throws IOException {
            final var bitInputStream = TestUtils.wrap(new byte[] { -1, 0x0F }, ByteOrder.BIG_ENDIAN);

            bitInputStream.readBits(3);
            bitInputStream.peekBits(10);
            bitInputStream.alignToByte();

            assertThat(bitInputStream.readBits(8)).isEqualTo(0x0F);
        }
    }

    @Nested
    class LittleEndian {

        @Test
        void readBitsValidation() {
            final var bitInputStream = TestUtils.randomStream(ByteOrder.LITTLE_ENDIAN);

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
            final var bitInputStream = TestUtils.randomStream(ByteOrder.BIG_ENDIAN);

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

            final var bitInputStream = BitInputStream.wrap(byteStream, ByteOrder.BIG_ENDIAN);

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

            final var bitInputStream = BitInputStream.wrap(byteStream, ByteOrder.LITTLE_ENDIAN);

            final var startingBits = bitInputStream.readBits(6);
            final var bitsAcrossBoundary = bitInputStream.readBits(4);
            final var remainderBits = bitInputStream.readBits(6);

            assertThat(startingBits).isEqualTo(4);
            assertThat(bitsAcrossBoundary).isEqualTo(8);
            assertThat(remainderBits).isEqualTo(0);
        }

        @Test
        void alignToByte() throws IOException {
            final var bytes = new byte[] { 0b1111111, 0 };

            final InputStream byteStream = new ByteArrayInputStream(bytes);
            final var bitInputStream = BitInputStream.wrap(byteStream, ByteOrder.LITTLE_ENDIAN);

            final var firstBits = bitInputStream.readBits(6);
            assertThat(firstBits).isEqualTo(63);
            bitInputStream.alignToByte();

            final var lastBits = bitInputStream.readBits(8);
            assertThat(lastBits).isEqualTo(0);
            bitInputStream.alignToByte();

            assertThatThrownBy(() -> bitInputStream.readBits(1)).isInstanceOf(EOFException.class);
        }

        @Test
        void countingConsumesTheRunButNotTheBitEndingIt() throws IOException {
            // Least significant bit first: 0001 01 10 then 1010 0101
            final var bitInputStream = TestUtils.wrap(new byte[] { 0b01101000, (byte) 0b10100101 }, ByteOrder.LITTLE_ENDIAN);

            assertThat(bitInputStream.countOnes()).isEqualTo(0);
            assertThat(bitInputStream.countZeros()).isEqualTo(3);
            assertThat(bitInputStream.readBits(1)).isEqualTo(1);
            assertThat(bitInputStream.countZeros()).isEqualTo(1);
            assertThat(bitInputStream.readBits(1)).isEqualTo(1);
            assertThat(bitInputStream.countOnes()).isEqualTo(1);
            assertThat(bitInputStream.readBits(1)).isEqualTo(0);
            assertThat(bitInputStream.readBits(8)).isEqualTo(0b10100101);
        }

        @Test
        void countZerosLongerThanTheBuffer() throws IOException {
            // One unread bit, then 56 and 7 more 0 bits: 64 in all, then a 1
            final var bytes = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0b10000000 };
            final var bitInputStream = TestUtils.wrap(bytes, ByteOrder.LITTLE_ENDIAN);

            bitInputStream.readBits(7);

            assertThat(bitInputStream.countZeros()).isEqualTo(64);
            assertThat(bitInputStream.readBits(1)).isEqualTo(1);
        }

        @Test
        void countOnesLongerThanTheBuffer() throws IOException {
            final var bytes = new byte[] { -1, -1, -1, -1, -1, -1, -1, -1, 0b01111111 };
            final var bitInputStream = TestUtils.wrap(bytes, ByteOrder.LITTLE_ENDIAN);

            bitInputStream.readBits(7);

            assertThat(bitInputStream.countOnes()).isEqualTo(64);
            assertThat(bitInputStream.readBits(1)).isEqualTo(0);
        }

        @Test
        void countingTakesOnlyTheBytesItNeeds() throws IOException {
            final var byteStream = new ByteArrayInputStream(new byte[] { (byte) 0b10000000, 0b01010101 });
            final var bitInputStream = BitInputStream.wrap(byteStream, ByteOrder.LITTLE_ENDIAN);

            assertThat(bitInputStream.countZeros()).isEqualTo(7);
            assertThat(byteStream.available()).isEqualTo(1);
        }

        @Test
        void countingRunningToTheEndOfTheInput() throws IOException {
            final var bitInputStream = TestUtils.wrap(new byte[] { -1, -1 }, ByteOrder.LITTLE_ENDIAN);

            bitInputStream.readBits(3);

            assertThat(bitInputStream.countOnes()).isEqualTo(13);
            assertThatThrownBy(() -> bitInputStream.readBits(1)).isInstanceOf(EOFException.class);
        }

        @Test
        void peekBitsDoesNotConsume() throws IOException {
            // Least significant bit first: 1011 0011 | 0101 0101 | 1111 0000 in reading order
            final var bytes = new byte[] { (byte) 0b11001101, (byte) 0b10101010, 0b00001111 };
            final var bitInputStream = TestUtils.wrap(bytes, ByteOrder.LITTLE_ENDIAN);

            assertThat(bitInputStream.peekBits(4)).isEqualTo(0b1101);
            assertThat(bitInputStream.peekBits(4)).isEqualTo(0b1101);
            bitInputStream.readBits(3);
            assertThat(bitInputStream.readBits(5)).isEqualTo(0b11001);
            assertThat(bitInputStream.peekBits(12)).isEqualTo(0b111110101010);
            assertThat(bitInputStream.readBits(16)).isEqualTo(0b0000111110101010);
        }

        @Test
        void peekBitsAfterAPartialRead() throws IOException {
            final var bytes = new byte[] { (byte) 0b11001101, (byte) 0b10101010, 0b00001111 };
            final var bitInputStream = TestUtils.wrap(bytes, ByteOrder.LITTLE_ENDIAN);

            bitInputStream.readBits(5);

            assertThat(bitInputStream.peekBits(10)).isEqualTo(0b0101010110);
            assertThat(bitInputStream.readBits(19)).isEqualTo(0b0000111110101010110);
        }

        @Test
        void peekBitsMatchesReadBitsAtEveryLength() throws IOException {
            final var bitInputStream = TestUtils.wrap(TestUtils.randomBytes(256), ByteOrder.LITTLE_ENDIAN);

            for (int numBits = 1; numBits <= 57; numBits++) {
                final var peekedBits = bitInputStream.peekBits(numBits);
                assertThat(bitInputStream.readBits(numBits)).isEqualTo(peekedBits);
            }
        }

        @Test
        void peekBitsTakesOnlyTheBytesItNeeds() throws IOException {
            final var byteStream = new ByteArrayInputStream(new byte[] { 1, 2, 3 });
            final var bitInputStream = BitInputStream.wrap(byteStream, ByteOrder.LITTLE_ENDIAN);

            bitInputStream.peekBits(9);

            assertThat(byteStream.available()).isEqualTo(1);
        }

        @Test
        void peekBitsAlreadyBufferedAtTheEndOfTheStream() throws IOException {
            final var bitInputStream = TestUtils.wrap(new byte[] { (byte) 0xAB }, ByteOrder.LITTLE_ENDIAN);

            assertThat(bitInputStream.readBits(4)).isEqualTo(0xB);
            assertThat(bitInputStream.peekBits(4)).isEqualTo(0xA);
            assertThat(bitInputStream.readBits(4)).isEqualTo(0xA);
        }

        @Test
        void peekBitsIgnoresBytesReadEarlier() throws IOException {
            final var bytes = new byte[] { -1, -1, -1, -1, -1, -1, -1, -1, 0, 0 };
            final var bitInputStream = TestUtils.wrap(bytes, ByteOrder.LITTLE_ENDIAN);

            bitInputStream.readBits(63);
            bitInputStream.readBits(1);

            assertThat(bitInputStream.peekBits(1)).isEqualTo(0);
            assertThat(bitInputStream.readBits(16)).isEqualTo(0);
        }

        @Test
        void alignToByteAfterPeekBits() throws IOException {
            final var bitInputStream = TestUtils.wrap(new byte[] { -1, 0x0F }, ByteOrder.LITTLE_ENDIAN);

            bitInputStream.readBits(3);
            bitInputStream.peekBits(10);
            bitInputStream.alignToByte();

            assertThat(bitInputStream.readBits(8)).isEqualTo(0x0F);
        }
    }
}