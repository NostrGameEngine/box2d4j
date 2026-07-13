package org.box2d4j;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.box2d4j.B2.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class WorldStepTest {
    @AfterEach
    void restoreAssertHandler() {
        b2SetAssertFcn((condition, fileName, lineNumber) -> 1);
    }

    @Test
    void rejectsInvalidStepArguments() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        try {
            assertThrows(AssertionError.class, () -> b2World_Step(worldId, Float.NaN, 1));
            assertThrows(AssertionError.class, () -> b2World_Step(worldId, Float.POSITIVE_INFINITY, 1));
            assertThrows(AssertionError.class, () -> b2World_Step(worldId, 1.0f / 60.0f, 0));
            assertThrows(AssertionError.class, () -> b2World_Step(worldId, 1.0f / 60.0f, -1));
        } finally {
            b2DestroyWorld(worldId);
        }
    }

    @Test
    void unlocksWorldWhenCallbackThrows() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId staticBody = b2CreateBody(worldId, b2DefaultBodyDef());
        b2CreatePolygonShape(staticBody, b2DefaultShapeDef(), b2MakeSquare(1.0f));

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2BodyId dynamicBody = b2CreateBody(worldId, bodyDef);
        b2CreatePolygonShape(dynamicBody, b2DefaultShapeDef(), b2MakeSquare(1.0f));

        b2World_SetCustomFilterCallback(worldId, (shapeA, shapeB, context) -> {
            throw new IllegalStateException("callback failure");
        }, null);

        assertThrows(IllegalStateException.class, () -> b2World_Step(worldId, 1.0f / 60.0f, 1));
        b2World_SetCustomFilterCallback(worldId, (b2CustomFilterFcn) null, null);
        assertTrue(b2Body_IsValid(b2CreateBody(worldId, b2DefaultBodyDef())));
        assertDoesNotThrow(() -> b2World_Step(worldId, 1.0f / 60.0f, 1));
        b2DestroyWorld(worldId);
    }

    @Test
    void zeroStepClearsProfileWithoutAdvancingTaskTelemetry() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        b2CreateCircleShape(bodyId, b2DefaultShapeDef(), new b2Circle(new b2Vec2(), 0.5f));

        b2World_Step(worldId, 1.0f / 60.0f, 1);
        b2Profile measured = b2World_GetProfile(worldId);
        int taskCount = b2World_GetCounters(worldId).taskCount;
        assertTrue(measured.step > 0.0f);
        assertTrue(measured.solve > 0.0f);
        assertTrue(taskCount > 0);

        measured.step = -1.0f;
        assertTrue(b2World_GetProfile(worldId).step > 0.0f);
        b2World_Step(worldId, 0.0f, 1);
        b2Profile cleared = b2World_GetProfile(worldId);
        assertEquals(0.0f, cleared.step);
        assertEquals(0.0f, cleared.solve);
        assertEquals(0.0f, cleared.sensors);
        assertEquals(taskCount, b2World_GetCounters(worldId).taskCount);
        b2DestroyWorld(worldId);
    }
}
