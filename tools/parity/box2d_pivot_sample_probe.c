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

    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(worldId, &bodyDef);

        b2Segment segment = {{-20.0f, 0.0f}, {20.0f, 0.0f}};
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreateSegmentShape(groundId, &shapeDef, &segment);
    }

    b2Vec2 v = {5.0f, 0.0f};
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){0.0f, 3.0f};
    bodyDef.gravityScale = 1.0f;
    bodyDef.linearVelocity = v;

    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    float lever = 3.0f;
    b2Vec2 r = {0.0f, -lever};
    float omega = b2Cross(v, r) / b2Dot(r, r);
    b2Body_SetAngularVelocity(bodyId, omega);

    b2Polygon box = b2MakeBox(0.1f, lever);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2CreatePolygonShape(bodyId, &shapeDef, &box);

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Vec2 p = b2Body_GetPosition(bodyId);
    b2Vec2 linearVelocity = b2Body_GetLinearVelocity(bodyId);
    float angularVelocity = b2Body_GetAngularVelocity(bodyId);
    b2Vec2 worldR = b2Body_GetWorldVector(bodyId, (b2Vec2){0.0f, -lever});
    b2Vec2 pivotVelocity = b2Add(linearVelocity, b2CrossSV(angularVelocity, worldR));
    b2Vec2 localVelocity = b2Body_GetLocalPointVelocity(bodyId, (b2Vec2){0.0f, -lever});
    b2Vec2 worldPoint = b2Body_GetWorldPoint(bodyId, (b2Vec2){0.0f, -lever});
    b2Vec2 worldVelocity = b2Body_GetWorldPointVelocity(bodyId, worldPoint);
    b2Counters counters = b2World_GetCounters(worldId);

    printf("pivot %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %d %d\n",
           p.x,
           p.y,
           b2Rot_GetAngle(b2Body_GetRotation(bodyId)),
           linearVelocity.x,
           linearVelocity.y,
           angularVelocity,
           pivotVelocity.x,
           pivotVelocity.y,
           localVelocity.x,
           localVelocity.y,
           worldVelocity.x,
           worldVelocity.y,
           counters.contactCount,
           b2World_GetAwakeBodyCount(worldId));

    b2DestroyWorld(worldId);
    return 0;
}
