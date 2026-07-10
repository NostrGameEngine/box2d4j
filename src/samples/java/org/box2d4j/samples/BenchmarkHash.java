package org.box2d4j.samples;

final class BenchmarkHash {
    private static final long FNV_PRIME = 0x100000001b3L;
    private long value = 0xcbf29ce484222325L;

    void mix(int bits) {
        value = (value ^ Integer.toUnsignedLong(bits)) * FNV_PRIME;
    }

    void mix(long bits) {
        mix((int) bits);
        mix((int) (bits >>> 32));
    }

    long value() {
        return value;
    }
}
