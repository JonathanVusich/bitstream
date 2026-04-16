package org.bitstream.converter;

@FunctionalInterface
public interface EndianConverter {

    long convert(final long input);
}
