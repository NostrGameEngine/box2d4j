package org.box2d4j;

import org.junit.jupiter.api.Test;

import static org.box2d4j.B2.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NullIdIsolationTest {
    @Test
    void returnedNullHandlesDoNotAliasPublicConstants() {
        b2WorldId[] worlds = new b2WorldId[B2_MAX_WORLDS];
        for (int i = 0; i < worlds.length; ++i) {
            worlds[i] = b2CreateWorld(b2DefaultWorldDef());
        }
        try {
            b2WorldId overflowA = b2CreateWorld(b2DefaultWorldDef());
            b2WorldId overflowB = b2CreateWorld(b2DefaultWorldDef());
            assertNotSame(b2_nullWorldId, overflowA);
            assertNotSame(overflowA, overflowB);
            overflowA.index1 = 42;
            overflowA.generation = 7;
            assertTrue(B2_IS_NULL(b2_nullWorldId));
            assertTrue(B2_IS_NULL(overflowB));
            assertEquals(0, b2_nullWorldId.generation);
        } finally {
            for (b2WorldId worldId : worlds) {
                b2DestroyWorld(worldId);
            }
        }

        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        try {
            b2BodyId bodyId = b2CreateBody(worldId, b2DefaultBodyDef());
            b2ShapeId shapeId = b2CreateCircleShape(bodyId, b2DefaultShapeDef(),
                new b2Circle(new b2Vec2(), 0.5f));
            b2ChainId parentA = b2Shape_GetParentChain(shapeId);
            b2ChainId parentB = b2Shape_GetParentChain(shapeId);
            assertNotSame(b2_nullChainId, parentA);
            assertNotSame(parentA, parentB);
            parentA.index1 = 42;
            assertTrue(B2_IS_NULL(b2_nullChainId));
            assertTrue(B2_IS_NULL(parentB));
        } finally {
            b2DestroyWorld(worldId);
        }
    }
}
