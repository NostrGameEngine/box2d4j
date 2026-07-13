package org.box2d4j;

import org.junit.jupiter.api.Test;

import static org.box2d4j.B2.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class IdTest {
    @Test
    void idRoundTrips() {
        int a = 0x01234567;
        b2WorldId worldId = b2LoadWorldId(a);
        assertEquals(a, b2StoreWorldId(worldId));

        long x = 0x0123456789ABCDEFL;

        b2BodyId bodyId = b2LoadBodyId(x);
        assertEquals(x, b2StoreBodyId(bodyId));

        b2ShapeId shapeId = b2LoadShapeId(x);
        assertEquals(x, b2StoreShapeId(shapeId));

        b2ChainId chainId = b2LoadChainId(x);
        assertEquals(x, b2StoreChainId(chainId));

        b2JointId jointId = b2LoadJointId(x);
        assertEquals(x, b2StoreJointId(jointId));
    }

    @Test
    void publicNullMacrosAndEmptySimplexCacheMirrorTheHeaders() {
        assertTrue(B2_IS_NULL(b2_nullWorldId));
        assertTrue(B2_IS_NULL(b2_nullBodyId));
        assertTrue(B2_IS_NULL(b2_nullShapeId));
        assertTrue(B2_IS_NULL(b2_nullChainId));
        assertTrue(B2_IS_NULL(b2_nullJointId));
        assertFalse(B2_IS_NON_NULL(b2_nullWorldId));
        assertFalse(B2_IS_NON_NULL(b2_nullBodyId));
        assertFalse(B2_IS_NON_NULL(b2_nullShapeId));
        assertFalse(B2_IS_NON_NULL(b2_nullChainId));
        assertFalse(B2_IS_NON_NULL(b2_nullJointId));

        assertTrue(B2_IS_NON_NULL(new b2WorldId(1, 0)));
        assertTrue(B2_IS_NON_NULL(new b2BodyId(1, 0, 0)));
        assertTrue(B2_IS_NON_NULL(new b2ShapeId(1, 0, 0)));
        assertTrue(B2_IS_NON_NULL(new b2ChainId(1, 0, 0)));
        assertTrue(B2_IS_NON_NULL(new b2JointId(1, 0, 0)));

        assertEquals(0, b2_emptySimplexCache.count);
        assertEquals(3, b2_emptySimplexCache.indexA.length);
        assertEquals(3, b2_emptySimplexCache.indexB.length);
        for (int i = 0; i < 3; ++i) {
            assertEquals(0, b2_emptySimplexCache.indexA[i]);
            assertEquals(0, b2_emptySimplexCache.indexB[i]);
        }
    }
}
