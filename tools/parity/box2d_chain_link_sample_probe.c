// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>
#include <stdlib.h>

static b2ChainId create_open_chain(b2BodyId bodyId, b2Vec2* points, int count)
{
    b2ChainDef chainDef = b2DefaultChainDef();
    chainDef.points = points;
    chainDef.count = count;
    chainDef.isLoop = false;
    return b2CreateChain(bodyId, &chainDef);
}

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

    b2Vec2 points1[] = { {40.0f, 1.0f},   {0.0f, 0.0f},   {-40.0f, 0.0f},
                         {-40.0f, -1.0f}, {0.0f, -1.0f},  {40.0f, -1.0f} };
    b2Vec2 points2[] = { {-40.0f, -1.0f}, {0.0f, -1.0f},  {40.0f, -1.0f},
                         {40.0f, 0.0f},   {0.0f, 0.0f},   {-40.0f, 0.0f} };

    b2ChainId chainId1 = create_open_chain(groundId, points1, 6);
    b2ChainId chainId2 = create_open_chain(groundId, points2, 6);

    bodyDef.type = b2_dynamicBody;
    b2ShapeDef shapeDef = b2DefaultShapeDef();

    b2BodyId bodies[3];
    bodyDef.position = (b2Vec2){-5.0f, 2.0f};
    bodies[0] = b2CreateBody(worldId, &bodyDef);
    b2Circle circle = {{0.0f, 0.0f}, 0.5f};
    b2CreateCircleShape(bodies[0], &shapeDef, &circle);

    bodyDef.position = (b2Vec2){0.0f, 2.0f};
    bodies[1] = b2CreateBody(worldId, &bodyDef);
    b2Capsule capsule = {{-0.5f, 0.0f}, {0.5f, 0.0f}, 0.25f};
    b2CreateCapsuleShape(bodies[1], &shapeDef, &capsule);

    bodyDef.position = (b2Vec2){5.0f, 2.0f};
    bodies[2] = b2CreateBody(worldId, &bodyDef);
    b2Polygon box = b2MakeBox(0.5f, 0.5f);
    b2CreatePolygonShape(bodies[2], &shapeDef, &box);

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("chainLink %d %d %d %d %d %d %d",
           b2Chain_GetSegmentCount(chainId1),
           b2Chain_GetSegmentCount(chainId2),
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           b2World_GetAwakeBodyCount(worldId),
           3);
    for (int i = 0; i < 3; ++i)
    {
        print_body(bodies[i]);
    }
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
