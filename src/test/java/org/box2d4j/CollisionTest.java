package org.box2d4j;

import org.junit.jupiter.api.Test;

import static org.box2d4j.B2.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CollisionTest {
    @Test
    void aabbRayCast() {
        b2AABB a = new b2AABB(new b2Vec2(-1.0f, -1.0f), new b2Vec2(-2.0f, -2.0f));
        assertFalse(b2IsValidAABB(a));

        a.upperBound = new b2Vec2(1.0f, 1.0f);
        assertTrue(b2IsValidAABB(a));

        b2AABB b = new b2AABB(new b2Vec2(2.0f, 2.0f), new b2Vec2(4.0f, 4.0f));
        assertFalse(b2AABB_Overlaps(a, b));
        assertFalse(b2AABB_Contains(a, b));

        b2CastOutput output = b2AABB_RayCast(a, new b2Vec2(-2.0f, 0.0f), new b2Vec2(2.0f, 0.0f));
        assertTrue(output.hit);
        assertTrue(0.1f < output.fraction && output.fraction < 0.9f);
    }
}
