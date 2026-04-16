package org.bitstream;

import java.nio.ByteOrder;
import java.util.random.RandomGenerator;

public final class Utils {

    private static final RandomGenerator GENERATOR = RandomGenerator.getDefault();
    private static final String[] BYTE_REPRESENTATIONS = new String[256];
    static {
        for (int i = 0; i < 256; i++) {
            final var bitString = Integer.toBinaryString(i);
            final var numZeros = 8 - bitString.length();
            final var paddedBits = "0".repeat(numZeros) + bitString;
            BYTE_REPRESENTATIONS[i] = paddedBits;
        }
    }

    public static ByteOrder randomOrder() {
        if (GENERATOR.nextBoolean()) {
            return ByteOrder.BIG_ENDIAN;
        }
        return ByteOrder.LITTLE_ENDIAN;
    }

    public static byte[] randomBytes() {
        final var bytes = new byte[GENERATOR.nextInt(10_000)];
        GENERATOR.nextBytes(bytes);
        return bytes;
    }

    public static byte[] randomBytes(int len) {
        final var bytes = new byte[len];
        GENERATOR.nextBytes(bytes);
        return bytes;
    }

    public static int randomValidBitLength() {
        return GENERATOR.nextInt(1, 64);
    }

    public static String bitString(byte[] bytes) {
        final var sb = new StringBuilder(bytes.length * 8);
        for (final var b : bytes) {
            final var index = b & 0xFF;
            sb.append(BYTE_REPRESENTATIONS[index]);
        }
        return sb.toString();
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

    public static String reverse(String input) {
        return new StringBuilder(input).reverse().toString();
    }
}
