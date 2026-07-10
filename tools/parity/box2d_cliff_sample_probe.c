// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>
#include <stdlib.h>

enum
{
    DEFAULT_STEP_COUNT = 120,
    BODY_COUNT = 9
};

static void print_body(b2BodyId bodyId)
{
    b2Transform transform = b2Body_GetTransform(bodyId);
    b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
    printf(" %.9g %.9g %.9g %.9g %.9g %.9g %.9g %d %d",
           transform.p.x,
           transform.p.y,
           transform.q.c,
           transform.q.s,
           velocity.x,
           velocity.y,
           b2Body_GetAngularVelocity(bodyId),
           b2Body_GetShapeCount(bodyId),
           b2Body_GetContactCapacity(bodyId));
}

static void create_ground(b2WorldId worldId)
{
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.position = (b2Vec2){0.0f, 0.0f};
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Polygon box = b2MakeOffsetBox(100.0f, 1.0f, (b2Vec2){0.0f, -1.0f}, b2Rot_identity);
    b2CreatePolygonShape(groundId, &shapeDef, &box);

    b2Segment segment = {{-14.0f, 4.0f}, {-8.0f, 4.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &segment);

    box = b2MakeOffsetBox(3.0f, 0.5f, (b2Vec2){0.0f, 4.0f}, b2Rot_identity);
    b2CreatePolygonShape(groundId, &shapeDef, &box);

    b2Capsule groundCapsule = {{8.5f, 4.0f}, {13.5f, 4.0f}, 0.5f};
    b2CreateCapsuleShape(groundId, &shapeDef, &groundCapsule);
}

static void create_bodies(b2WorldId worldId, b2BodyId* bodyIds, bool flip)
{
    float sign = flip ? -1.0f : 1.0f;

    b2Capsule capsule = {{-0.25f, 0.0f}, {0.25f, 0.0f}, 0.25f};
    b2Circle circle = {{0.0f, 0.0f}, 0.5f};
    b2Polygon square = b2MakeSquare(0.5f);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.material.friction = 0.01f;
    bodyDef.linearVelocity = (b2Vec2){2.0f * sign, 0.0f};
    float capsuleOffset = flip ? -4.0f : 0.0f;

    bodyDef.position = (b2Vec2){-9.0f + capsuleOffset, 4.25f};
    bodyIds[0] = b2CreateBody(worldId, &bodyDef);
    b2CreateCapsuleShape(bodyIds[0], &shapeDef, &capsule);

    bodyDef.position = (b2Vec2){2.0f + capsuleOffset, 4.75f};
    bodyIds[1] = b2CreateBody(worldId, &bodyDef);
    b2CreateCapsuleShape(bodyIds[1], &shapeDef, &capsule);

    bodyDef.position = (b2Vec2){13.0f + capsuleOffset, 4.75f};
    bodyIds[2] = b2CreateBody(worldId, &bodyDef);
    b2CreateCapsuleShape(bodyIds[2], &shapeDef, &capsule);

    shapeDef = b2DefaultShapeDef();
    shapeDef.material.friction = 0.01f;
    bodyDef.linearVelocity = (b2Vec2){2.5f * sign, 0.0f};

    bodyDef.position = (b2Vec2){-11.0f, 4.5f};
    bodyIds[3] = b2CreateBody(worldId, &bodyDef);
    b2CreatePolygonShape(bodyIds[3], &shapeDef, &square);

    bodyDef.position = (b2Vec2){0.0f, 5.0f};
    bodyIds[4] = b2CreateBody(worldId, &bodyDef);
    b2CreatePolygonShape(bodyIds[4], &shapeDef, &square);

    bodyDef.position = (b2Vec2){11.0f, 5.0f};
    bodyIds[5] = b2CreateBody(worldId, &bodyDef);
    b2CreatePolygonShape(bodyIds[5], &shapeDef, &square);

    shapeDef = b2DefaultShapeDef();
    shapeDef.material.friction = 0.2f;
    bodyDef.linearVelocity = (b2Vec2){1.5f * sign, 0.0f};
    float circleOffset = flip ? 4.0f : 0.0f;

    bodyDef.position = (b2Vec2){-13.0f + circleOffset, 4.5f};
    bodyIds[6] = b2CreateBody(worldId, &bodyDef);
    b2CreateCircleShape(bodyIds[6], &shapeDef, &circle);

    bodyDef.position = (b2Vec2){-2.0f + circleOffset, 5.0f};
    bodyIds[7] = b2CreateBody(worldId, &bodyDef);
    b2CreateCircleShape(bodyIds[7], &shapeDef, &circle);

    bodyDef.position = (b2Vec2){9.0f + circleOffset, 5.0f};
    bodyIds[8] = b2CreateBody(worldId, &bodyDef);
    b2CreateCircleShape(bodyIds[8], &shapeDef, &circle);
}

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : DEFAULT_STEP_COUNT;
    bool flip = argc <= 2 || atoi(argv[2]) != 0;

    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    create_ground(worldId);
    b2BodyId bodyIds[BODY_COUNT];
    create_bodies(worldId, bodyIds, flip);

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("cliff %d %d %d %d %d %d %d",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           counters.jointCount,
           b2World_GetAwakeBodyCount(worldId),
           flip ? 1 : 0,
           BODY_COUNT);
    for (int i = 0; i < BODY_COUNT; ++i)
    {
        print_body(bodyIds[i]);
    }
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
