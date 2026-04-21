package org.bitstream;

import org.apache.commons.compress.utils.BitInputStream;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.random.RandomGenerator;

@State(Scope.Benchmark)
@Fork(value = 1)
public class ApacheStreamBenchmark {

    public static int BYTE_LEN = 50_000;

    public BitInputStream kiloStream;

    @Setup(Level.Trial)
    public void setUp() {
        final var bytes = new byte[BYTE_LEN];
        RandomGenerator.getDefault().nextBytes(bytes);
        kiloStream = new BitInputStream(new ByteArrayInputStream(bytes), ByteOrder.BIG_ENDIAN);
    }

    public static void main(String[] args) throws Exception {
        Main.main(args);
    }

    @Benchmark
    public void readSingleBit(Blackhole blackhole) throws IOException {
        blackhole.consume(kiloStream.readBit());
    }

    @Benchmark
    public void read63Bits(Blackhole blackhole) throws IOException {
        blackhole.consume(kiloStream.readBits(63));
    }
}
