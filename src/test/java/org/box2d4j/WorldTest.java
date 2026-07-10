package org.box2d4j;

import org.junit.jupiter.api.Test;

import static org.box2d4j.B2.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class WorldTest {
    @Test
    void helloWorld() {
        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = new b2Vec2(0.0f, -10.0f);
        b2WorldId worldId = b2CreateWorld(worldDef);
        assertTrue(b2World_IsValid(worldId));

        b2BodyDef groundBodyDef = b2DefaultBodyDef();
        groundBodyDef.position = new b2Vec2(0.0f, -10.0f);
        b2BodyId groundId = b2CreateBody(worldId, groundBodyDef);
        assertTrue(b2Body_IsValid(groundId));

        b2ShapeId groundShapeId = b2CreatePolygonShape(groundId, b2DefaultShapeDef(), b2MakeBox(50.0f, 10.0f));

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(0.0f, 4.0f);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        shapeDef.material.friction = 0.3f;
        b2ShapeId shapeId = b2CreatePolygonShape(bodyId, shapeDef, b2MakeBox(1.0f, 1.0f));

        b2Vec2 position = b2Body_GetPosition(bodyId);
        b2Rot rotation = b2Body_GetRotation(bodyId);
        for (int i = 0; i < 90; ++i) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
            position = b2Body_GetPosition(bodyId);
            rotation = b2Body_GetRotation(bodyId);
        }

        assertEquals(1, b2Body_GetContactCapacity(bodyId));
        b2ContactData[] contactData = new b2ContactData[1];
        assertEquals(1, b2Body_GetContactData(bodyId, contactData, contactData.length));
        assertEquals(1, b2Shape_GetContactCapacity(shapeId));
        assertEquals(1, b2Shape_GetContactCapacity(groundShapeId));
        b2DestroyWorld(worldId);
        assertTrue(b2AbsFloat(position.x) < 0.01f);
        assertTrue(b2AbsFloat(position.y - 1.00f) < 0.01f);
        assertTrue(b2AbsFloat(b2Rot_GetAngle(rotation)) < 0.01f);
    }

    @Test
    void emptyWorldAndDestroyAllBodies() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        assertTrue(b2World_IsValid(worldId));
        for (int i = 0; i < 60; ++i) {
            b2World_Step(worldId, 1.0f / 60.0f, 1);
        }
        b2DestroyWorld(worldId);
        assertFalse(b2World_IsValid(worldId));

        worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId[] bodyIds = new b2BodyId[10];
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2Polygon square = b2MakeSquare(0.5f);
        int count = 0;
        boolean creating = true;
        for (int i = 0; i < 30; ++i) {
            if (creating) {
                if (count < bodyIds.length) {
                    bodyIds[count] = b2CreateBody(worldId, bodyDef);
                    b2CreatePolygonShape(bodyIds[count], b2DefaultShapeDef(), square);
                    count += 1;
                } else {
                    creating = false;
                }
            } else if (count > 0) {
                b2DestroyBody(bodyIds[count - 1]);
                bodyIds[count - 1] = b2_nullBodyId;
                count -= 1;
            }
            b2World_Step(worldId, 1.0f / 60.0f, 3);
        }
        assertEquals(0, b2World_GetCounters(worldId).bodyCount);
        b2DestroyWorld(worldId);
        assertFalse(b2World_IsValid(worldId));
    }

    @Test
    void idValidityAndWorldRecycle() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId bodyId1 = b2CreateBody(worldId, b2DefaultBodyDef());
        b2BodyId bodyId2 = b2CreateBody(worldId, b2DefaultBodyDef());
        assertTrue(b2Body_IsValid(bodyId1));
        assertTrue(b2Body_IsValid(bodyId2));
        b2DestroyBody(bodyId1);
        assertFalse(b2Body_IsValid(bodyId1));
        b2DestroyBody(bodyId2);
        assertFalse(b2Body_IsValid(bodyId2));
        b2DestroyWorld(worldId);
        assertFalse(b2World_IsValid(worldId));
        assertFalse(b2Body_IsValid(bodyId1));
        assertFalse(b2Body_IsValid(bodyId2));

        int worldCount = B2_MAX_WORLDS / 2;
        b2WorldId[] worldIds = new b2WorldId[worldCount];
        for (int iter = 0; iter < 10; ++iter) {
            for (int i = 0; i < worldCount; ++i) {
                worldIds[i] = b2CreateWorld(b2DefaultWorldDef());
                assertTrue(b2World_IsValid(worldIds[i]));
                b2CreateBody(worldIds[i], b2DefaultBodyDef());
            }
            for (int i = 0; i < worldCount; ++i) {
                for (int k = 0; k < 10; ++k) {
                    b2World_Step(worldIds[i], 1.0f / 60.0f, 1);
                }
            }
            for (int i = worldCount - 1; i >= 0; --i) {
                b2DestroyWorld(worldIds[i]);
                assertFalse(b2World_IsValid(worldIds[i]));
                worldIds[i] = b2_nullWorldId;
            }
        }
    }

    @Test
    void worldCoverageAndSensorEvents() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2World_EnableSleeping(worldId, true);
        b2World_EnableSleeping(worldId, false);
        assertFalse(b2World_IsSleepingEnabled(worldId));

        b2World_EnableContinuous(worldId, false);
        b2World_EnableContinuous(worldId, true);
        assertTrue(b2World_IsContinuousEnabled(worldId));

        b2World_SetRestitutionThreshold(worldId, 0.0f);
        b2World_SetRestitutionThreshold(worldId, 2.0f);
        assertEquals(2.0f, b2World_GetRestitutionThreshold(worldId));

        b2World_SetHitEventThreshold(worldId, 0.0f);
        b2World_SetHitEventThreshold(worldId, 100.0f);
        assertEquals(100.0f, b2World_GetHitEventThreshold(worldId));

        b2World_SetCustomFilterCallback(worldId, new Object(), null);
        b2World_SetPreSolveCallback(worldId, new Object(), null);

        b2Vec2 gravity = new b2Vec2(1.0f, 2.0f);
        b2World_SetGravity(worldId, gravity);
        b2Vec2 v = b2World_GetGravity(worldId);
        assertTrue(v.x == gravity.x && v.y == gravity.y);

        b2World_Explode(worldId, b2DefaultExplosionDef());
        b2World_SetContactTuning(worldId, 10.0f, 2.0f, 4.0f);
        b2World_SetMaximumLinearSpeed(worldId, 10.0f);
        assertEquals(10.0f, b2World_GetMaximumLinearSpeed(worldId));
        b2World_EnableWarmStarting(worldId, true);
        assertTrue(b2World_IsWarmStartingEnabled(worldId));
        assertEquals(0, b2World_GetAwakeBodyCount(worldId));

        Object userData = new Object();
        b2World_SetUserData(worldId, userData);
        assertSame(userData, b2World_GetUserData(worldId));
        b2World_Step(worldId, 1.0f, 1);
        b2DestroyWorld(worldId);

        worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_staticBody;
        bodyDef.position = new b2Vec2(1.5f, 11.0f);
        b2BodyId wallId = b2CreateBody(worldId, bodyDef);
        b2ShapeDef sensorEventsDef = b2DefaultShapeDef();
        sensorEventsDef.enableSensorEvents = true;
        b2CreatePolygonShape(wallId, sensorEventsDef, b2MakeBox(0.5f, 10.0f));

        bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.isBullet = true;
        bodyDef.gravityScale = 0.0f;
        bodyDef.position = new b2Vec2(7.39814f, 4.0f);
        bodyDef.linearVelocity = new b2Vec2(-20.0f, 0.0f);
        b2BodyId bulletId = b2CreateBody(worldId, bodyDef);
        b2ShapeDef bulletShapeDef = b2DefaultShapeDef();
        bulletShapeDef.isSensor = true;
        bulletShapeDef.enableSensorEvents = true;
        b2CreateCircleShape(bulletId, bulletShapeDef, new b2Circle(new b2Vec2(), 0.1f));

        int beginCount = 0;
        int endCount = 0;
        while (true) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
            b2Vec2 bulletPos = b2Body_GetPosition(bulletId);
            b2SensorEvents events = b2World_GetSensorEvents(worldId);
            if (events.beginCount > 0) {
                beginCount += 1;
            }
            if (events.endCount > 0) {
                endCount += 1;
            }
            if (bulletPos.x < -1.0f) {
                break;
            }
        }

        b2DestroyWorld(worldId);
        assertEquals(1, beginCount);
        assertEquals(1, endCount);
    }
}
