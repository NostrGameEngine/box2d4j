package org.box2d4j;

import org.junit.jupiter.api.Test;

import static org.box2d4j.B2.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TableTest {
    private static final int SET_SPAN = 317;
    private static final int ITEM_COUNT = (SET_SPAN * SET_SPAN - SET_SPAN) / 2;

    @Test
    void tableParity() {
        int power = b2BoundingPowerOf2(3008);
        assertEquals(12, power);

        int nextPowerOf2 = b2RoundUpPowerOf2(3008);
        assertEquals(1 << power, nextPowerOf2);

        int n = SET_SPAN;
        int itemCount = ITEM_COUNT;
        boolean[] removed = new boolean[ITEM_COUNT];

        b2HashSet set = b2CreateSet(16);

        for (int i = 0; i < n; ++i) {
            for (int j = i + 1; j < n; ++j) {
                b2AddKey(set, B2_SHAPE_PAIR_KEY(i, j));
            }
        }

        assertEquals(itemCount, set.count);

        int k = 0;
        int removeCount = 0;
        for (int i = 0; i < n; ++i) {
            for (int j = i + 1; j < n; ++j) {
                if (j == i + 1) {
                    b2RemoveKey(set, B2_SHAPE_PAIR_KEY(i, j));
                    removed[k++] = true;
                    removeCount += 1;
                } else {
                    removed[k++] = false;
                }
            }
        }

        assertEquals(itemCount - removeCount, set.count);

        k = 0;
        for (int i = 0; i < n; ++i) {
            for (int j = i + 1; j < n; ++j) {
                long key = B2_SHAPE_PAIR_KEY(j, i);
                assertTrue(b2ContainsKey(set, key) || removed[k]);
                k += 1;
            }
        }

        for (int i = 0; i < n; ++i) {
            for (int j = i + 1; j < n; ++j) {
                b2RemoveKey(set, B2_SHAPE_PAIR_KEY(i, j));
            }
        }

        assertEquals(0, set.count);
        b2DestroySet(set);
    }
}
