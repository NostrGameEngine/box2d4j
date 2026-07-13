// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>

static b2WorldId g_worldId;
static b2ShapeId g_protectedShape;
static b2BodyId g_attemptedBody;
static int g_assertionCount;
static int g_callbackCount;
static int g_lockedMoveCount;
static b2TreeStats g_lockedStats;
static float g_lockedMoverFraction;

static int IgnoreAssert(const char* condition, const char* fileName, int lineNumber)
{
    (void)condition;
    (void)fileName;
    (void)lineNumber;
    g_assertionCount += 1;
    return 0;
}

static bool IgnoreOverlap(b2ShapeId shapeId, void* context)
{
    (void)shapeId;
    (void)context;
    return true;
}

static bool LockedFilter(b2ShapeId shapeIdA, b2ShapeId shapeIdB, void* context)
{
    (void)shapeIdA;
    (void)shapeIdB;
    (void)context;
    g_callbackCount += 1;

    b2BodyDef bodyDef = b2DefaultBodyDef();
    g_attemptedBody = b2CreateBody(g_worldId, &bodyDef);
    b2World_Step(g_worldId, 1.0f / 60.0f, 1);
    b2Shape_SetFriction(g_protectedShape, 0.25f);
    g_lockedMoveCount = b2World_GetBodyEvents(g_worldId).moveCount;
    b2AABB aabb = {{-2.0f, -2.0f}, {2.0f, 2.0f}};
    g_lockedStats = b2World_OverlapAABB(g_worldId, aabb, b2DefaultQueryFilter(), IgnoreOverlap, NULL);
    b2Capsule mover = {{-0.5f, 0.0f}, {0.5f, 0.0f}, 0.25f};
    g_lockedMoverFraction = b2World_CastMover(g_worldId, &mover, (b2Vec2){1.0f, 0.0f}, b2DefaultQueryFilter());
    return true;
}

static void RunZeroStep(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId staticBody = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.enableContactEvents = true;
    b2Polygon square = b2MakeSquare(1.0f);
    b2CreatePolygonShape(staticBody, &shapeDef, &square);

    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.gravityScale = 0.0f;
    bodyDef.linearVelocity = (b2Vec2){1.0f, 0.0f};
    b2BodyId dynamicBody = b2CreateBody(worldId, &bodyDef);
    b2CreatePolygonShape(dynamicBody, &shapeDef, &square);

    int before = b2World_GetCounters(worldId).contactCount;
    b2World_Step(worldId, 0.0f, 1);
    int afterFirstZero = b2World_GetCounters(worldId).contactCount;
    b2World_Step(worldId, 1.0f / 60.0f, 1);
    int afterPositive = b2World_GetCounters(worldId).contactCount;
    int positiveMoves = b2World_GetBodyEvents(worldId).moveCount;
    int positiveBegins = b2World_GetContactEvents(worldId).beginCount;
    float beforeSecondZero = b2Body_GetPosition(dynamicBody).x;
    b2World_Step(worldId, 0.0f, 1);
    float afterSecondZero = b2Body_GetPosition(dynamicBody).x;

    printf("zero %d %d %d %d %d %d %d %d %.9g %.9g\n",
           before, afterFirstZero, afterPositive, positiveMoves, positiveBegins,
           b2World_GetBodyEvents(worldId).moveCount, b2World_GetContactEvents(worldId).beginCount,
           b2World_GetSensorEvents(worldId).beginCount, beforeSecondZero, afterSecondZero);
    b2DestroyWorld(worldId);
}

