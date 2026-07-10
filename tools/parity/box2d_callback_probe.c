// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>

static int filterCalls = 0;
static int preSolveCalls = 0;
static int preSolvePointCount = 0;

static bool custom_filter(b2ShapeId shapeIdA, b2ShapeId shapeIdB, void* context)
{
    (void)shapeIdA;
    (void)shapeIdB;
    (void)context;
    filterCalls += 1;
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
    printf("filter %d %d %d %d\n", filterCalls, counters.contactCount, events.beginCount, events.endCount);
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

    return 0;
}
