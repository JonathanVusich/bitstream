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

    // Each count also reads the bit that ends the run, as a Rice decoder would.
    @Benchmark
    public void countZerosBe(Blackhole blackhole) throws IOException {
        blackhole.consume(bigEndianStream.countZeros());
        blackhole.consume(bigEndianStream.readBits(1));
    }

    @Benchmark
    public void countZerosLe(Blackhole blackhole) throws IOException {
        blackhole.consume(littleEndianStream.countZeros());
        blackhole.consume(littleEndianStream.readBits(1));
    }

    @Benchmark
    public void countOnesBe(Blackhole blackhole) throws IOException {
        blackhole.consume(bigEndianStream.countOnes());
        blackhole.consume(bigEndianStream.readBits(1));
    }

    @Benchmark
    public void countOnesLe(Blackhole blackhole) throws IOException {
        blackhole.consume(littleEndianStream.countOnes());
        blackhole.consume(littleEndianStream.readBits(1));
    }

    // Each peek is followed by a read of a 9-bit code, as a Huffman decoder would read the code it looked up.
    @Benchmark
    public void peek16BitsBe(Blackhole blackhole) throws IOException {
        blackhole.consume(bigEndianStream.peekBits(16));
        blackhole.consume(bigEndianStream.readBits(9));
    }

    @Benchmark
    public void peek16BitsLe(Blackhole blackhole) throws IOException {
        blackhole.consume(littleEndianStream.peekBits(16));
        blackhole.consume(littleEndianStream.readBits(9));
    }
}
