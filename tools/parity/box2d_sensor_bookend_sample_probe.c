// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>
#include <stdlib.h>

enum
{
    DEFAULT_STEP_COUNT = 120
};

typedef struct Scene
{
    b2WorldId worldId;
    b2BodyId sensorBodyId1;
    b2ShapeId sensorShapeId1;
    b2BodyId sensorBodyId2;
    b2ShapeId sensorShapeId2;
    b2BodyId visitorBodyId;
    b2ShapeId visitorShapeId;
    bool isVisiting1;
    bool isVisiting2;
    int sensorsOverlapCount;
} Scene;

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

static void create_ground(Scene* scene)
{
    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(scene->worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();

    b2Segment groundSegment = {{-10.0f, 0.0f}, {10.0f, 0.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &groundSegment);
    groundSegment = (b2Segment){{-10.0f, 0.0f}, {-10.0f, 10.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &groundSegment);
    groundSegment = (b2Segment){{10.0f, 0.0f}, {10.0f, 10.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &groundSegment);
}

static void create_sensor1(Scene* scene)
{
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.position = (b2Vec2){-2.0f, 1.0f};
    scene->sensorBodyId1 = b2CreateBody(scene->worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.isSensor = true;
    shapeDef.enableSensorEvents = true;
    b2Polygon box = b2MakeSquare(1.0f);
    scene->sensorShapeId1 = b2CreatePolygonShape(scene->sensorBodyId1, &shapeDef, &box);
}

static void create_sensor2(Scene* scene)
{
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){2.0f, 1.0f};
    scene->sensorBodyId2 = b2CreateBody(scene->worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.isSensor = true;
    shapeDef.enableSensorEvents = true;
    b2Polygon box = b2MakeRoundedBox(0.5f, 0.5f, 0.5f);
    scene->sensorShapeId2 = b2CreatePolygonShape(scene->sensorBodyId2, &shapeDef, &box);

    shapeDef.isSensor = false;
    shapeDef.enableSensorEvents = false;
    box = b2MakeSquare(0.5f);
    b2CreatePolygonShape(scene->sensorBodyId2, &shapeDef, &box);
}

static void create_visitor(Scene* scene)
{
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.position = (b2Vec2){-4.0f, 1.0f};
    bodyDef.type = b2_dynamicBody;
    scene->visitorBodyId = b2CreateBody(scene->worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.enableSensorEvents = true;
    b2Circle circle = {{0.0f, 0.0f}, 0.5f};
    scene->visitorShapeId = b2CreateCircleShape(scene->visitorBodyId, &shapeDef, &circle);
}

static void process_sensor_events(Scene* scene, b2SensorEvents sensorEvents)
{
    for (int i = 0; i < sensorEvents.beginCount; ++i)
    {
        b2SensorBeginTouchEvent event = sensorEvents.beginEvents[i];
        if (B2_ID_EQUALS(event.sensorShapeId, scene->sensorShapeId1))
        {
            if (B2_ID_EQUALS(event.visitorShapeId, scene->visitorShapeId))
            {
                scene->isVisiting1 = true;
            }
            else
            {
                scene->sensorsOverlapCount += 1;
            }
        }
        else if (B2_ID_EQUALS(event.sensorShapeId, scene->sensorShapeId2))
        {
            if (B2_ID_EQUALS(event.visitorShapeId, scene->visitorShapeId))
            {
                scene->isVisiting2 = true;
            }
            else
            {
                scene->sensorsOverlapCount += 1;
            }
        }
    }

    for (int i = 0; i < sensorEvents.endCount; ++i)
    {
        b2SensorEndTouchEvent event = sensorEvents.endEvents[i];
        if (B2_ID_EQUALS(event.sensorShapeId, scene->sensorShapeId1))
        {
            if (B2_ID_EQUALS(event.visitorShapeId, scene->visitorShapeId))
            {
                scene->isVisiting1 = false;
            }
            else
            {
                scene->sensorsOverlapCount -= 1;
            }
        }
        else if (B2_ID_EQUALS(event.sensorShapeId, scene->sensorShapeId2))
        {
            if (B2_ID_EQUALS(event.visitorShapeId, scene->visitorShapeId))
            {
                scene->isVisiting2 = false;
            }
            else
            {
                scene->sensorsOverlapCount -= 1;
            }
        }
    }
}

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : DEFAULT_STEP_COUNT;

    Scene scene = {0};
    b2WorldDef worldDef = b2DefaultWorldDef();
    scene.worldId = b2CreateWorld(&worldDef);
    create_ground(&scene);
    create_sensor1(&scene);
    create_sensor2(&scene);
    create_visitor(&scene);

    int beginTotal = 0;
    int endTotal = 0;
    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(scene.worldId, 1.0f / 60.0f, 4);
        b2SensorEvents sensorEvents = b2World_GetSensorEvents(scene.worldId);
        beginTotal += sensorEvents.beginCount;
        endTotal += sensorEvents.endCount;
        process_sensor_events(&scene, sensorEvents);
    }

    b2ShapeId overlaps[8];
    int sensor1Capacity = b2Shape_GetSensorCapacity(scene.sensorShapeId1);
    int sensor1OverlapCount = b2Shape_GetSensorOverlaps(scene.sensorShapeId1, overlaps, 8);
    int sensor2Capacity = b2Shape_GetSensorCapacity(scene.sensorShapeId2);
    int sensor2OverlapCount = b2Shape_GetSensorOverlaps(scene.sensorShapeId2, overlaps, 8);
    b2Counters counters = b2World_GetCounters(scene.worldId);
    printf("sensorBookend %d %d %d %d %d %d %d %d %d %d %d %d %d %d %d %d %d",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           counters.jointCount,
           b2World_GetAwakeBodyCount(scene.worldId),
           beginTotal,
           endTotal,
           scene.isVisiting1 ? 1 : 0,
           scene.isVisiting2 ? 1 : 0,
           scene.sensorsOverlapCount,
           b2Shape_IsValid(scene.sensorShapeId1) ? 1 : 0,
           b2Shape_IsValid(scene.sensorShapeId2) ? 1 : 0,
           b2Shape_IsValid(scene.visitorShapeId) ? 1 : 0,
           sensor1Capacity,
           sensor1OverlapCount,
           sensor2Capacity,
           sensor2OverlapCount);
    print_body(scene.sensorBodyId2);
    print_body(scene.visitorBodyId);
    printf("\n");

    b2DestroyWorld(scene.worldId);
    return 0;
}
