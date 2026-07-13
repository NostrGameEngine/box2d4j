// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>
#include <stdlib.h>

static void print_body(b2BodyId bodyId)
{
    b2Vec2 p = b2Body_GetPosition(bodyId);
    b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
    printf(" %.9g %.9g %.9g %.9g %.9g %.9g",
           p.x,
           p.y,
           b2Rot_GetAngle(b2Body_GetRotation(bodyId)),
           v.x,
           v.y,
           b2Body_GetAngularVelocity(bodyId));
}

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : 240;
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyId bodies[20];
    float lift = 0.0f;
    float resistScale = 0.02f;
    b2Circle circle = {b2Vec2_zero, 0.5f};
    b2ShapeDef shapeDef = b2DefaultShapeDef();

    for (int i = 0; i < 20; ++i)
    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(worldId, &bodyDef);

        b2Segment segment = {{-40.0f, 2.0f * i}, {40.0f, 2.0f * i + lift}};
        b2CreateSegmentShape(groundId, &shapeDef, &segment);

        bodyDef.type = b2_dynamicBody;
        bodyDef.position = (b2Vec2){-39.5f, 2.0f * i + 0.75f};
        bodyDef.angularVelocity = -10.0f;
        bodyDef.linearVelocity = (b2Vec2){5.0f, 0.0f};

        bodies[i] = b2CreateBody(worldId, &bodyDef);
        shapeDef.material.rollingResistance = resistScale * i;
        b2CreateCircleShape(bodies[i], &shapeDef, &circle);
    }

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    int indices[] = {0, 5, 10, 15, 19};
    b2Counters counters = b2World_GetCounters(worldId);
    printf("rollingResistance %d %d", counters.contactCount, b2World_GetAwakeBodyCount(worldId));
    for (int i = 0; i < 5; ++i)
    {
        print_body(bodies[indices[i]]);
    }
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
