// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>
#include <stdlib.h>

enum
{
    DEFAULT_STEP_COUNT = 120,
    GRID_COUNT = 25,
    BODY_COUNT = GRID_COUNT * GRID_COUNT,
    SAMPLE_COUNT = 9
};

static const int sample_indices[SAMPLE_COUNT] = {0, 12, 24, 300, 312, 324, 600, 612, 624};

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

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : DEFAULT_STEP_COUNT;

    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyId bodies[BODY_COUNT];

    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Capsule capsule = {{-10.5f, 0.0f}, {10.5f, 0.0f}, 0.5f};
    b2CreateCapsuleShape(groundId, &shapeDef, &capsule);
    capsule = (b2Capsule){{-10.5f, 0.0f}, {-10.5f, 20.5f}, 0.5f};
    b2CreateCapsuleShape(groundId, &shapeDef, &capsule);
    capsule = (b2Capsule){{10.5f, 0.0f}, {10.5f, 20.5f}, 0.5f};
    b2CreateCapsuleShape(groundId, &shapeDef, &capsule);
    capsule = (b2Capsule){{-10.5f, 20.5f}, {10.5f, 20.5f}, 0.5f};
    b2CreateCapsuleShape(groundId, &shapeDef, &capsule);

    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.gravityScale = 0.0f;

    shapeDef = b2DefaultShapeDef();
    b2Circle circle = {{0.0f, 0.0f}, 0.5f};

    int row = 0;
    int column = 0;
    int count = 0;
    while (count < BODY_COUNT)
    {
        row = 0;
        for (int i = 0; i < GRID_COUNT; ++i)
        {
            float x = -8.75f + column * 18.0f / GRID_COUNT;
            float y = 1.5f + row * 18.0f / GRID_COUNT;

            bodyDef.position = (b2Vec2){x, y};
            b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
            b2CreateCircleShape(bodyId, &shapeDef, &circle);

            bodies[count] = bodyId;
            count += 1;
            row += 1;
        }
        column += 1;
    }

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("confined %d %d %d %d %d %d %d",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           counters.jointCount,
           b2World_GetAwakeBodyCount(worldId),
           BODY_COUNT,
           SAMPLE_COUNT);
    for (int i = 0; i < SAMPLE_COUNT; ++i)
    {
        print_body(bodies[sample_indices[i]]);
    }
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
