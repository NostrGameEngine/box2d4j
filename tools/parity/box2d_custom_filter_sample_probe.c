// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

enum
{
    e_count = 10
};

static int filterCalls = 0;

static bool custom_filter(b2ShapeId shapeIdA, b2ShapeId shapeIdB, void* context)
{
    (void)context;
    filterCalls += 1;

    void* userDataA = b2Shape_GetUserData(shapeIdA);
    void* userDataB = b2Shape_GetUserData(shapeIdB);

    if (userDataA == NULL || userDataB == NULL)
    {
        return true;
    }

    int indexA = (int)(intptr_t)userDataA;
    int indexB = (int)(intptr_t)userDataB;
    return ((indexA & 1) + (indexB & 1)) != 1;
}

static void print_shape(b2ShapeId shapeId)
{
    printf(" %d", (int)(intptr_t)b2Shape_GetUserData(shapeId));
}

static void print_body(b2BodyId bodyId)
{
    b2Vec2 p = b2Body_GetPosition(bodyId);
    b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
    printf(" %d %.9g %.9g %.9g %.9g %.9g",
           b2Body_GetContactCapacity(bodyId),
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

    b2World_SetCustomFilterCallback(worldId, custom_filter, NULL);

    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
        b2Segment segment = {{-40.0f, 0.0f}, {40.0f, 0.0f}};

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreateSegmentShape(groundId, &shapeDef, &segment);
    }

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Polygon box = b2MakeSquare(1.0f);
    b2BodyId bodies[e_count];
    b2ShapeId shapes[e_count];
    float x = -e_count;

    for (int i = 0; i < e_count; ++i)
    {
        bodyDef.position = (b2Vec2){x, 5.0f};
        bodies[i] = b2CreateBody(worldId, &bodyDef);

        shapeDef.userData = (void*)(intptr_t)(i + 1);
        shapes[i] = b2CreatePolygonShape(bodies[i], &shapeDef, &box);
        x += 2.0f;
    }

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("customFilter %d %d %d %d %d %d %d",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           b2World_GetAwakeBodyCount(worldId),
           filterCalls,
           e_count,
           e_count);
    for (int i = 0; i < e_count; ++i)
    {
        print_shape(shapes[i]);
    }
    for (int i = 0; i < e_count; ++i)
    {
        print_body(bodies[i]);
    }
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
