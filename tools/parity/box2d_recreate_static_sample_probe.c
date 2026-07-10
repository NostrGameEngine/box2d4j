// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){0.0f, 1.0f};
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Polygon box = b2MakeBox(1.0f, 1.0f);
    b2CreatePolygonShape(bodyId, &shapeDef, &box);

    b2BodyId groundId = b2_nullBodyId;
    for (int step = 0; step < 120; ++step)
    {
        if (B2_IS_NON_NULL(groundId))
        {
            b2DestroyBody(groundId);
            groundId = b2_nullBodyId;
        }

        b2BodyDef groundDef = b2DefaultBodyDef();
        groundId = b2CreateBody(worldId, &groundDef);

        b2ShapeDef groundShapeDef = b2DefaultShapeDef();
        groundShapeDef.invokeContactCreation = true;
        b2Segment segment = {{-10.0f, 0.0f}, {10.0f, 0.0f}};
        b2CreateSegmentShape(groundId, &groundShapeDef, &segment);

        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Vec2 p = b2Body_GetPosition(bodyId);
    b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
    b2ContactData contactData[4];
    int bodyContactCount = b2Body_GetContactData(bodyId, contactData, 4);
    b2Counters counters = b2World_GetCounters(worldId);

    printf("recreateStatic %.9g %.9g %.9g %.9g %.9g %.9g %d %d %d %d %d\n",
           p.x,
           p.y,
           b2Rot_GetAngle(b2Body_GetRotation(bodyId)),
           v.x,
           v.y,
           b2Body_GetAngularVelocity(bodyId),
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           bodyContactCount,
           b2World_GetAwakeBodyCount(worldId));

    b2DestroyWorld(worldId);
    return 0;
}
