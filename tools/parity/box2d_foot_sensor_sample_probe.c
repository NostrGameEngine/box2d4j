// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>
#include <stdlib.h>

enum
{
    DEFAULT_STEP_COUNT = 120,
    GROUND = 0x00000001,
    PLAYER = 0x00000002,
    FOOT = 0x00000004
};

static void print_body(b2BodyId bodyId)
{
    b2Transform transform = b2Body_GetTransform(bodyId);
    b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
    printf(" %.9g %.9g %.9g %.9g %.9g %.9g %.9g %d %d",
           transform.p.x,
           transform.p.y,
           transform.q.c,
           transform.q.s,
           velocity.x,
           velocity.y,
           b2Body_GetAngularVelocity(bodyId),
           b2Body_GetShapeCount(bodyId),
           b2Body_GetContactCapacity(bodyId));
}

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : DEFAULT_STEP_COUNT;

    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);

    b2Vec2 points[20];
    float x = 10.0f;
    for (int i = 0; i < 20; ++i)
    {
        points[i] = (b2Vec2){x, 0.0f};
        x -= 1.0f;
    }

    b2ChainDef chainDef = b2DefaultChainDef();
    chainDef.points = points;
    chainDef.count = 20;
    chainDef.filter.categoryBits = GROUND;
    chainDef.filter.maskBits = FOOT | PLAYER;
    chainDef.isLoop = false;
    chainDef.enableSensorEvents = true;
    b2CreateChain(groundId, &chainDef);

    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.fixedRotation = true;
    bodyDef.position = (b2Vec2){0.0f, 1.0f};
    b2BodyId playerId = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.filter.categoryBits = PLAYER;
    shapeDef.filter.maskBits = GROUND;
    shapeDef.material.friction = 0.3f;
    b2Capsule capsule = {{0.0f, -0.5f}, {0.0f, 0.5f}, 0.5f};
    b2CreateCapsuleShape(playerId, &shapeDef, &capsule);

    b2Polygon box = b2MakeOffsetBox(0.5f, 0.25f, (b2Vec2){0.0f, -1.0f}, b2Rot_identity);
    shapeDef.filter.categoryBits = FOOT;
    shapeDef.filter.maskBits = GROUND;
    shapeDef.isSensor = true;
    shapeDef.enableSensorEvents = true;
    b2ShapeId sensorId = b2CreatePolygonShape(playerId, &shapeDef, &box);

    int overlapCount = 0;
    int beginTotal = 0;
    int endTotal = 0;
    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        b2SensorEvents sensorEvents = b2World_GetSensorEvents(worldId);
        beginTotal += sensorEvents.beginCount;
        endTotal += sensorEvents.endCount;
        for (int i = 0; i < sensorEvents.beginCount; ++i)
        {
            if (B2_ID_EQUALS(sensorEvents.beginEvents[i].sensorShapeId, sensorId))
            {
                overlapCount += 1;
            }
        }
        for (int i = 0; i < sensorEvents.endCount; ++i)
        {
            if (B2_ID_EQUALS(sensorEvents.endEvents[i].sensorShapeId, sensorId))
            {
                overlapCount -= 1;
            }
        }
    }

    b2ShapeId overlaps[8];
    int sensorCapacity = b2Shape_GetSensorCapacity(sensorId);
    int sensorOverlapCount = b2Shape_GetSensorOverlaps(sensorId, overlaps, 8);
    b2Counters counters = b2World_GetCounters(worldId);
    printf("footSensor %d %d %d %d %d %d %d %d %d %d",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           counters.jointCount,
           b2World_GetAwakeBodyCount(worldId),
           beginTotal,
           endTotal,
           overlapCount,
           sensorCapacity,
           sensorOverlapCount);
    print_body(playerId);
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
