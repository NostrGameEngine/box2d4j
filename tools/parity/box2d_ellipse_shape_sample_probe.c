// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>
#include <stdlib.h>

static int sample_indices[] = {0, 1, 9, 10, 44, 55, 90, 99};

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

    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Polygon box = b2MakeOffsetBox(20.0f, 1.0f, (b2Vec2){0.0f, -1.0f}, b2Rot_identity);
    b2CreatePolygonShape(groundId, &shapeDef, &box);

    box = b2MakeOffsetBox(1.0f, 5.0f, (b2Vec2){19.0f, 5.0f}, b2Rot_identity);
    b2CreatePolygonShape(groundId, &shapeDef, &box);

    box = b2MakeOffsetBox(1.0f, 5.0f, (b2Vec2){-19.0f, 5.0f}, b2Rot_identity);
    b2CreatePolygonShape(groundId, &shapeDef, &box);

    b2Vec2 points[6] = {
        {0.0f, -0.25f},
        {0.0f, 0.25f},
        {0.05f, 0.075f},
        {-0.05f, 0.075f},
        {0.05f, -0.075f},
        {-0.05f, -0.075f},
    };
    b2Hull diamondHull = b2ComputeHull(points, 6);
    b2Polygon poly = b2MakePolygon(&diamondHull, 0.2f);

    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    shapeDef = b2DefaultShapeDef();
    shapeDef.material.rollingResistance = 0.2f;

    b2BodyId bodies[100];
    int bodyIndex = 0;
    float y = 2.0f;
    for (int i = 0; i < 10; ++i)
    {
        float x = -5.0f;
        for (int j = 0; j < 10; ++j)
        {
            bodyDef.position = (b2Vec2){x, y};
            b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
            b2CreatePolygonShape(bodyId, &shapeDef, &poly);
            bodies[bodyIndex++] = bodyId;
            x += 1.0f;
        }
        y += 1.0f;
    }

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    int sampleCount = (int)(sizeof(sample_indices) / sizeof(sample_indices[0]));
    printf("ellipseShape %d %.9g %d %d %d %d %d",
           poly.count,
           poly.radius,
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           b2World_GetAwakeBodyCount(worldId),
           sampleCount);
    for (int i = 0; i < sampleCount; ++i)
    {
        print_body(bodies[sample_indices[i]]);
    }
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
