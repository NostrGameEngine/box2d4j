// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>

typedef struct PreSolveState
{
    int callbackCount;
    int pointCount;
    int shapeA;
    int shapeB;
} PreSolveState;

static bool RejectToi(b2ShapeId shapeIdA, b2ShapeId shapeIdB, b2Manifold* manifold, void* context)
{
    PreSolveState* state = context;
    state->callbackCount += 1;
    state->pointCount = manifold->pointCount;
    state->shapeA = shapeIdA.index1;
    state->shapeB = shapeIdB.index1;
    return false;
}

static void MovingKinematic(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = b2Vec2_zero;
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyDef targetDef = b2DefaultBodyDef();
    targetDef.type = b2_kinematicBody;
    targetDef.linearVelocity = (b2Vec2){-5.0f, 0.0f};
    b2BodyId targetId = b2CreateBody(worldId, &targetDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Polygon targetBox = b2MakeBox(0.25f, 2.0f);
    b2CreatePolygonShape(targetId, &shapeDef, &targetBox);

    b2BodyDef bulletDef = b2DefaultBodyDef();
    bulletDef.type = b2_dynamicBody;
    bulletDef.isBullet = true;
    bulletDef.position = (b2Vec2){-5.0f, 0.0f};
    bulletDef.linearVelocity = (b2Vec2){20.0f, 0.0f};
    b2BodyId bulletId = b2CreateBody(worldId, &bulletDef);
    shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.0f;
    b2Circle bulletCircle = {{0.0f, 0.0f}, 0.25f};
    b2CreateCircleShape(bulletId, &shapeDef, &bulletCircle);

    b2World_Step(worldId, 0.25f, 1);
    printf("kinematic %.9g %.9g %.9g %.9g %d\n",
           b2Body_GetPosition(bulletId).x, b2Body_GetPosition(targetId).x,
           b2Body_GetLinearVelocity(bulletId).x, b2Body_GetLinearVelocity(targetId).x,
           b2World_GetCounters(worldId).contactCount);
    b2DestroyWorld(worldId);
}

static void ToiPreSolve(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = b2Vec2_zero;
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyDef wallDef = b2DefaultBodyDef();
    b2BodyId wallId = b2CreateBody(worldId, &wallDef);
    b2ShapeDef wallShapeDef = b2DefaultShapeDef();
    wallShapeDef.enablePreSolveEvents = true;
    b2Polygon wallBox = b2MakeBox(0.1f, 2.0f);
    b2CreatePolygonShape(wallId, &wallShapeDef, &wallBox);

    b2BodyDef bulletDef = b2DefaultBodyDef();
    bulletDef.type = b2_dynamicBody;
    bulletDef.isBullet = true;
    bulletDef.position = (b2Vec2){-5.0f, 0.0f};
    bulletDef.linearVelocity = (b2Vec2){40.0f, 0.0f};
    b2BodyId bulletId = b2CreateBody(worldId, &bulletDef);
    b2ShapeDef bulletShapeDef = b2DefaultShapeDef();
    bulletShapeDef.density = 1.0f;
    b2Circle bulletCircle = {{0.0f, 0.0f}, 0.25f};
    b2CreateCircleShape(bulletId, &bulletShapeDef, &bulletCircle);

    PreSolveState state = {0};
    b2World_SetPreSolveCallback(worldId, RejectToi, &state);
    b2World_Step(worldId, 0.2f, 1);
    printf("preSolve %d %d %d %d %.9g %.9g %d\n",
           state.callbackCount, state.pointCount, state.shapeA, state.shapeB,
           b2Body_GetPosition(bulletId).x, b2Body_GetLinearVelocity(bulletId).x,
           b2World_GetCounters(worldId).contactCount);
    b2DestroyWorld(worldId);
}

int main(void)
{
    MovingKinematic();
    ToiPreSolve();
    return 0;
}
