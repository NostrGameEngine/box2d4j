// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "random.h"

#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>

enum
{
    DEFAULT_STEP_COUNT = 108,
    MAX_COUNT = 50
};

typedef struct Scene
{
    b2WorldId worldId;
    b2BodyId bodyIds[MAX_COUNT];
    bool sleeping[MAX_COUNT];
    int count;
    int sleepCount;
    int lastMoveCount;
    int totalMoveCount;
    int totalFellAsleep;
} Scene;

static void print_body(b2BodyId bodyId, bool sleeping)
{
    b2Transform transform = b2Body_GetTransform(bodyId);
    b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
    printf(" %.9g %.9g %.9g %.9g %.9g %.9g %.9g %d %d %d",
           transform.p.x,
           transform.p.y,
           transform.q.c,
           transform.q.s,
           velocity.x,
           velocity.y,
           b2Body_GetAngularVelocity(bodyId),
           b2Body_GetShapeCount(bodyId),
           b2Body_GetContactCapacity(bodyId),
           sleeping ? 1 : 0);
}

static void create_ground(Scene* scene)
{
    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(scene->worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.material.friction = 0.1f;

    b2Polygon box = b2MakeOffsetBox(12.0f, 0.1f, (b2Vec2){-10.0f, -0.1f}, b2MakeRot(-0.15f * B2_PI));
    b2CreatePolygonShape(groundId, &shapeDef, &box);

    box = b2MakeOffsetBox(12.0f, 0.1f, (b2Vec2){10.0f, -0.1f}, b2MakeRot(0.15f * B2_PI));
    b2CreatePolygonShape(groundId, &shapeDef, &box);

    shapeDef.material.restitution = 0.8f;

    box = b2MakeOffsetBox(0.1f, 10.0f, (b2Vec2){19.9f, 10.0f}, b2Rot_identity);
    b2CreatePolygonShape(groundId, &shapeDef, &box);

    box = b2MakeOffsetBox(0.1f, 10.0f, (b2Vec2){-19.9f, 10.0f}, b2Rot_identity);
    b2CreatePolygonShape(groundId, &shapeDef, &box);

    box = b2MakeOffsetBox(20.0f, 0.1f, (b2Vec2){0.0f, 20.1f}, b2Rot_identity);
    b2CreatePolygonShape(groundId, &shapeDef, &box);
}

static void create_bodies(Scene* scene)
{
    b2Capsule capsule = {{-0.25f, 0.0f}, {0.25f, 0.0f}, 0.25f};
    b2Circle circle = {{0.0f, 0.0f}, 0.35f};
    b2Polygon square = b2MakeSquare(0.35f);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    b2ShapeDef shapeDef = b2DefaultShapeDef();

    float x = -5.0f;
    float y = 10.0f;
    for (int i = 0; i < 10 && scene->count < MAX_COUNT; ++i)
    {
        bodyDef.position = (b2Vec2){x, y};
        bodyDef.isBullet = scene->count % 12 == 0;
        bodyDef.userData = scene->bodyIds + scene->count;
        scene->bodyIds[scene->count] = b2CreateBody(scene->worldId, &bodyDef);
        scene->sleeping[scene->count] = false;

        int remainder = scene->count % 4;
        if (remainder == 0)
        {
            b2CreateCapsuleShape(scene->bodyIds[scene->count], &shapeDef, &capsule);
        }
        else if (remainder == 1)
        {
            b2CreateCircleShape(scene->bodyIds[scene->count], &shapeDef, &circle);
        }
        else if (remainder == 2)
        {
            b2CreatePolygonShape(scene->bodyIds[scene->count], &shapeDef, &square);
        }
        else
        {
            b2Polygon poly = RandomPolygon(0.75f);
            poly.radius = 0.1f;
            b2CreatePolygonShape(scene->bodyIds[scene->count], &shapeDef, &poly);
        }

        scene->count += 1;
        x += 1.0f;
    }
}

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : DEFAULT_STEP_COUNT;
    g_randomSeed = RAND_SEED;

    Scene scene = {0};
    b2WorldDef worldDef = b2DefaultWorldDef();
    scene.worldId = b2CreateWorld(&worldDef);
    create_ground(&scene);

    for (int step = 0; step < stepCount; ++step)
    {
        if ((step & 15) == 15 && scene.count < MAX_COUNT)
        {
            create_bodies(&scene);
        }

        b2World_Step(scene.worldId, 1.0f / 60.0f, 4);
        b2BodyEvents events = b2World_GetBodyEvents(scene.worldId);
        scene.lastMoveCount = events.moveCount;
        scene.totalMoveCount += events.moveCount;
        for (int i = 0; i < events.moveCount; ++i)
        {
            b2BodyMoveEvent* event = events.moveEvents + i;
            b2BodyId* bodyId = event->userData;
            ptrdiff_t diff = bodyId - scene.bodyIds;

            if (event->fellAsleep)
            {
                if (scene.sleeping[diff] == false)
                {
                    scene.sleeping[diff] = true;
                    scene.sleepCount += 1;
                }
                scene.totalFellAsleep += 1;
            }
            else if (scene.sleeping[diff])
            {
                scene.sleeping[diff] = false;
                scene.sleepCount -= 1;
            }
        }
    }

    b2Counters counters = b2World_GetCounters(scene.worldId);
    printf("bodyMove %d %d %d %d %d %d %d %d %d %d %d",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           counters.jointCount,
           b2World_GetAwakeBodyCount(scene.worldId),
           scene.count,
           scene.sleepCount,
           scene.lastMoveCount,
           scene.totalMoveCount,
           scene.totalFellAsleep,
           scene.count);
    for (int i = 0; i < scene.count; ++i)
    {
        print_body(scene.bodyIds[i], scene.sleeping[i]);
    }
    printf("\n");

    b2DestroyWorld(scene.worldId);
    return 0;
}
