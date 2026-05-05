package dev.javax.bitstream;

import java.io.ByteArrayInputStream;
import java.nio.ByteOrder;
import java.util.Random;
import java.util.random.RandomGenerator;

public final class TestUtils {

    private static final long SEED = RandomGenerator.getDefault().nextLong();

    private static final RandomGenerator GENERATOR = new Random(SEED);

    static {
        System.out.println("Fuzz seed: " + SEED);
    }

    public static byte[] randomBytes(int length) {
        final var bytes = new byte[length];
        GENERATOR.nextBytes(bytes);
        return bytes;
    }

    public static byte[] randomBytes() {
        final var length = GENERATOR.nextInt(5000);
        return randomBytes(length);
    }

    public static BitInputStream wrap(byte[] bytes, ByteOrder byteOrder) {
        return BitInputStream.wrap(new ByteArrayInputStream(bytes), byteOrder);
    }

    public static BitInputStream randomStream(ByteOrder byteOrder) {
        return BitInputStream.wrap(new ByteArrayInputStream(randomBytes()), byteOrder);
    }

    public static int randomValidBitLength() {
        return GENERATOR.nextInt(1, 64);
    }

    public static String bitString(long bits, int numBits) {
        if (numBits < 1 || numBits > 63) {
            throw new IllegalArgumentException("Invalid number of bits!");
        }
        final var bitString = Long.toBinaryString(bits);
        final var numZeros = numBits - bitString.length();

        if (numZeros < 0) {
            // Little endian requires us to pad left with missing zeroes up until 64 bits
            final var leftZeros = 64 - bitString.length();
            // Then we need to add out to the right with any leftover space
            final var result = "0".repeat(leftZeros) + bitString.substring(0, bitString.length() + numZeros - leftZeros);
            if (result.length() != numBits) {
                throw new IllegalStateException("Invalid length computed!");
            }
            return result;
        } else {
            // Big endian only requires us to pad extra zeroes on the left.
            final var result = "0".repeat(numZeros) + bitString;
            if (result.length() != numBits) {
                throw new IllegalStateException("Invalid length computed!");
            }
            return result;
        }
    }
}
