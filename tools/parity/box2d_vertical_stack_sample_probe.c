// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>

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

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.position = (b2Vec2){0.0f, 0.0f};
        b2BodyId groundId = b2CreateBody(worldId, &bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2Segment segment = {{10.0f, 0.0f}, {10.0f, 20.0f}};
        b2CreateSegmentShape(groundId, &shapeDef, &segment);

        segment = (b2Segment){{-30.0f, 0.0f}, {30.0f, 0.0f}};
        b2CreateSegmentShape(groundId, &shapeDef, &segment);
    }

    enum
    {
        rows = 12,
        columns = 1,
        count = rows * columns
    };
    b2BodyId bodies[count];
    b2Polygon box = b2MakeRoundedBox(0.45f, 0.45f, 0.05f);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.0f;
    shapeDef.material.friction = 0.3f;

    float offset = 0.01f;
    float dx = -3.0f;
    float xroot = 8.0f;

    for (int j = 0; j < columns; ++j)
    {
        float x = xroot + j * dx;

        for (int i = 0; i < rows; ++i)
        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;

            int n = j * rows + i;
            float shift = i % 2 == 0 ? -offset : offset;
            bodyDef.position = (b2Vec2){x + shift, 0.5f + 1.0f * i};
            bodies[n] = b2CreateBody(worldId, &bodyDef);

            b2CreatePolygonShape(bodies[n], &shapeDef, &box);
        }
    }

    for (int step = 0; step < 2400; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("verticalStack %d %d %d %d %d", counters.bodyCount, counters.shapeCount, counters.contactCount,
           b2World_GetAwakeBodyCount(worldId), count);
    for (int i = 0; i < count; ++i)
    {
        print_body(bodies[i]);
    }
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
