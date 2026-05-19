package dev.javax.bitstream.adapter;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

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

}