# bitstream

A high-performance Java library for reading and writing individual bits to and from underlying byte streams.

Standard `java.io` libraries are designed to operate on bytes. When parsing binary formats, network protocols, or compressed data (like Huffman coding or video codecs), you frequently need to read and write at the bit level. This library bridges that gap, providing a highly optimized layer for bitwise I/O with first-class support for multiple endian formats.

## Features

* **High Performance:** Heavily optimized read/write loops utilizing internal buffering and efficient bit-shifting. Reduces the overhead of bit-by-bit extraction.
* **Multi-Endian Support:** Read and write bits in both Big-Endian and Little-Endian byte orders using standard `java.nio.ByteOrder`.
* **Bidirectional I/O:** Complete implementations wrapping standard `InputStream` and `OutputStream` instances via clean static factory methods.
* **Zero-Allocation Reads:** Designed to minimize memory allocations and garbage collection overhead during hot-path stream processing.

## Performance (Apache BitInputStream added for comparison)

| Benchmark | Mode | Cnt | Score (ns/op) | Error (±) |
| :--- | :---: | :---: | :--- | :--- |
| **ApacheStreamBenchmark** | | | | |
| `.read63BitsBe` | `avgt` | 25 | 42.830 | 1.673 |
| `.read63BitsLe` | `avgt` | 25 | 43.324 | 1.548 |
| `.readSingleBitBe` | `avgt` | 25 | 1.699 | 0.073 |
| `.readSingleBitLe` | `avgt` | 25 | 1.712 | 0.052 |
| **BitInputStreamBenchmark** | | | | |
| `.read63BitsBe` | `avgt` | 25 | 6.737 | 0.115 |
| `.read63BitsLe` | `avgt` | 25 | 6.760 | 0.076 |
| `.readSingleBitBe` | `avgt` | 25 | 1.086 | 0.056 |
| `.readSingleBitLe` | `avgt` | 25 | 1.196 | 0.166 |
| **BitOutputStreamBenchmark** | | | | |
| `.write63BitsBe` | `avgt` | 25 | 2.994 | 0.669 |
| `.write63BitsLe` | `avgt` | 25 | 3.033 | 0.500 |
| `.writeSingleBitBe` | `avgt` | 25 | 0.982 | 0.015 |
| `.writeSingleBitLe` | `avgt` | 25 | 0.974 | 0.032 |

## Installation

This package is not currently published to Maven Central. To use it in your project, you can compile it locally using Gradle.

**1. Clone and build the library:**
```bash
git clone [https://github.com/JonathanVusich/bitstream.git](https://github.com/JonathanVusich/bitstream.git)
cd bitstream
./gradlew build
```

**2. Add to your project:**
Copy the generated JAR file (usually located in `build/libs/`) into your project's `libs` directory and include it in your `build.gradle`:

```kotlin
dependencies {
    implementation(files('libs/dev.javax.bitstream-1.0.0.jar')) 
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

// 3. Read specific numbers of bits (returns a long to support up to 64 bits)
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