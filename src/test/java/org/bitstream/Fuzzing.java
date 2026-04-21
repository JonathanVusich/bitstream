package org.bitstream;

import org.junit.jupiter.api.RepeatedTest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Random;
import java.util.random.RandomGenerator;

import static org.assertj.core.api.Assertions.assertThat;

class Fuzzing {

    private static final long SEED = RandomGenerator.getDefault().nextLong();
    private static final RandomGenerator GEN = new Random(SEED);

    static {
        System.out.println("Fuzz seed: " + SEED);
    }

    @RepeatedTest(value = 100)
    void fuzz() throws IOException {
        record Bits(long value, int numBits) {}

        final var numBytes = GEN.nextInt(0, 1_00_000);

        final var randomBytes = TestUtils.randomBytes(numBytes);

        final var inputStream = new ByteArrayInputStream(randomBytes);
        final var bitInputStream = new BitInputStream(inputStream, ByteOrder.BIG_ENDIAN);

        int numBits = numBytes * Byte.SIZE;

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

        final var byteOutput = new ByteArrayOutputStream(numBytes);
        final var bitOutputStream = new BitOutputStream(byteOutput, ByteOrder.BIG_ENDIAN);
        for (final var bits : bitsRead.reversed()) {
            bitOutputStream.writeBits(bits.numBits, bits.value);
        }

        final var resultBytes = byteOutput.toByteArray();

        assertThat(resultBytes).isEqualTo(randomBytes);
    }
}
