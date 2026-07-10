// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>

static void print_sensor_events(const char* label, b2WorldId worldId)
{
    b2SensorEvents events = b2World_GetSensorEvents(worldId);
    printf("%s counts %d %d\n", label, events.beginCount, events.endCount);
    if (events.beginCount > 0)
    {
        b2SensorBeginTouchEvent event = events.beginEvents[0];
        printf("%s begin %d %d %d %d\n", label,
               event.sensorShapeId.index1, event.sensorShapeId.generation,
               event.visitorShapeId.index1, event.visitorShapeId.generation);
    }
    if (events.endCount > 0)
    {
        b2SensorEndTouchEvent event = events.endEvents[0];
        printf("%s end %d %d %d %d\n", label,
               event.sensorShapeId.index1, event.sensorShapeId.generation,
               event.visitorShapeId.index1, event.visitorShapeId.generation);
    }
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = (b2Vec2){0.0f, 0.0f};
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef sensorBodyDef = b2DefaultBodyDef();
    b2BodyId sensorBodyId = b2CreateBody(worldId, &sensorBodyDef);
    b2ShapeDef sensorShapeDef = b2DefaultShapeDef();
    sensorShapeDef.isSensor = true;
    sensorShapeDef.enableSensorEvents = true;
    b2ShapeId sensorShapeId = b2CreateCircleShape(sensorBodyId, &sensorShapeDef, &(b2Circle){{0.0f, 0.0f}, 1.0f});

    b2BodyDef visitorBodyDef = b2DefaultBodyDef();
    visitorBodyDef.type = b2_dynamicBody;
    visitorBodyDef.position = (b2Vec2){0.5f, 0.0f};
    visitorBodyDef.gravityScale = 0.0f;
    b2BodyId visitorBodyId = b2CreateBody(worldId, &visitorBodyDef);
    b2ShapeDef visitorShapeDef = b2DefaultShapeDef();
    visitorShapeDef.density = 1.0f;
    visitorShapeDef.enableSensorEvents = true;
    b2ShapeId visitorShapeId = b2CreateCircleShape(visitorBodyId, &visitorShapeDef, &(b2Circle){{0.0f, 0.0f}, 0.25f});

    printf("ids %d %d %d %d\n", sensorShapeId.index1, sensorShapeId.generation,
           visitorShapeId.index1, visitorShapeId.generation);

    b2World_Step(worldId, 1.0f / 60.0f, 4);
    print_sensor_events("step1", worldId);
    b2ShapeId overlaps[4];
    int overlapCount = b2Shape_GetSensorOverlaps(sensorShapeId, overlaps, 4);
    printf("step1 overlaps %d %d %d %d\n", b2Shape_GetSensorCapacity(sensorShapeId), overlapCount,
           overlapCount > 0 ? overlaps[0].index1 : 0, overlapCount > 0 ? overlaps[0].generation : 0);

    b2World_Step(worldId, 1.0f / 60.0f, 4);
    print_sensor_events("step2", worldId);
    overlapCount = b2Shape_GetSensorOverlaps(sensorShapeId, overlaps, 4);
    printf("step2 overlaps %d %d %d %d\n", b2Shape_GetSensorCapacity(sensorShapeId), overlapCount,
           overlapCount > 0 ? overlaps[0].index1 : 0, overlapCount > 0 ? overlaps[0].generation : 0);

    b2Body_SetTransform(visitorBodyId, (b2Vec2){3.0f, 0.0f}, b2MakeRot(0.0f));
    b2World_Step(worldId, 1.0f / 60.0f, 4);
    print_sensor_events("step3", worldId);
    overlapCount = b2Shape_GetSensorOverlaps(sensorShapeId, overlaps, 4);
    printf("step3 overlaps %d %d\n", b2Shape_GetSensorCapacity(sensorShapeId), overlapCount);

    b2DestroyWorld(worldId);
    return 0;
}
