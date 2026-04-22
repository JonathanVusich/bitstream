package org.bitstream.converter;

@FunctionalInterface
public interface EndianAwareWriter {

    long write(long input, int numBits);
}
