// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdint.h>
#include <stdio.h>

static int filterCalls = 0;
static int filterShapeA = 0;
static int filterShapeB = 0;
static int preSolveCalls = 0;
static int preSolvePointCount = 0;
static int frictionMixCalls = 0;
static int restitutionMixCalls = 0;
static uint32_t frictionMixArgs[4];
static uint32_t restitutionMixArgs[4];

static uint32_t float_bits(float value)
{
    union
    {
        float f;
        uint32_t u;
    } bits = {value};
    return bits.u;
}

static float friction_mix(float frictionA, int materialA, float frictionB, int materialB)
{
    frictionMixCalls += 1;
    frictionMixArgs[0] = float_bits(frictionA);
    frictionMixArgs[1] = (uint32_t)materialA;
    frictionMixArgs[2] = float_bits(frictionB);
    frictionMixArgs[3] = (uint32_t)materialB;
    return 0.25f;
}

static float restitution_mix(float restitutionA, int materialA, float restitutionB, int materialB)
{
    restitutionMixCalls += 1;
    restitutionMixArgs[0] = float_bits(restitutionA);
    restitutionMixArgs[1] = (uint32_t)materialA;
    restitutionMixArgs[2] = float_bits(restitutionB);
    restitutionMixArgs[3] = (uint32_t)materialB;
    return 0.0f;
}

static void print_mix(const char* label)
{
    printf("%s %d %d %08x %u %08x %u %08x %u %08x %u\n", label,
           frictionMixCalls, restitutionMixCalls,
           frictionMixArgs[0], frictionMixArgs[1], frictionMixArgs[2], frictionMixArgs[3],
           restitutionMixArgs[0], restitutionMixArgs[1], restitutionMixArgs[2], restitutionMixArgs[3]);
}

static bool custom_filter(b2ShapeId shapeIdA, b2ShapeId shapeIdB, void* context)
{
    (void)context;
    filterCalls += 1;
    filterShapeA = shapeIdA.index1;
    filterShapeB = shapeIdB.index1;
    return false;
}

static bool pre_solve(b2ShapeId shapeIdA, b2ShapeId shapeIdB, b2Manifold* manifold, void* context)
{
    (void)shapeIdA;
    (void)shapeIdB;
    (void)context;
    preSolveCalls += 1;
    preSolvePointCount = manifold->pointCount;
    return false;
}

static b2BodyId create_box_body(b2WorldId worldId, b2Vec2 position, bool enablePreSolve)
{
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = position;
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.0f;
    shapeDef.enablePreSolveEvents = enablePreSolve;
    b2Polygon box = b2MakeBox(0.5f, 0.5f);
    b2CreatePolygonShape(bodyId, &shapeDef, &box);
    return bodyId;
}

