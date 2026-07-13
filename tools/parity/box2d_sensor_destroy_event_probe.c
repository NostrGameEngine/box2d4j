// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>

static void run_case(bool destroyBody)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = b2Vec2_zero;
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef sensorBodyDef = b2DefaultBodyDef();
    b2BodyId sensorBodyId = b2CreateBody(worldId, &sensorBodyDef);
    b2ShapeDef sensorDef = b2DefaultShapeDef();
    sensorDef.isSensor = true;
    sensorDef.enableSensorEvents = true;
    b2Circle sensorCircle = {{0.0f, 0.0f}, 1.0f};
    b2ShapeId sensorShapeId = b2CreateCircleShape(sensorBodyId, &sensorDef, &sensorCircle);

    b2BodyDef visitorBodyDef = b2DefaultBodyDef();
    visitorBodyDef.type = b2_dynamicBody;
    b2BodyId visitorBodyId = b2CreateBody(worldId, &visitorBodyDef);
    b2ShapeDef visitorDef = b2DefaultShapeDef();
    visitorDef.enableSensorEvents = true;
    b2Circle visitorCircle = {{0.0f, 0.0f}, 0.5f};
    b2ShapeId visitorShapeId = b2CreateCircleShape(visitorBodyId, &visitorDef, &visitorCircle);

    b2World_Step(worldId, 1.0f / 60.0f, 4);
    b2SensorEvents events = b2World_GetSensorEvents(worldId);
    int beginCount = events.beginCount;
    if (destroyBody)
    {
        b2DestroyBody(sensorBodyId);
    }
    else
    {
        b2DestroyShape(sensorShapeId, false);
    }
    int immediateEndCount = b2World_GetSensorEvents(worldId).endCount;
    b2World_Step(worldId, 0.0f, 1);
    events = b2World_GetSensorEvents(worldId);
    int deferredEndCount = events.endCount;
    int sensorIndex = deferredEndCount > 0 ? events.endEvents[0].sensorShapeId.index1 : 0;
    int visitorIndex = deferredEndCount > 0 ? events.endEvents[0].visitorShapeId.index1 : 0;
    printf(" %d %d %d %d %d %d %d", destroyBody ? 1 : 0, beginCount, immediateEndCount, deferredEndCount,
           sensorIndex, visitorIndex, b2Shape_IsValid(visitorShapeId) ? 1 : 0);

    b2DestroyWorld(worldId);
}

static void run_visitor_case(bool destroyBody)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = b2Vec2_zero;
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef sensorBodyDef = b2DefaultBodyDef();
    b2BodyId sensorBodyId = b2CreateBody(worldId, &sensorBodyDef);
    b2ShapeDef sensorDef = b2DefaultShapeDef();
    sensorDef.isSensor = true;
    sensorDef.enableSensorEvents = true;
    b2Circle sensorCircle = {{0.0f, 0.0f}, 1.0f};
    b2ShapeId sensorShapeId = b2CreateCircleShape(sensorBodyId, &sensorDef, &sensorCircle);

    b2BodyDef visitorBodyDef = b2DefaultBodyDef();
    visitorBodyDef.type = b2_dynamicBody;
    b2BodyId visitorBodyId = b2CreateBody(worldId, &visitorBodyDef);
    b2ShapeDef visitorDef = b2DefaultShapeDef();
    visitorDef.enableSensorEvents = true;
    b2Circle visitorCircle = {{0.0f, 0.0f}, 0.5f};
    b2ShapeId visitorShapeId = b2CreateCircleShape(visitorBodyId, &visitorDef, &visitorCircle);

    b2World_Step(worldId, 1.0f / 60.0f, 4);
    int beginCount = b2World_GetSensorEvents(worldId).beginCount;
    if (destroyBody)
    {
        b2DestroyBody(visitorBodyId);
    }
    else
    {
        b2DestroyShape(visitorShapeId, false);
    }
    b2World_Step(worldId, 0.0f, 1);
    int zeroStepEndCount = b2World_GetSensorEvents(worldId).endCount;
    b2World_Step(worldId, 1.0f / 60.0f, 4);
    b2SensorEvents events = b2World_GetSensorEvents(worldId);
    int positiveStepEndCount = events.endCount;
    int sensorIndex = positiveStepEndCount > 0 ? events.endEvents[0].sensorShapeId.index1 : 0;
    int visitorIndex = positiveStepEndCount > 0 ? events.endEvents[0].visitorShapeId.index1 : 0;
    printf(" %d %d %d %d %d %d %d", destroyBody ? 1 : 0, beginCount, zeroStepEndCount,
           positiveStepEndCount, sensorIndex, visitorIndex, b2Shape_IsValid(sensorShapeId) ? 1 : 0);

    b2DestroyWorld(worldId);
}

int main(void)
{
    printf("sensorDestroyEvent");
    run_case(false);
    run_case(true);
    run_visitor_case(false);
    run_visitor_case(true);
    printf("\n");
    return 0;
}
