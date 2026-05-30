package dev.javax.bitstream;

import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.random.RandomGenerator;

@State(Scope.Benchmark)
public class BitOutputStreamBenchmark {


    public static int BYTE_LEN = 50_000;

    public BitOutputStream bigEndianStream;
    public BitOutputStream littleEndianStream;

    @Setup(Level.Trial)
    public void setUp() {
        final var bytes = new byte[BYTE_LEN];
        RandomGenerator.getDefault().nextBytes(bytes);
        bigEndianStream = BitOutputStream.wrap(new BlackholeStream(), ByteOrder.BIG_ENDIAN);
        littleEndianStream = BitOutputStream.wrap(new BlackholeStream(), ByteOrder.LITTLE_ENDIAN);
    }

    public static void main(String[] args) throws Exception {
        Main.main(args);
    }

    @Benchmark
    public void writeSingleBitBe() throws IOException {
        bigEndianStream.writeBits(0b0001L, 1);
    }

    @Benchmark
    public void write63BitsBe() throws IOException {
        bigEndianStream.writeBits(0b0001L, 63);
    }

    @Benchmark
    public void writeSingleBitLe() throws IOException {
        littleEndianStream.writeBits(0b0001L, 1);
    }

    @Benchmark
    public void write63BitsLe() throws IOException {
        littleEndianStream.writeBits(0b0001L, 63);
    }
}
