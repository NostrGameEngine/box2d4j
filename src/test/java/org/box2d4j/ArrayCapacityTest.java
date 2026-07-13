package org.box2d4j;

import org.junit.jupiter.api.Test;

import static org.box2d4j.B2.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArrayCapacityTest {
    @Test
    void outputCapacityNeverExceedsTheJavaArrayLength() {
        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = b2Vec2_zero.copy();
        b2WorldId worldId = b2CreateWorld(worldDef);
        try {
            b2BodyId ground = b2CreateBody(worldId, b2DefaultBodyDef());
            b2ShapeId groundShape = b2CreateCircleShape(ground, b2DefaultShapeDef(),
                new b2Circle(new b2Vec2(), 1.0f));

            b2BodyDef dynamicDef = b2DefaultBodyDef();
            dynamicDef.type = b2_dynamicBody;
            b2BodyId dynamic = b2CreateBody(worldId, dynamicDef);
            b2ShapeId dynamicShape = b2CreateCircleShape(dynamic, b2DefaultShapeDef(),
                new b2Circle(new b2Vec2(), 1.0f));

            b2DistanceJointDef jointDef = b2DefaultDistanceJointDef();
            jointDef.bodyIdA = ground;
            jointDef.bodyIdB = dynamic;
            jointDef.length = 1.0f;
            jointDef.collideConnected = true;
            b2CreateDistanceJoint(worldId, jointDef);

            b2ChainDef chainDef = b2DefaultChainDef();
            chainDef.points = new b2Vec2[] {
                new b2Vec2(-4.0f, -2.0f), new b2Vec2(-3.0f, -2.0f),
                new b2Vec2(-2.0f, -2.0f), new b2Vec2(-1.0f, -2.0f)
            };
            chainDef.count = chainDef.points.length;
            b2ChainId chain = b2CreateChain(ground, chainDef);

            b2BodyDef sensorBodyDef = b2DefaultBodyDef();
            sensorBodyDef.position = new b2Vec2(5.0f, 0.0f);
            b2BodyId sensorBody = b2CreateBody(worldId, sensorBodyDef);
            b2ShapeDef sensorDef = b2DefaultShapeDef();
            sensorDef.isSensor = true;
            sensorDef.enableSensorEvents = true;
            b2ShapeId sensorShape = b2CreateCircleShape(sensorBody, sensorDef,
                new b2Circle(new b2Vec2(), 1.0f));
            dynamicDef.position = new b2Vec2(5.0f, 0.0f);
            b2BodyId visitor = b2CreateBody(worldId, dynamicDef);
            b2ShapeDef visitorDef = b2DefaultShapeDef();
            visitorDef.enableSensorEvents = true;
            b2CreateCircleShape(visitor, visitorDef, new b2Circle(new b2Vec2(), 0.5f));

            b2World_Step(worldId, 1.0f / 60.0f, 4);

            assertTrue(b2Body_GetShapeCount(dynamic) > 0);
            assertTrue(b2Body_GetJointCount(dynamic) > 0);
            assertTrue(b2Chain_GetSegmentCount(chain) > 0);
            assertTrue(b2Body_GetContactCapacity(dynamic) > 0);
            assertTrue(b2Shape_GetContactCapacity(dynamicShape) > 0);
            assertTrue(b2Shape_GetSensorCapacity(sensorShape) > 0);

            assertEquals(0, b2Body_GetShapes(dynamic, new b2ShapeId[0], 10));
            assertEquals(0, b2Body_GetJoints(dynamic, new b2JointId[0], 10));
            assertEquals(0, b2Chain_GetSegments(chain, new b2ShapeId[0], 10));
            assertEquals(0, b2Body_GetContactData(dynamic, new b2ContactData[0], 10));
            assertEquals(0, b2Shape_GetContactData(dynamicShape, new b2ContactData[0], 10));
            assertEquals(0, b2Shape_GetSensorOverlaps(sensorShape, new b2ShapeId[0], 10));

            assertEquals(0, b2Body_GetShapes(dynamic, new b2ShapeId[1], -1));
            assertEquals(0, b2Body_GetJoints(dynamic, new b2JointId[1], -1));
            assertEquals(0, b2Chain_GetSegments(chain, new b2ShapeId[1], -1));
            assertEquals(0, b2Body_GetContactData(dynamic, new b2ContactData[1], -1));
            assertEquals(0, b2Shape_GetContactData(groundShape, new b2ContactData[1], -1));
            assertEquals(0, b2Shape_GetSensorOverlaps(sensorShape, new b2ShapeId[1], -1));
        } finally {
            b2DestroyWorld(worldId);
        }
    }
}
