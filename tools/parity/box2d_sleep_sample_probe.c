// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>
#include <stdlib.h>

enum
{
    DEFAULT_STEP_COUNT = 20,
    CREATE_INVOKER_STEP = 10,
    DESTROY_INVOKER_STEP = 15
};

typedef struct Scene
{
    b2ShapeId groundShapeId;
    b2ShapeId sensorIds[2];
    b2BodyId bodies[7];
    bool sensorTouching[2];
    b2BodyId staticBodyId;
} Scene;

static void toggle_invoker(b2WorldId worldId, Scene* scene)
{
    if (B2_IS_NULL(scene->staticBodyId))
    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.position = (b2Vec2){-10.5f, 3.0f};
        scene->staticBodyId = b2CreateBody(worldId, &bodyDef);

        b2Polygon box = b2MakeOffsetBox(2.0f, 0.1f, (b2Vec2){0.0f, 0.0f}, b2MakeRot(0.25f * B2_PI));
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.invokeContactCreation = true;
        b2CreatePolygonShape(scene->staticBodyId, &shapeDef, &box);
    }
    else
    {
        b2DestroyBody(scene->staticBodyId);
        scene->staticBodyId = b2_nullBodyId;
    }
}

static void update_sensor_touching(Scene* scene, b2SensorEvents sensorEvents)
{
    for (int i = 0; i < sensorEvents.beginCount; ++i)
    {
        b2SensorBeginTouchEvent* event = sensorEvents.beginEvents + i;
        if (B2_ID_EQUALS(event->visitorShapeId, scene->groundShapeId))
        {
            if (B2_ID_EQUALS(event->sensorShapeId, scene->sensorIds[0]))
            {
                scene->sensorTouching[0] = true;
            }
            else if (B2_ID_EQUALS(event->sensorShapeId, scene->sensorIds[1]))
            {
                scene->sensorTouching[1] = true;
            }
        }
    }

    for (int i = 0; i < sensorEvents.endCount; ++i)
    {
        b2SensorEndTouchEvent* event = sensorEvents.endEvents + i;
        if (B2_ID_EQUALS(event->visitorShapeId, scene->groundShapeId))
        {
            if (B2_ID_EQUALS(event->sensorShapeId, scene->sensorIds[0]))
            {
                scene->sensorTouching[0] = false;
            }
            else if (B2_ID_EQUALS(event->sensorShapeId, scene->sensorIds[1]))
            {
                scene->sensorTouching[1] = false;
            }
        }
    }
}

