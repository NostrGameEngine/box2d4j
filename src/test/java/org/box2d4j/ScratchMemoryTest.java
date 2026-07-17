package org.box2d4j;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.box2d4j.B2.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ScratchMemoryTest {
    @Test
    void nestedTreeQueriesUseIndependentScratchStacks() {
        b2DynamicTree tree = b2DynamicTree_Create();
        b2AABB first = new b2AABB(new b2Vec2(-1.0f, -1.0f), new b2Vec2(1.0f, 1.0f));
        b2AABB second = new b2AABB(new b2Vec2(2.0f, -1.0f), new b2Vec2(4.0f, 1.0f));
        b2DynamicTree_CreateProxy(tree, first, B2_DEFAULT_CATEGORY_BITS, 1L);
        b2DynamicTree_CreateProxy(tree, second, B2_DEFAULT_CATEGORY_BITS, 2L);
        b2AABB all = new b2AABB(new b2Vec2(-2.0f, -2.0f), new b2Vec2(5.0f, 2.0f));
        AtomicInteger outerVisits = new AtomicInteger();
        AtomicInteger nestedVisits = new AtomicInteger();

        b2DynamicTree_Query(tree, all, B2_DEFAULT_MASK_BITS, (proxyId, userData) -> {
            outerVisits.incrementAndGet();
            b2DynamicTree_Query(tree, all, B2_DEFAULT_MASK_BITS, (nestedId, nestedData) -> {
                nestedVisits.incrementAndGet();
                return true;
            });
            return true;
        });

        assertEquals(2, outerVisits.get());
        assertEquals(4, nestedVisits.get());
        b2DynamicTree_Destroy(tree);
    }

    @Test
    void polygonCollisionScratchDoesNotEscapeThroughManifolds() {
        b2Polygon box = b2MakeBox(1.0f, 1.0f);
        b2Transform firstTransform = new b2Transform(new b2Vec2(), b2Rot_identity);
        b2Transform secondTransform = new b2Transform(new b2Vec2(1.5f, 0.0f), b2Rot_identity);
        b2Manifold first = b2CollidePolygons(box, firstTransform, box, secondTransform);
        assertTrue(first.pointCount > 0);
        float normalX = first.normal.x;
        float anchorX = first.points[0].anchorA.x;

        b2Transform thirdTransform = new b2Transform(new b2Vec2(0.0f, 1.5f), b2Rot_identity);
        b2CollidePolygons(box, firstTransform, box, thirdTransform);

        assertEquals(normalX, first.normal.x, 0.0f);
        assertEquals(anchorX, first.points[0].anchorA.x, 0.0f);
    }
}
