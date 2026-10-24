# bitstream

![Maven Central Version](https://img.shields.io/maven-central/v/dev.javax/bitstream)
![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/JonathanVusich/bitstream/pr.yaml)
[![javadoc](https://javadoc.io/badge2/dev.javax/bitstream/javadoc.svg)](https://javadoc.io/doc/dev.javax/bitstream)

A high-performance Java library for reading and writing individual bits to and from underlying byte streams.

Standard `java.io` libraries are designed to operate on bytes. When parsing binary formats, network protocols, or compressed data (like Huffman coding or video codecs), you frequently need to read and write at the bit level. This library bridges that gap, providing a highly optimized layer for bitwise I/O with first-class support for multiple endian formats.

## Features

* **High Performance:** Heavily optimized read/write loops utilizing internal buffering and efficient bit-shifting. Reduces the overhead of bit-by-bit extraction.
* **Multi-Endian Support:** Read and write bits in both Big-Endian and Little-Endian byte orders using standard `java.nio.ByteOrder`.
* **Bidirectional I/O:** Complete implementations wrapping standard `InputStream` and `OutputStream` instances via clean static factory methods.
* **Zero-Allocation Reads:** Designed to minimize memory allocations and garbage collection overhead during hot-path stream processing.

## Performance (Apache BitInputStream added for comparison)

| Benchmark | Mode | Cnt | Score | Error (±) | Units |
| :--- | :--- | :--- | :--- | :--- | :--- |
| ApacheStreamBenchmark.read63BitsBe | avgt | 25 | 43.712 | 0.419 | ns/op |
| ApacheStreamBenchmark.read63BitsLe | avgt | 25 | 45.663 | 2.281 | ns/op |
| ApacheStreamBenchmark.readSingleBitBe | avgt | 25 | 2.225 | 0.176 | ns/op |
| ApacheStreamBenchmark.readSingleBitLe | avgt | 25 | 1.883 | 0.114 | ns/op |
| BitInputStreamBenchmark.countOnesBe | avgt | 25 | 4.044 | 0.084 | ns/op |
| BitInputStreamBenchmark.countOnesLe | avgt | 25 | 3.842 | 0.186 | ns/op |
| BitInputStreamBenchmark.countZerosBe | avgt | 25 | 3.712 | 0.077 | ns/op |
| BitInputStreamBenchmark.countZerosLe | avgt | 25 | 3.685 | 0.104 | ns/op |
| BitInputStreamBenchmark.peek16BitsBe | avgt | 25 | 9.206 | 0.050 | ns/op |
| BitInputStreamBenchmark.peek16BitsLe | avgt | 25 | 8.946 | 0.028 | ns/op |
| BitInputStreamBenchmark.read63BitsBe | avgt | 25 | 14.106 | 0.102 | ns/op |
| BitInputStreamBenchmark.read63BitsLe | avgt | 25 | 14.685 | 0.049 | ns/op |
| BitInputStreamBenchmark.readSingleBitBe | avgt | 25 | 1.995 | 0.016 | ns/op |
| BitInputStreamBenchmark.readSingleBitLe | avgt | 25 | 1.958 | 0.012 | ns/op |
| BitOutputStreamBenchmark.write63BitsBe | avgt | 25 | 2.524 | 0.044 | ns/op |
| BitOutputStreamBenchmark.write63BitsLe | avgt | 25 | 2.705 | 0.039 | ns/op |
| BitOutputStreamBenchmark.writeSingleBitBe | avgt | 25 | 1.032 | 0.022 | ns/op |
| BitOutputStreamBenchmark.writeSingleBitLe | avgt | 25 | 1.015 | 0.007 | ns/op |

The `count` benchmarks also read the bit that ends each run, and the `peek16Bits` benchmarks read 9 bits after each peek, as Rice and Huffman decoders would.

## Installation

This package is currently published to Maven Central as a release candidate. To use it in your project, you can compile it locally using Gradle.

```kotlin
dependencies {
    implementation("dev.javax:bitstream:1.0.0-RC")
}
```

## Quick Start

### Reading Bits

```java
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteOrder;

// 1. Initialize your underlying byte stream
InputStream fileStream = new FileInputStream("data.bin");

// 2. Wrap it in a BitInputStream, specifying the endianness
BitInputStream bitReader = BitInputStream.wrap(fileStream, ByteOrder.LITTLE_ENDIAN);

// 3. Read specific numbers of bits (returns a long to support up to 63 unsigned bits)
long header   = bitReader.readBits(4);
long length   = bitReader.readBits(12);
long checksum = bitReader.readBits(16);

// 4. Align back to a byte boundary if necessary
bitReader.alignToByte();
```

### Unary and Huffman Codes

```java
// Consume a run of 0 bits (or 1 bits with countOnes), leaving the bit that ends it unread
int quotient = bitReader.countZeros();
bitReader.readBits(1);

// Look at up to BitInputStream.MAX_PEEK_BITS (57) bits without consuming them,
// then read only as many as the code they start turned out to be
long nextBits = bitReader.peekBits(16);
bitReader.readBits(codeLengths[(int) nextBits]);
```

### Writing Bits

```java
import java.io.ByteArrayOutputStream;
import java.nio.ByteOrder;

// 1. Initialize your underlying byte stream
ByteArrayOutputStream memStream = new ByteArrayOutputStream();

// 2. Wrap it in a BitOutputStream
BitOutputStream bitWriter = BitOutputStream.wrap(memStream, ByteOrder.BIG_ENDIAN);

// 3. Write bits (value, numberOfBits)
bitWriter.writeBits(0xAL, 4);      // Writes 1010
bitWriter.writeBits(0xFFL, 8);     // Writes 11111111
bitWriter.writeBits(0x01L, 1);     // Writes 1

// 4. Flush the buffer to the underlying stream
bitWriter.flush();

// 5. Clean up
bitWriter.close();
```

### Endianness Handling

Bit order and byte order can be confusing when dealing with bit streams. This library explicitly defines how bits are packed into bytes:

* **Big-Endian (MSB First):** Bits are populated starting from the Most Significant Bit of the current byte. Commonly used in network protocols.
* **Little-Endian (LSB First):** Bits are populated starting from the Least Significant Bit of the current byte. Commonly used in zip files and certain instruction sets.

## Contributing

Contributions, issues, and feature requests are welcome. Feel free to check the issues page if you want to contribute.

## License

This project is licensed under the MIT License - see the LICENSE file for details.