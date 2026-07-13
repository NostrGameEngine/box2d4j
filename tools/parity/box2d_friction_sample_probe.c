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

    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(worldId, &bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.2f;

        b2Segment segment = {{-40.0f, 0.0f}, {40.0f, 0.0f}};
        b2CreateSegmentShape(groundId, &shapeDef, &segment);

        b2Polygon box = b2MakeOffsetBox(13.0f, 0.25f, (b2Vec2){-4.0f, 22.0f}, b2MakeRot(-0.25f));
        b2CreatePolygonShape(groundId, &shapeDef, &box);

        box = b2MakeOffsetBox(0.25f, 1.0f, (b2Vec2){10.5f, 19.0f}, b2Rot_identity);
        b2CreatePolygonShape(groundId, &shapeDef, &box);

        box = b2MakeOffsetBox(13.0f, 0.25f, (b2Vec2){4.0f, 14.0f}, b2MakeRot(0.25f));
        b2CreatePolygonShape(groundId, &shapeDef, &box);

        box = b2MakeOffsetBox(0.25f, 1.0f, (b2Vec2){-10.5f, 11.0f}, b2Rot_identity);
        b2CreatePolygonShape(groundId, &shapeDef, &box);

        box = b2MakeOffsetBox(13.0f, 0.25f, (b2Vec2){-4.0f, 6.0f}, b2MakeRot(-0.25f));
        b2CreatePolygonShape(groundId, &shapeDef, &box);
    }

    b2BodyId bodies[5];
    b2Polygon box = b2MakeBox(0.5f, 0.5f);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 25.0f;
    float friction[5] = {0.75f, 0.5f, 0.35f, 0.1f, 0.0f};

    for (int i = 0; i < 5; ++i)
    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = (b2Vec2){-15.0f + 4.0f * i, 28.0f};
        bodies[i] = b2CreateBody(worldId, &bodyDef);

        shapeDef.material.friction = friction[i];
        b2CreatePolygonShape(bodies[i], &shapeDef, &box);
    }

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("friction %d %d", counters.contactCount, b2World_GetAwakeBodyCount(worldId));
    for (int i = 0; i < 5; ++i)
    {
        print_body(bodies[i]);
    }
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
