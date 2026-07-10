package org.box2d4j;

import org.junit.jupiter.api.Test;

import static org.box2d4j.B2.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class BitSetTest {
    private static final int COUNT = 169;

    @Test
    void fibonacciBits() {
        b2BitSet bitSet = b2CreateBitSet(COUNT);
        b2SetBitCountAndClear(bitSet, COUNT);
        boolean[] values = new boolean[COUNT];

        int i1 = 0;
        int i2 = 1;
        b2SetBit(bitSet, i1);
        values[i1] = true;

        while (i2 < COUNT) {
            b2SetBit(bitSet, i2);
            values[i2] = true;
            int next = i1 + i2;
            i1 = i2;
            i2 = next;
        }

        for (int i = 0; i < COUNT; ++i) {
            assertEquals(values[i], b2GetBit(bitSet, i), "bit " + i);
        }

        b2DestroyBitSet(bitSet);
    }
}
