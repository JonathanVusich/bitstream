package dev.javax.bitstream;

import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.random.RandomGenerator;

@State(Scope.Benchmark)
public class BitInputStreamBenchmark {

    public static int BYTE_LEN = 50_000;

    public BitInputStream bigEndianStream;
    public BitInputStream littleEndianStream;

    @Setup(Level.Trial)
    public void setUp() {
        final var bytes = new byte[BYTE_LEN];
        RandomGenerator.getDefault().nextBytes(bytes);
        bigEndianStream = BitInputStream.wrap(new RotatingStream(), ByteOrder.BIG_ENDIAN);
        littleEndianStream = BitInputStream.wrap(new RotatingStream(), ByteOrder.LITTLE_ENDIAN);
    }

    public static void main(String[] args) throws Exception {
        Main.main(args);
    }

    @Benchmark
    public void readSingleBitBe(Blackhole blackhole) throws IOException {
        blackhole.consume(bigEndianStream.readBits(1));
    }

    @Benchmark
    public void read63BitsBe(Blackhole blackhole) throws IOException {
        blackhole.consume(bigEndianStream.readBits(63));
    }

    @Benchmark
    public void readSingleBitLe(Blackhole blackhole) throws IOException {
        blackhole.consume(littleEndianStream.readBits(1));
    }

    @Benchmark
    public void read63BitsLe(Blackhole blackhole) throws IOException {
        blackhole.consume(littleEndianStream.readBits(63));
    }
}
