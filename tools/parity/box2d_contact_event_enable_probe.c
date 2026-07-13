// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>

static void run_case(bool initialFlag)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = b2Vec2_zero;
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef groundDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &groundDef);
    b2Polygon groundBox = b2MakeBox(2.0f, 0.5f);
    b2ShapeDef groundShapeDef = b2DefaultShapeDef();
    b2CreatePolygonShape(groundId, &groundShapeDef, &groundBox);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){0.0f, 1.08f};
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.enableContactEvents = initialFlag;
    b2Circle circle = {{0.0f, 0.0f}, 0.5f};
    b2ShapeId shapeId = b2CreateCircleShape(bodyId, &shapeDef, &circle);

    b2World_Step(worldId, 1.0f / 60.0f, 4);
    int capacityBeforeTouch = b2Body_GetContactCapacity(bodyId);

    b2Shape_EnableContactEvents(shapeId, !initialFlag);
    b2Body_SetTransform(bodyId, (b2Vec2){0.0f, 0.9f}, b2Rot_identity);
    b2World_Step(worldId, 1.0f / 60.0f, 4);
    b2ContactEvents events = b2World_GetContactEvents(worldId);
    int beginCount = events.beginCount;

    b2Body_SetTransform(bodyId, (b2Vec2){0.0f, 2.0f}, b2Rot_identity);
    b2World_Step(worldId, 1.0f / 60.0f, 4);
    events = b2World_GetContactEvents(worldId);
    printf(" %d %d %d %d", initialFlag ? 1 : 0, capacityBeforeTouch, beginCount, events.endCount);

    b2DestroyWorld(worldId);
}

static void run_destroy_case(bool destroyBody)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = b2Vec2_zero;
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef groundDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &groundDef);
    b2Polygon groundBox = b2MakeBox(2.0f, 0.5f);
    b2ShapeDef groundShapeDef = b2DefaultShapeDef();
    b2CreatePolygonShape(groundId, &groundShapeDef, &groundBox);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){0.0f, 0.9f};
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.enableContactEvents = true;
    b2Circle circle = {{0.0f, 0.0f}, 0.5f};
    b2ShapeId shapeId = b2CreateCircleShape(bodyId, &shapeDef, &circle);

    b2World_Step(worldId, 1.0f / 60.0f, 4);
    int beginCount = b2World_GetContactEvents(worldId).beginCount;
    if (destroyBody)
    {
        b2DestroyBody(bodyId);
    }
    else
    {
        b2DestroyShape(shapeId, false);
    }
    int immediateEndCount = b2World_GetContactEvents(worldId).endCount;
    b2World_Step(worldId, 0.0f, 1);
    int deferredEndCount = b2World_GetContactEvents(worldId).endCount;
    printf(" %d %d %d %d", destroyBody ? 1 : 0, beginCount, immediateEndCount, deferredEndCount);

    b2DestroyWorld(worldId);
}

int main(void)
{
    printf("contactEventEnable");
    run_case(false);
    run_case(true);
    run_destroy_case(false);
    run_destroy_case(true);
    printf("\n");
    return 0;
}
