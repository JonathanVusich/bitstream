package org.bitstream;

import org.junit.jupiter.api.RepeatedTest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Random;
import java.util.random.RandomGenerator;

import static org.assertj.core.api.Assertions.assertThat;

class Fuzzing {

    // private static final long SEED = RandomGenerator.getDefault().nextLong();
    private static final long SEED = 1215950653243394673L;
    private static final RandomGenerator GEN = new Random(SEED);

    static {
        System.out.println("Fuzz seed: " + SEED);
    }

    @RepeatedTest(value = 100)
    void fuzz() throws IOException {
        record Bits(long value, int numBits) {}

        final var numBytes = GEN.nextInt(0, 8);

        final var randomBytes = TestUtils.randomBytes(numBytes);

        final var inputStream = new ByteArrayInputStream(randomBytes);
        final var bitInputStream = new BitInputStream(inputStream, ByteOrder.BIG_ENDIAN);

        int numBits = numBytes * Byte.SIZE;

        final var bitsRead = new ArrayList<Bits>();

        while (numBits > 0) {
            var bitLength = GEN.nextInt(1, 64);
            if (bitLength > numBits) {
                bitLength = numBits;
            }

            final long longVal = bitInputStream.readBits(bitLength);
            bitsRead.add(new Bits(longVal, bitLength));
            numBits -= bitLength;
        }

        final var byteOutput = new ByteArrayOutputStream(numBytes);
        final var bitOutputStream = new BitOutputStream(byteOutput, ByteOrder.BIG_ENDIAN);
        for (final var bits : bitsRead) {
            bitOutputStream.writeBits(bits.numBits, bits.value);
        }
        bitOutputStream.flush();

        final var resultBytes = byteOutput.toByteArray();

        assertThat(resultBytes).isEqualTo(randomBytes);
    }

    @RepeatedTest(value = 10_000)
    void toBytes() {
        final var byteBuffer = ByteBuffer.allocate(8);
        final var longVal = GEN.nextLong();
        byteBuffer.putLong(longVal);
        byteBuffer.position(0);

        final var bytes = new byte[8];
        byteBuffer.get(bytes);

        assertThat(Utils.toBytes(longVal)).isEqualTo(bytes);
    }
}
