# bitstream

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
| ApacheStreamBenchmark.read63BitsBe | avgt | 25 | 45.324 | 2.260 | ns/op |
| ApacheStreamBenchmark.read63BitsLe | avgt | 25 | 46.637 | 2.005 | ns/op |
| ApacheStreamBenchmark.readSingleBitBe | avgt | 25 | 1.843 | 0.101 | ns/op |
| ApacheStreamBenchmark.readSingleBitLe | avgt | 25 | 1.853 | 0.052 | ns/op |
| BitInputStreamBenchmark.read63BitsBe | avgt | 25 | 14.458 | 0.221 | ns/op |
| BitInputStreamBenchmark.read63BitsLe | avgt | 25 | 13.720 | 0.182 | ns/op |
| BitInputStreamBenchmark.readSingleBitBe | avgt | 25 | 1.989 | 0.050 | ns/op |
| BitInputStreamBenchmark.readSingleBitLe | avgt | 25 | 1.948 | 0.093 | ns/op |
| BitOutputStreamBenchmark.write63BitsBe | avgt | 25 | 3.218 | 0.706 | ns/op |
| BitOutputStreamBenchmark.write63BitsLe | avgt | 25 | 3.272 | 0.538 | ns/op |
| BitOutputStreamBenchmark.writeSingleBitBe | avgt | 25 | 1.048 | 0.013 | ns/op |
| BitOutputStreamBenchmark.writeSingleBitLe | avgt | 25 | 1.046 | 0.016 | ns/op |

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