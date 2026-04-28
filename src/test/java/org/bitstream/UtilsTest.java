package org.bitstream;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;

class UtilsTest {

    @Test
    void toBytes() {
        for (int i = 0; i < 8; i++) {
            long longVal = 1L << ((i * 8) + 7);

            final var byteBuffer = ByteBuffer.allocate(8);
            byteBuffer.putLong(longVal);
            byteBuffer.position(0);

            final var bytes = new byte[8];
            byteBuffer.get(bytes);

            assertThat(Utils.toBeBytes(longVal)).isEqualTo(bytes);
        }

    }
}