static void RunLockedCallback(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    g_worldId = b2CreateWorld(&worldDef);
    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId staticBody = b2CreateBody(g_worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Polygon square = b2MakeSquare(1.0f);
    g_protectedShape = b2CreatePolygonShape(staticBody, &shapeDef, &square);

    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    b2BodyId dynamicBody = b2CreateBody(g_worldId, &bodyDef);
    b2CreatePolygonShape(dynamicBody, &shapeDef, &square);

    b2SetAssertFcn(IgnoreAssert);
    b2World_SetCustomFilterCallback(g_worldId, LockedFilter, NULL);
    b2World_Step(g_worldId, 1.0f / 60.0f, 1);

    printf("locked %d %d %d %.9g %d %d %d %d %.9g\n",
           g_callbackCount, g_assertionCount, b2Body_IsValid(g_attemptedBody),
           b2Shape_GetFriction(g_protectedShape), g_lockedMoveCount,
           g_lockedStats.nodeVisits, g_lockedStats.leafVisits,
           b2World_GetCounters(g_worldId).contactCount, g_lockedMoverFraction);
    b2DestroyWorld(g_worldId);
}

static void CreateTelemetryBody(b2WorldId worldId, b2BodyType type, float x, float y)
{
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = type;
    bodyDef.position = (b2Vec2){x, y};
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Polygon square = b2MakeSquare(0.5f);
    b2CreatePolygonShape(bodyId, &shapeDef, &square);
}

static int ProfileMask(b2Profile profile)
{
    int mask = 0;
    mask |= profile.step > 0.0f ? 1 : 0;
    mask |= profile.pairs > 0.0f ? 2 : 0;
    mask |= profile.collide > 0.0f ? 4 : 0;
    mask |= profile.solve > 0.0f ? 8 : 0;
    mask |= profile.prepareStages > 0.0f ? 16 : 0;
    mask |= profile.solveConstraints > 0.0f ? 32 : 0;
    mask |= profile.transforms > 0.0f ? 64 : 0;
    mask |= profile.hitEvents > 0.0f ? 128 : 0;
    mask |= profile.refit > 0.0f ? 256 : 0;
    mask |= profile.sleepIslands > 0.0f ? 512 : 0;
    mask |= profile.sensors > 0.0f ? 1024 : 0;
    mask |= profile.mergeIslands > 0.0f ? 2048 : 0;
    mask |= profile.prepareConstraints > 0.0f ? 4096 : 0;
    mask |= profile.integrateVelocities > 0.0f ? 8192 : 0;
    mask |= profile.warmStart > 0.0f ? 16384 : 0;
    mask |= profile.solveImpulses > 0.0f ? 32768 : 0;
    mask |= profile.integratePositions > 0.0f ? 65536 : 0;
    mask |= profile.relaxImpulses > 0.0f ? 131072 : 0;
    mask |= profile.applyRestitution > 0.0f ? 262144 : 0;
    mask |= profile.storeImpulses > 0.0f ? 524288 : 0;
    return mask;
}

static void RunTelemetry(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = b2Vec2_zero;
    b2WorldId worldId = b2CreateWorld(&worldDef);
    for (int i = 0; i < 7; ++i)
    {
        CreateTelemetryBody(worldId, b2_staticBody, -18.0f + 3.0f * i, 0.0f);
    }
    for (int i = 0; i < 5; ++i)
    {
        CreateTelemetryBody(worldId, b2_dynamicBody, -12.0f + 3.0f * i, 4.0f);
    }
    for (int i = 0; i < 3; ++i)
    {
        CreateTelemetryBody(worldId, b2_kinematicBody, -6.0f + 3.0f * i, 8.0f);
    }

    b2Counters before = b2World_GetCounters(worldId);
    b2World_Step(worldId, 1.0f / 60.0f, 1);
    b2Counters after = b2World_GetCounters(worldId);
    b2Profile profile = b2World_GetProfile(worldId);
    printf("telemetry %d %d %d %d %d %d %d\n",
           before.staticTreeHeight, before.treeHeight, after.staticTreeHeight, after.treeHeight,
           after.taskCount, after.stackUsed, ProfileMask(profile));
    b2DestroyWorld(worldId);
}

int main(void)
{
    RunZeroStep();
    RunLockedCallback();
    RunTelemetry();
    return 0;
}
