// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>

enum CollisionBits
{
    GROUND = 0x00000001,
    TEAM1 = 0x00000002,
    TEAM2 = 0x00000004,
    TEAM3 = 0x00000008,
    ALL_BITS = (~0u)
};

static void print_shape(b2ShapeId shapeId)
{
    b2Filter filter = b2Shape_GetFilter(shapeId);
    printf(" %llu %llu %d", (unsigned long long)filter.categoryBits, (unsigned long long)filter.maskBits, filter.groupIndex);
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

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
        b2Segment segment = {{-20.0f, 0.0f}, {20.0f, 0.0f}};

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.filter.categoryBits = GROUND;
        shapeDef.filter.maskBits = ALL_BITS;

        b2CreateSegmentShape(groundId, &shapeDef, &segment);
    }

    b2BodyId bodies[3];
    b2ShapeId shapes[3];
    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;

        bodyDef.position = (b2Vec2){0.0f, 2.0f};
        bodies[0] = b2CreateBody(worldId, &bodyDef);

        bodyDef.position = (b2Vec2){0.0f, 5.0f};
        bodies[1] = b2CreateBody(worldId, &bodyDef);

        bodyDef.position = (b2Vec2){0.0f, 8.0f};
        bodies[2] = b2CreateBody(worldId, &bodyDef);

        b2Polygon box = b2MakeBox(2.0f, 1.0f);

        b2ShapeDef shapeDef = b2DefaultShapeDef();

        shapeDef.filter.categoryBits = TEAM1;
        shapeDef.filter.maskBits = GROUND | TEAM2 | TEAM3;
        shapes[0] = b2CreatePolygonShape(bodies[0], &shapeDef, &box);

        shapeDef.filter.categoryBits = TEAM2;
        shapeDef.filter.maskBits = GROUND | TEAM1 | TEAM3;
        shapes[1] = b2CreatePolygonShape(bodies[1], &shapeDef, &box);

        shapeDef.filter.categoryBits = TEAM3;
        shapeDef.filter.maskBits = GROUND | TEAM1 | TEAM2;
        shapes[2] = b2CreatePolygonShape(bodies[2], &shapeDef, &box);
    }

    for (int step = 0; step < 120; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Filter filter1 = b2Shape_GetFilter(shapes[0]);
    filter1.maskBits &= ~TEAM2;
    b2Shape_SetFilter(shapes[0], filter1);

    b2Filter filter2 = b2Shape_GetFilter(shapes[1]);
    filter2.maskBits &= ~TEAM1;
    filter2.maskBits &= ~TEAM3;
    b2Shape_SetFilter(shapes[1], filter2);

    b2Filter filter3 = b2Shape_GetFilter(shapes[2]);
    filter3.maskBits &= ~TEAM2;
    b2Shape_SetFilter(shapes[2], filter3);

    for (int step = 0; step < 120; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("shapeFilter %d %d %d %d 3 3",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           b2World_GetAwakeBodyCount(worldId));
    for (int i = 0; i < 3; ++i)
    {
        print_shape(shapes[i]);
    }
    for (int i = 0; i < 3; ++i)
    {
        print_body(bodies[i]);
    }
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
