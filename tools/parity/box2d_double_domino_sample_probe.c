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
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.position = (b2Vec2){0.0f, -1.0f};
        b2BodyId groundId = b2CreateBody(worldId, &bodyDef);

        b2Polygon box = b2MakeBox(100.0f, 1.0f);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreatePolygonShape(groundId, &shapeDef, &box);
    }

    enum
    {
        count = 15
    };
    int stepCount = argc > 1 ? atoi(argv[1]) : 54;

    b2Polygon box = b2MakeBox(0.125f, 0.5f);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.material.friction = 0.6f;
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;

    b2BodyId bodies[count];
    float x = -0.5f * count;
    for (int i = 0; i < count; ++i)
    {
        bodyDef.position = (b2Vec2){x, 0.5f};
        b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
        b2CreatePolygonShape(bodyId, &shapeDef, &box);
        if (i == 0)
        {
            b2Body_ApplyLinearImpulse(bodyId, (b2Vec2){0.2f, 0.0f}, (b2Vec2){x, 1.0f}, true);
        }

        bodies[i] = bodyId;
        x += 1.0f;
    }

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("doubleDomino %d %d %d %d %d",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           b2World_GetAwakeBodyCount(worldId),
           count);
    for (int i = 0; i < count; ++i)
    {
        print_body(bodies[i]);
    }
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
