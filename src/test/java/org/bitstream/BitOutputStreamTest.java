package org.bitstream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BitOutputStreamTest {

    @Nested
    class Sanity {

        @Test
        void constructorValidation() {
            record NoByteOrder() implements ByteSink {
                @Override
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

            assertThatThrownBy(() -> new BitOutputStream(null, ByteOrder.BIG_ENDIAN)).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new BitOutputStream(new ByteArrayOutputStream(1), null)).isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> new BitOutputStream(new NoByteOrder())).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> new BitOutputStream(null)).isInstanceOf(NullPointerException.class);
        }
    }
}