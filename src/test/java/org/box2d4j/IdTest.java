package org.box2d4j;

import org.junit.jupiter.api.Test;

import static org.box2d4j.B2.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