static void run_pre_solve_capture(const b2WorldDef* worldDef, bool initialFlag)
{
    preSolveCalls = 0;
    preSolvePointCount = 0;
    b2WorldId worldId = b2CreateWorld(worldDef);
    b2World_SetPreSolveCallback(worldId, pre_solve, NULL);

    b2BodyDef groundDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &groundDef);
    b2ShapeDef groundShapeDef = b2DefaultShapeDef();
    b2Polygon groundBox = b2MakeBox(2.0f, 0.5f);
    b2CreatePolygonShape(groundId, &groundShapeDef, &groundBox);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){0.0f, 1.08f};
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.enablePreSolveEvents = initialFlag;
    b2Circle circle = {{0.0f, 0.0f}, 0.5f};
    b2ShapeId shapeId = b2CreateCircleShape(bodyId, &shapeDef, &circle);

    b2World_Step(worldId, 1.0f / 60.0f, 4);
    b2Shape_EnablePreSolveEvents(shapeId, !initialFlag);
    b2Body_SetTransform(bodyId, (b2Vec2){0.0f, 0.9f}, b2Rot_identity);
    b2World_Step(worldId, 1.0f / 60.0f, 4);
    printf("capture %d %d %d\n", initialFlag ? 1 : 0, preSolveCalls, preSolvePointCount);
    b2DestroyWorld(worldId);
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = (b2Vec2){0.0f, 0.0f};

    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2World_SetCustomFilterCallback(worldId, custom_filter, NULL);
    create_box_body(worldId, (b2Vec2){0.0f, 0.0f}, false);
    create_box_body(worldId, (b2Vec2){0.25f, 0.0f}, false);
    b2World_Step(worldId, 1.0f / 60.0f, 4);
    b2Counters counters = b2World_GetCounters(worldId);
    b2ContactEvents events = b2World_GetContactEvents(worldId);
    printf("filter %d %d %d %d %d %d\n", filterCalls, filterShapeA, filterShapeB,
           counters.contactCount, events.beginCount, events.endCount);
    b2DestroyWorld(worldId);

    filterCalls = 0;
    worldId = b2CreateWorld(&worldDef);
    b2World_SetCustomFilterCallback(worldId, custom_filter, NULL);
    b2BodyDef sensorBodyDef = b2DefaultBodyDef();
    b2BodyId sensorBodyId = b2CreateBody(worldId, &sensorBodyDef);
    b2ShapeDef sensorDef = b2DefaultShapeDef();
    sensorDef.isSensor = true;
    b2Circle sensorCircle = {{0.0f, 0.0f}, 1.0f};
    b2CreateCircleShape(sensorBodyId, &sensorDef, &sensorCircle);
    create_box_body(worldId, (b2Vec2){0.0f, 0.0f}, false);
    b2World_Step(worldId, 1.0f / 60.0f, 4);
    printf("sensor_filter %d %d\n", filterCalls, b2World_GetCounters(worldId).contactCount);
    b2DestroyWorld(worldId);

    worldId = b2CreateWorld(&worldDef);
    b2World_SetPreSolveCallback(worldId, pre_solve, NULL);
    b2BodyId bodyA = create_box_body(worldId, (b2Vec2){0.0f, 0.0f}, true);
    b2BodyId bodyB = create_box_body(worldId, (b2Vec2){0.25f, 0.0f}, false);
    b2World_Step(worldId, 1.0f / 60.0f, 4);
    counters = b2World_GetCounters(worldId);
    events = b2World_GetContactEvents(worldId);
    b2ContactData data[2];
    int contactDataCount = b2Body_GetContactData(bodyA, data, 2);
    printf("presolve %d %d %d %d %d %d %d %d\n", preSolveCalls, preSolvePointCount,
           counters.contactCount, events.beginCount, events.endCount,
           b2Body_GetContactCapacity(bodyA), b2Body_GetContactCapacity(bodyB), contactDataCount);
    b2DestroyWorld(worldId);

    run_pre_solve_capture(&worldDef, false);
    run_pre_solve_capture(&worldDef, true);

    worldId = b2CreateWorld(&worldDef);
    b2World_SetFrictionCallback(worldId, friction_mix);
    b2World_SetRestitutionCallback(worldId, restitution_mix);

    b2BodyDef groundDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &groundDef);
    b2ShapeDef groundShapeDef = b2DefaultShapeDef();
    groundShapeDef.material.friction = 0.2f;
    groundShapeDef.material.restitution = 0.1f;
    groundShapeDef.material.userMaterialId = 11;
    b2Polygon groundBox = b2MakeBox(1.0f, 1.0f);
    b2CreatePolygonShape(groundId, &groundShapeDef, &groundBox);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){0.0f, 1.5f};
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.0f;
    shapeDef.material.friction = 0.8f;
    shapeDef.material.restitution = 0.6f;
    shapeDef.material.userMaterialId = 22;
    b2Polygon box = b2MakeBox(1.0f, 1.0f);
    b2ShapeId shapeId = b2CreatePolygonShape(bodyId, &shapeDef, &box);

    b2World_Step(worldId, 1.0f / 60.0f, 4);
    print_mix("mix1");

    b2SurfaceMaterial material = b2Shape_GetSurfaceMaterial(shapeId);
    material.friction = 0.45f;
    material.restitution = 0.75f;
    material.userMaterialId = 33;
    b2Shape_SetSurfaceMaterial(shapeId, material);
    b2World_Step(worldId, 1.0f / 60.0f, 4);
    print_mix("mix2");

    b2World_SetFrictionCallback(worldId, NULL);
    b2World_SetRestitutionCallback(worldId, NULL);
    b2World_Step(worldId, 1.0f / 60.0f, 4);
    printf("mix3 %d %d\n", frictionMixCalls, restitutionMixCalls);
    b2DestroyWorld(worldId);

    return 0;
}
