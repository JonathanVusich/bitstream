package org.bitstream;

import org.apache.commons.compress.changes.ChangeSet;
import org.junit.jupiter.api.RepeatedTest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class Fuzzing {

    public record Bits(long value, int numBits) {
        @Override
        public String toString() {
            return "Bits[value=%s, numBits=%d]".formatted(TestUtils.bitString(value, numBits), numBits);
        }
    }

    @RepeatedTest(value = 100)
    void beFuzz() throws IOException {

        final var randomBytes = TestUtils.randomBytes();

        final var inputStream = new ByteArrayInputStream(randomBytes);
        final var bitInputStream = BitInputStream.wrap(inputStream, ByteOrder.BIG_ENDIAN);

        int numBits = randomBytes.length * Byte.SIZE;

        final var bitsRead = new ArrayList<Bits>();

        while (numBits > 0) {
            var bitLength = TestUtils.randomValidBitLength();
            if (bitLength > numBits) {
                bitLength = numBits;
            }

            final long longVal = bitInputStream.readBits(bitLength);
            bitsRead.add(new Bits(longVal, bitLength));
            numBits -= bitLength;
        }

        final var byteOutput = new ByteArrayOutputStream(randomBytes.length);
        final var bitOutputStream = BitOutputStream.wrap(byteOutput, ByteOrder.BIG_ENDIAN);
        for (final var bits : bitsRead) {
            bitOutputStream.writeBits(bits.value, bits.numBits);
        }
        bitOutputStream.flush();

        final var resultBytes = byteOutput.toByteArray();

        assertThat(resultBytes).isEqualTo(randomBytes);
    }

    @RepeatedTest(value = 100)
    void leFuzz() throws IOException {

        final var randomBytes = TestUtils.randomBytes();

        final var inputStream = new ByteArrayInputStream(randomBytes);
        final var bitInputStream = BitInputStream.wrap(inputStream, ByteOrder.LITTLE_ENDIAN);

        int numBits = randomBytes.length * Byte.SIZE;

        final var bitsRead = new ArrayList<Bits>();

        while (numBits > 0) {
            var bitLength = TestUtils.randomValidBitLength();
            if (bitLength > numBits) {
                bitLength = numBits;
            }

            final long longVal = bitInputStream.readBits(bitLength);
            bitsRead.add(new Bits(longVal, bitLength));
            numBits -= bitLength;
        }

        final var byteOutput = new ByteArrayOutputStream(randomBytes.length);
        final var bitOutputStream = BitOutputStream.wrap(byteOutput, ByteOrder.LITTLE_ENDIAN);
        for (final var bits : bitsRead) {
            bitOutputStream.writeBits(bits.value, bits.numBits);
        }
        bitOutputStream.flush();

        final var resultBytes = byteOutput.toByteArray();

        assertThat(resultBytes).isEqualTo(randomBytes);
    }

    @RepeatedTest(value = 100)
    void leReadVsApacheRead() throws IOException {

        final var randomBytes = TestUtils.randomBytes();

        final var apacheStream = new org.apache.commons.compress.utils.BitInputStream(new ByteArrayInputStream(randomBytes), ByteOrder.LITTLE_ENDIAN);
        final var leStream = BitInputStream.wrap(new ByteArrayInputStream(randomBytes), ByteOrder.LITTLE_ENDIAN);

        int numBits = randomBytes.length * Byte.SIZE;

        while (numBits > 0) {
            var bitLength = TestUtils.randomValidBitLength();
            if (bitLength > numBits) {
                bitLength = numBits;
            }

            final long apacheVal = apacheStream.readBits(bitLength);
            final long leVal = leStream.readBits(bitLength);

            assertThat(leVal).isEqualTo(apacheVal);
            numBits -= bitLength;
        }
    }

    @RepeatedTest(value = 100)
    void beReadVsApacheRead() throws IOException {

        final var randomBytes = TestUtils.randomBytes();

        final var apacheStream = new org.apache.commons.compress.utils.BitInputStream(new ByteArrayInputStream(randomBytes), ByteOrder.BIG_ENDIAN);
        final var beStream = BitInputStream.wrap(new ByteArrayInputStream(randomBytes), ByteOrder.BIG_ENDIAN);

        int numBits = randomBytes.length * Byte.SIZE;

        final var bitsRead = new ArrayList<Bits>();
        while (numBits > 0) {
            var bitLength = TestUtils.randomValidBitLength();
            if (bitLength > numBits) {
                bitLength = numBits;
            }

            final long apacheVal = apacheStream.readBits(bitLength);
            final long beVal = beStream.readBits(bitLength);

            bitsRead.add(new Bits(beVal, bitLength));

            assertThat(beVal).isEqualTo(apacheVal);
            numBits -= bitLength;
        }
    }
}
