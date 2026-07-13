// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>
#include <stdlib.h>

static void print_body(b2BodyId bodyId)
{
    b2Vec2 p = b2Body_GetPosition(bodyId);
    b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
    printf(" %.9g %.9g %.9g %.9g %.9g",
           p.x,
           p.y,
           b2Rot_GetAngle(b2Body_GetRotation(bodyId)),
           v.x,
           v.y);
}

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : 240;
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    enum
    {
        count = 40
    };

    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(worldId, &bodyDef);

        float h = 1.0f * count;
        b2Segment segment = {{-h, 0.0f}, {h, 0.0f}};
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreateSegmentShape(groundId, &shapeDef, &segment);
    }

    b2BodyId bodies[count];
    b2Circle circle = {0};
    circle.radius = 0.5f;

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.0f;
    shapeDef.material.restitution = 0.0f;

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;

    float dr = 1.0f / (count - 1);
    float x = -1.0f * (count - 1);
    float dx = 2.0f;

    for (int i = 0; i < count; ++i)
    {
        bodyDef.position = (b2Vec2){x, 40.0f};
        bodies[i] = b2CreateBody(worldId, &bodyDef);
        b2CreateCircleShape(bodies[i], &shapeDef, &circle);
        shapeDef.material.restitution += dr;
        x += dx;
    }

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    int indices[] = {0, 10, 20, 30, 39};
    b2Counters counters = b2World_GetCounters(worldId);
    printf("restitution %d %d", counters.contactCount, b2World_GetAwakeBodyCount(worldId));
    for (int i = 0; i < 5; ++i)
    {
        print_body(bodies[indices[i]]);
    }
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
