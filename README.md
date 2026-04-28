# bitstream

A high-performance Java library for reading and writing individual bits to and from underlying byte streams.

Standard `java.io` libraries are designed to operate on bytes. When parsing binary formats, network protocols, or compressed data (like Huffman coding or video codecs), you frequently need to read and write at the bit level. This library bridges that gap, providing a highly optimized layer for bitwise I/O with first-class support for multiple endian formats.

## Features

* **High Performance:** Heavily optimized read/write loops utilizing internal buffering and efficient bit-shifting. Reduces the overhead of bit-by-bit extraction.
* **Multi-Endian Support:** Read and write bits in both Big-Endian and Little-Endian byte orders using standard `java.nio.ByteOrder`.
* **Bidirectional I/O:** Complete implementations wrapping standard `InputStream` and `OutputStream` instances via clean static factory methods.
* **Zero-Allocation Reads:** Designed to minimize memory allocations and garbage collection overhead during hot-path stream processing.

## Installation

This package is not currently published to Maven Central. To use it in your project, you can compile it locally using Gradle.

**1. Clone and build the library:**
```bash
git clone [https://github.com/yourusername/bit-streams.git](https://github.com/yourusername/bit-streams.git)
cd bit-streams
./gradlew build
```

**2. Add to your project:**
Copy the generated JAR file (usually located in `build/libs/`) into your project's `libs` directory and include it in your `build.gradle`:

```groovy
dependencies {
    implementation files('libs/bit-streams-1.0.jar') // Update with actual filename
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

// 5. Clean up
bitReader.close();
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