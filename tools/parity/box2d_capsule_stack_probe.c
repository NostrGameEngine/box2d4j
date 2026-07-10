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
        bodyDef.position = (b2Vec2){0.0f, -1.0f};
        b2BodyId groundId = b2CreateBody(worldId, &bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2Polygon polygon = b2MakeBox(10.0f, 1.0f);
        b2CreatePolygonShape(groundId, &shapeDef, &polygon);
    }

    b2BodyId bodies[20];
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;

    float a = 0.25f;
    b2Capsule capsule = {{-4.0f * a, 0.0f}, {4.0f * a, 0.0f}, a};

    b2ShapeDef shapeDef = b2DefaultShapeDef();

    float y = 2.0f * a;
    for (int i = 0; i < 20; ++i)
    {
        bodyDef.position.y = y;
        bodies[i] = b2CreateBody(worldId, &bodyDef);
        b2CreateCapsuleShape(bodies[i], &shapeDef, &capsule);
        y += 3.0f * a;
    }

    for (int step = 0; step < 180; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("capsuleStack %d %d", counters.contactCount, b2World_GetAwakeBodyCount(worldId));
    print_body(bodies[0]);
    print_body(bodies[10]);
    print_body(bodies[19]);
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
