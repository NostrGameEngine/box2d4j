// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>
#include <stdlib.h>

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : 120;
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    float extent = 1.0f;

    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);

    float groundWidth = 66.0f * extent;
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Segment segment = {
        {-0.5f * 2.0f * groundWidth, 0.0f},
        {0.5f * 2.0f * groundWidth, 0.0f},
    };
    b2CreateSegmentShape(groundId, &shapeDef, &segment);

    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){0.0f, 1.0f};
    bodyDef.linearVelocity = (b2Vec2){5.0f, 0.0f};

    b2Polygon box = b2MakeBox(extent, extent);
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    b2CreatePolygonShape(bodyId, &shapeDef, &box);

    for (int i = 0; i < stepCount; ++i)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Vec2 p = b2Body_GetPosition(bodyId);
    b2Rot q = b2Body_GetRotation(bodyId);
    b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
    b2ContactData contactData[4];
    int contactCount = b2Body_GetContactData(bodyId, contactData, 4);

    printf("singleBox %.9g %.9g %.9g %.9g %.9g %.9g %d %d\n",
           p.x,
           p.y,
           b2Rot_GetAngle(q),
           v.x,
           v.y,
           b2Body_GetAngularVelocity(bodyId),
           contactCount,
           b2World_GetAwakeBodyCount(worldId));

    b2DestroyWorld(worldId);
    return 0;
}
