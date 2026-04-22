package org.bitstream;

import org.bitstream.converter.EndianAwareReader;
import org.bitstream.converter.EndianAwareWriter;

import java.nio.ByteOrder;

public final class Utils {

    public static byte[] toBytes(long val) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte)(val & 0xFF);
            val >>= 8;
        }
        return result;
    }

    public static EndianAwareWriter buildWriter(ByteOrder byteOrder) {
        // If there is a byte order difference, reverse the bits that are read.
        if (ByteOrder.BIG_ENDIAN != byteOrder) {
            return (input, numBits) -> {
                final var flippedInput = Long.reverse(input);
                final var bitShift = Long.SIZE - numBits;
                return (flippedInput >>> bitShift) << bitShift;
            };
        }
        // Else we just return as is
        return (input, numBits) -> {
            final var bitShift = Long.SIZE - numBits;
            return (input << bitShift) >>> bitShift;
        };
    }

    public static EndianAwareReader buildReader(ByteOrder byteOrder) {
        // If there is a byte order difference, reverse the bits that are read.
        if (ByteOrder.BIG_ENDIAN != byteOrder) {
            return Long::reverse;
        }
        // Else we just return as is
        return identity -> identity;
    }


    private Utils() {
        throw new IllegalStateException("Should never be instantiated!");
    }
}
