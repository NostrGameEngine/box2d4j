// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>
#include <stdlib.h>

enum
{
    DEFAULT_STEP_COUNT = 120,
    GROUND = 0x00000001,
    SENSOR = 0x00000002,
    DEFAULT = 0x00000004
};

typedef struct Scene
{
    b2ShapeId staticSensorId;
    b2ShapeId kinematicSensorId;
    b2ShapeId dynamicSensorId;
    b2BodyId kinematicBodyId;
    b2BodyId dynamicBodyId;
    b2BodyId ballBodyId;
} Scene;

static void print_sensor(b2ShapeId sensorId)
{
    int capacity = b2Shape_GetSensorCapacity(sensorId);
    b2ShapeId overlaps[16];
    int count = b2Shape_GetSensorOverlaps(sensorId, overlaps, capacity);
    int indexSum = 0;
    int generationSum = 0;
    for (int i = 0; i < count; ++i)
    {
        indexSum += overlaps[i].index1;
        generationSum += overlaps[i].generation;
    }
    printf(" %d %d %d %d", capacity, count, indexSum, generationSum);
}

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

static void print_ray(b2RayResult ray)
{
    printf(" %d %.9g %.9g %.9g %.9g %.9g %d %d %d %d",
           ray.hit ? 1 : 0,
           ray.fraction,
           ray.point.x,
           ray.point.y,
           ray.normal.x,
           ray.normal.y,
           ray.shapeId.index1,
           ray.shapeId.generation,
           ray.nodeVisits,
           ray.leafVisits);
}

static Scene create_scene(b2WorldId worldId)
{
    Scene scene = {0};

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.name = "ground";
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.filter.categoryBits = GROUND;
    shapeDef.filter.maskBits = DEFAULT;
    shapeDef.enableSensorEvents = true;

    b2Segment groundSegment = {{-6.0f, 0.0f}, {6.0f, 0.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &groundSegment);
    groundSegment = (b2Segment){{-6.0f, 0.0f}, {-6.0f, 4.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &groundSegment);
    groundSegment = (b2Segment){{6.0f, 0.0f}, {6.0f, 4.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &groundSegment);

    bodyDef = b2DefaultBodyDef();
    bodyDef.name = "static sensor";
    bodyDef.type = b2_staticBody;
    bodyDef.position = (b2Vec2){-3.0f, 0.8f};
    b2BodyId staticBodyId = b2CreateBody(worldId, &bodyDef);

    shapeDef = b2DefaultShapeDef();
    shapeDef.filter.categoryBits = SENSOR;
    shapeDef.isSensor = true;
    shapeDef.enableSensorEvents = true;
    b2Polygon box = b2MakeSquare(1.0f);
    scene.staticSensorId = b2CreatePolygonShape(staticBodyId, &shapeDef, &box);

    bodyDef = b2DefaultBodyDef();
    bodyDef.name = "kinematic sensor";
    bodyDef.type = b2_kinematicBody;
    bodyDef.position = (b2Vec2){0.0f, 0.0f};
    bodyDef.linearVelocity = (b2Vec2){0.0f, 1.0f};
    scene.kinematicBodyId = b2CreateBody(worldId, &bodyDef);

    shapeDef = b2DefaultShapeDef();
    shapeDef.filter.categoryBits = SENSOR;
    shapeDef.isSensor = true;
    shapeDef.enableSensorEvents = true;
    box = b2MakeSquare(1.0f);
    scene.kinematicSensorId = b2CreatePolygonShape(scene.kinematicBodyId, &shapeDef, &box);

    bodyDef = b2DefaultBodyDef();
    bodyDef.name = "dynamic sensor";
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){3.0f, 1.0f};
    scene.dynamicBodyId = b2CreateBody(worldId, &bodyDef);

    shapeDef = b2DefaultShapeDef();
    shapeDef.filter.categoryBits = SENSOR;
    shapeDef.isSensor = true;
    shapeDef.enableSensorEvents = true;
    box = b2MakeSquare(1.0f);
    scene.dynamicSensorId = b2CreatePolygonShape(scene.dynamicBodyId, &shapeDef, &box);

    shapeDef.filter.categoryBits = DEFAULT;
    shapeDef.isSensor = false;
    shapeDef.enableSensorEvents = false;
    box = b2MakeSquare(0.8f);
    b2CreatePolygonShape(scene.dynamicBodyId, &shapeDef, &box);

    bodyDef = b2DefaultBodyDef();
    bodyDef.name = "ball_01";
    bodyDef.position = (b2Vec2){-5.0f, 1.0f};
    bodyDef.type = b2_dynamicBody;
    scene.ballBodyId = b2CreateBody(worldId, &bodyDef);

    shapeDef = b2DefaultShapeDef();
    shapeDef.filter.categoryBits = DEFAULT;
    shapeDef.filter.maskBits = GROUND | DEFAULT | SENSOR;
    shapeDef.enableSensorEvents = true;
    b2Circle circle = {{0.0f, 0.0f}, 0.5f};
    b2CreateCircleShape(scene.ballBodyId, &shapeDef, &circle);

    return scene;
}

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : DEFAULT_STEP_COUNT;

    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    Scene scene = create_scene(worldId);

    int beginTotal = 0;
    int endTotal = 0;
    b2RayResult ray = {0};
    for (int step = 0; step < stepCount; ++step)
    {
        b2Vec2 position = b2Body_GetPosition(scene.kinematicBodyId);
        if (position.y < 0.0f)
        {
            b2Body_SetLinearVelocity(scene.kinematicBodyId, (b2Vec2){0.0f, 1.0f});
        }
        else if (position.y > 3.0f)
        {
            b2Body_SetLinearVelocity(scene.kinematicBodyId, (b2Vec2){0.0f, -1.0f});
        }

        b2World_Step(worldId, 1.0f / 60.0f, 4);
        b2SensorEvents sensorEvents = b2World_GetSensorEvents(worldId);
        beginTotal += sensorEvents.beginCount;
        endTotal += sensorEvents.endCount;
        ray = b2World_CastRayClosest(worldId, (b2Vec2){5.0f, 1.0f}, (b2Vec2){-10.0f, 0.0f}, b2DefaultQueryFilter());
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("sensorTypes %d %d %d %d %d %d %d",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           counters.jointCount,
           b2World_GetAwakeBodyCount(worldId),
           beginTotal,
           endTotal);
    print_sensor(scene.staticSensorId);
    print_sensor(scene.kinematicSensorId);
    print_sensor(scene.dynamicSensorId);
    print_body(scene.kinematicBodyId);
    print_body(scene.dynamicBodyId);
    print_body(scene.ballBodyId);
    print_ray(ray);
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
