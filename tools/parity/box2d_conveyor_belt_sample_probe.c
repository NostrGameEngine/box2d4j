// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>

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

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(worldId, &bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2Segment segment = {{-20.0f, 0.0f}, {20.0f, 0.0f}};
        b2CreateSegmentShape(groundId, &shapeDef, &segment);
    }

    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.position = (b2Vec2){-5.0f, 5.0f};
        b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

        b2Polygon box = b2MakeRoundedBox(10.0f, 0.25f, 0.25f);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.8f;
        shapeDef.material.tangentSpeed = 2.0f;

        b2CreatePolygonShape(bodyId, &shapeDef, &box);
    }

    b2BodyId bodies[5];
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Polygon cube = b2MakeSquare(0.5f);
    for (int i = 0; i < 5; ++i)
    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = (b2Vec2){-10.0f + 2.0f * i, 7.0f};
        bodies[i] = b2CreateBody(worldId, &bodyDef);

        b2CreatePolygonShape(bodies[i], &shapeDef, &cube);
    }

    for (int step = 0; step < 240; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("conveyorBelt %d %d", counters.contactCount, b2World_GetAwakeBodyCount(worldId));
    for (int i = 0; i < 5; ++i)
    {
        print_body(bodies[i]);
    }
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
