package dev.javax.bitstream.adapter;

import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class OutputStreamAdapterTest {

    @Test
    void closeUnderlyingStream() throws InterruptedException {

        final var latch = new CountDownLatch(1);
        final var stream = new OutputStream() {

            @Override
            public void write(final int b) {

            }

            @Override
            public void close() {
                latch.countDown();
            }
        };

        try (final var ignored = new OutputStreamAdapter(stream)) {

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertThat(latch.await(1, TimeUnit.MICROSECONDS));
    }

}