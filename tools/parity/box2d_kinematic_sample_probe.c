// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <math.h>
#include <stdio.h>
#include <stdlib.h>

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : 120;
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    float amplitude = 2.0f;
    float time = 0.0f;
    float timeStep = 1.0f / 60.0f;

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_kinematicBody;
    bodyDef.position.x = 2.0f * amplitude;
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    b2Polygon box = b2MakeBox(0.1f, 1.0f);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2CreatePolygonShape(bodyId, &shapeDef, &box);

    for (int i = 0; i < stepCount; ++i)
    {
        b2Vec2 point = {
            .x = 2.0f * amplitude * cosf(time),
            .y = amplitude * sinf(2.0f * time),
        };
        b2Rot rotation = b2MakeRot(2.0f * time);
        b2Body_SetTargetTransform(bodyId, (b2Transform){point, rotation}, timeStep);

        b2World_Step(worldId, timeStep, 4);
        time += timeStep;
    }

    b2Vec2 p = b2Body_GetPosition(bodyId);
    b2Rot q = b2Body_GetRotation(bodyId);
    b2Vec2 v = b2Body_GetLinearVelocity(bodyId);

    printf("kinematic %.9g %.9g %.9g %.9g %.9g %.9g %.9g %d\n",
           p.x,
           p.y,
           b2Rot_GetAngle(q),
           v.x,
           v.y,
           b2Body_GetAngularVelocity(bodyId),
           time,
           b2World_GetAwakeBodyCount(worldId));

    b2DestroyWorld(worldId);
    return 0;
}
