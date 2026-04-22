package org.bitstream.converter;

@FunctionalInterface
public interface EndianAwareReader {

    long read(final long input);
}
