// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = (b2Vec2){0.0f, -10.0f};
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef groundBodyDef = b2DefaultBodyDef();
    groundBodyDef.position = (b2Vec2){0.0f, -10.0f};
    b2BodyId groundId = b2CreateBody(worldId, &groundBodyDef);
    b2Polygon groundBox = b2MakeBox(50.0f, 10.0f);
    b2ShapeDef groundShapeDef = b2DefaultShapeDef();
    groundShapeDef.enableContactEvents = true;
    b2ShapeId groundShapeId = b2CreatePolygonShape(groundId, &groundShapeDef, &groundBox);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){0.0f, 4.0f};
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    b2Polygon dynamicBox = b2MakeBox(1.0f, 1.0f);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.0f;
    shapeDef.material.friction = 0.3f;
    shapeDef.enableContactEvents = true;
    b2ShapeId dynamicShapeId = b2CreatePolygonShape(bodyId, &shapeDef, &dynamicBox);

    int beginTotal = 0;
    int endTotal = 0;
    for (int i = 0; i < 90; ++i)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        b2ContactEvents events = b2World_GetContactEvents(worldId);
        beginTotal += events.beginCount;
        endTotal += events.endCount;
    }

    int bodyCapacity = b2Body_GetContactCapacity(bodyId);
    int dynamicShapeCapacity = b2Shape_GetContactCapacity(dynamicShapeId);
    int groundShapeCapacity = b2Shape_GetContactCapacity(groundShapeId);
    b2ContactData data[4] = {0};
    int bodyCount = b2Body_GetContactData(bodyId, data, 4);
    int pointCount = bodyCount > 0 ? data[0].manifold.pointCount : 0;

    printf("contacts %d %d %d %d %d %d\n", bodyCapacity, dynamicShapeCapacity, groundShapeCapacity, bodyCount, beginTotal,
           endTotal);
    printf("manifold %d\n", pointCount);

    b2DestroyWorld(worldId);
    return 0;
}
