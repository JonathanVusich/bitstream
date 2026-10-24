package dev.javax.bitstream.adapter;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InputStreamAdapterTest {

    @Test
    void closeUnderlyingStream() throws InterruptedException {

        final var latch = new CountDownLatch(1);
        final var stream = new InputStream() {

            @Override
            public int read() {
                return 0;
            }

            @Override
            public void close() {
                latch.countDown();
            }
        };

        try (final var ignored = new InputStreamAdapter(stream)) {

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertThat(latch.await(1, TimeUnit.MICROSECONDS));
    }

    @Test
    void readFillsTheBufferWhenTheStreamReturnsOneByteAtATime() throws IOException {
        final var stream = oneByteAtATime(new byte[]{1, 2, 3, 4});
        final var buffer = new byte[4];

        new InputStreamAdapter(stream).read(buffer, 4);

        assertThat(buffer).containsExactly(1, 2, 3, 4);
    }

    @Test
    void readThrowsWhenTheStreamEndsBeforeTheBufferIsFull() {
        final var stream = oneByteAtATime(new byte[]{1, 2, 3});
        final var adapter = new InputStreamAdapter(stream);

        assertThatThrownBy(() -> adapter.read(new byte[4], 4)).isInstanceOf(EOFException.class);
    }

    // Wraps rather than extends ByteArrayInputStream, whose own readNBytes would make just one capped read.
    private static InputStream oneByteAtATime(final byte[] bytes) {
        final var source = new ByteArrayInputStream(bytes);
        return new InputStream() {

            @Override
            public int read() {
                return source.read();
            }

            @Override
            public int read(final byte[] buffer, final int offset, final int length) {
                return source.read(buffer, offset, Math.min(length, 1));
            }
        };
    }
}