static void create_scene(b2WorldId worldId, Scene* scene)
{
    *scene = (Scene){0};
    scene->staticBodyId = b2_nullBodyId;

    b2BodyId groundId;
    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        groundId = b2CreateBody(worldId, &bodyDef);

        b2Segment segment = {{-40.0f, 0.0f}, {40.0f, 0.0f}};
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.enableSensorEvents = true;
        scene->groundShapeId = b2CreateSegmentShape(groundId, &shapeDef, &segment);
    }

    for (int i = 0; i < 2; ++i)
    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = (b2Vec2){-4.0f, 3.0f + 2.0f * i};
        bodyDef.isAwake = false;
        bodyDef.enableSleep = true;
        scene->bodies[i] = b2CreateBody(worldId, &bodyDef);

        b2Capsule capsule = {{0.0f, 1.0f}, {1.0f, 1.0f}, 0.75f};
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreateCapsuleShape(scene->bodies[i], &shapeDef, &capsule);

        shapeDef.isSensor = true;
        shapeDef.enableSensorEvents = true;
        capsule.radius = 1.0f;
        scene->sensorIds[i] = b2CreateCapsuleShape(scene->bodies[i], &shapeDef, &capsule);
    }

    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = (b2Vec2){0.0f, 3.0f};
        bodyDef.isAwake = false;
        bodyDef.enableSleep = false;
        scene->bodies[2] = b2CreateBody(worldId, &bodyDef);

        b2Circle circle = {{1.0f, 1.0f}, 1.0f};
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreateCircleShape(scene->bodies[2], &shapeDef, &circle);
    }

    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = (b2Vec2){5.0f, 3.0f};
        bodyDef.isAwake = true;
        bodyDef.enableSleep = false;
        scene->bodies[3] = b2CreateBody(worldId, &bodyDef);

        b2Polygon box = b2MakeOffsetBox(1.0f, 1.0f, (b2Vec2){0.0f, 1.0f}, b2MakeRot(0.25f * B2_PI));
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreatePolygonShape(scene->bodies[3], &shapeDef, &box);
    }

    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = (b2Vec2){5.0f, 1.0f};
        bodyDef.isAwake = false;
        bodyDef.enableSleep = true;
        scene->bodies[4] = b2CreateBody(worldId, &bodyDef);

        b2Polygon box = b2MakeSquare(1.0f);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreatePolygonShape(scene->bodies[4], &shapeDef, &box);
    }

    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = (b2Vec2){0.0f, 100.0f};
        bodyDef.angularDamping = 0.5f;
        bodyDef.sleepThreshold = 0.05f;
        scene->bodies[5] = b2CreateBody(worldId, &bodyDef);

        b2Capsule capsule = {{0.0f, 0.0f}, {90.0f, 0.0f}, 0.25f};
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreateCapsuleShape(scene->bodies[5], &shapeDef, &capsule);

        b2Vec2 pivot = bodyDef.position;
        b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
        jointDef.bodyIdA = groundId;
        jointDef.bodyIdB = scene->bodies[5];
        jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
        jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
        b2CreateRevoluteJoint(worldId, &jointDef);
    }

    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = (b2Vec2){-10.0f, 1.0f};
        bodyDef.isAwake = false;
        bodyDef.enableSleep = true;
        scene->bodies[6] = b2CreateBody(worldId, &bodyDef);

        b2Polygon box = b2MakeSquare(1.0f);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreatePolygonShape(scene->bodies[6], &shapeDef, &box);
    }
}

static void print_body(b2BodyId bodyId)
{
    b2Vec2 p = b2Body_GetPosition(bodyId);
    b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
    printf(" %d %d %d %d %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g",
           b2Body_IsAwake(bodyId) ? 1 : 0,
           b2Body_IsSleepEnabled(bodyId) ? 1 : 0,
           b2Body_IsEnabled(bodyId) ? 1 : 0,
           b2Body_GetContactCapacity(bodyId),
           p.x,
           p.y,
           b2Rot_GetAngle(b2Body_GetRotation(bodyId)),
           v.x,
           v.y,
           b2Body_GetAngularVelocity(bodyId),
           b2Body_GetSleepThreshold(bodyId),
           b2Body_GetAngularDamping(bodyId));
}

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : DEFAULT_STEP_COUNT;
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    Scene scene;
    create_scene(worldId, &scene);

    int beginCount = 0;
    int endCount = 0;
    for (int step = 0; step < stepCount; ++step)
    {
        if (step == CREATE_INVOKER_STEP)
        {
            toggle_invoker(worldId, &scene);
        }
        else if (step == DESTROY_INVOKER_STEP)
        {
            toggle_invoker(worldId, &scene);
        }

        b2World_Step(worldId, 1.0f / 60.0f, 4);
        b2SensorEvents sensorEvents = b2World_GetSensorEvents(worldId);
        beginCount += sensorEvents.beginCount;
        endCount += sensorEvents.endCount;
        update_sensor_touching(&scene, sensorEvents);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("sleep %d %d %d %d %d %d %d %d %d 7",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           b2World_GetAwakeBodyCount(worldId),
           beginCount,
           endCount,
           scene.sensorTouching[0] ? 1 : 0,
           scene.sensorTouching[1] ? 1 : 0,
           B2_IS_NULL(scene.staticBodyId) ? 0 : 1);
    for (int i = 0; i < 7; ++i)
    {
        print_body(scene.bodies[i]);
    }
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
