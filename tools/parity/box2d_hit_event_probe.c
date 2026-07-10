// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = (b2Vec2){0.0f, 0.0f};
    worldDef.hitEventThreshold = 0.1f;
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef groundDef = b2DefaultBodyDef();
    groundDef.position = (b2Vec2){0.0f, -0.5f};
    b2BodyId groundId = b2CreateBody(worldId, &groundDef);
    b2ShapeDef groundShapeDef = b2DefaultShapeDef();
    b2Polygon groundBox = b2MakeBox(2.0f, 0.5f);
    b2ShapeId groundShapeId = b2CreatePolygonShape(groundId, &groundShapeDef, &groundBox);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){0.0f, 0.45f};
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    b2Body_SetLinearVelocity(bodyId, (b2Vec2){0.0f, -3.0f});
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.0f;
    shapeDef.enableHitEvents = true;
    b2Polygon box = b2MakeBox(0.4f, 0.4f);
    b2ShapeId dynamicShapeId = b2CreatePolygonShape(bodyId, &shapeDef, &box);

    b2World_Step(worldId, 1.0f / 60.0f, 4);
    b2ContactEvents events = b2World_GetContactEvents(worldId);
    printf("counts %d %d %d\n", events.beginCount, events.endCount, events.hitCount);
    if (events.hitCount > 0)
    {
        b2ContactHitEvent event = events.hitEvents[0];
        printf("hit %d %d %d %d %.9g %.9g %.9g %.9g %.9g\n",
               event.shapeIdA.index1, event.shapeIdA.generation,
               event.shapeIdB.index1, event.shapeIdB.generation,
               event.point.x, event.point.y, event.normal.x, event.normal.y,
               event.approachSpeed);
    }
    printf("ids %d %d\n", groundShapeId.index1, dynamicShapeId.index1);

    b2DestroyWorld(worldId);
    return 0;
}
