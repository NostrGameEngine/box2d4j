// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>
#include <stdlib.h>

enum
{
    DEFAULT_STEP_COUNT = 120
};

typedef struct Scene
{
    b2BodyId playerId;
    b2ShapeId playerShapeId;
    b2BodyId movingPlatformId;
    bool jumping;
    bool lastCanJump;
    float radius;
    float force;
    float impulse;
    float jumpDelay;
    int preSolveCalls;
    int preSolveDisabled;
} Scene;

static bool PreSolve(b2ShapeId shapeIdA, b2ShapeId shapeIdB, b2Manifold* manifold, void* context)
{
    Scene* scene = context;
    scene->preSolveCalls += 1;

    if (b2Shape_IsValid(shapeIdA) == false || b2Shape_IsValid(shapeIdB) == false)
    {
        return false;
    }

    float sign = 0.0f;
    if (B2_ID_EQUALS(shapeIdA, scene->playerShapeId))
    {
        sign = -1.0f;
    }
    else if (B2_ID_EQUALS(shapeIdB, scene->playerShapeId))
    {
        sign = 1.0f;
    }
    else
    {
        return true;
    }

    b2Vec2 normal = manifold->normal;
    if (sign * normal.y > 0.95f)
    {
        return true;
    }

    float separation = 0.0f;
    for (int i = 0; i < manifold->pointCount; ++i)
    {
        float s = manifold->points[i].separation;
        separation = separation < s ? separation : s;
    }

    if (separation > 0.1f * scene->radius)
    {
        return true;
    }

    scene->preSolveDisabled += 1;
    return false;
}

static Scene create_scene(b2WorldId worldId)
{
    Scene scene = {0};
    b2World_SetPreSolveCallback(worldId, PreSolve, &scene);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Segment segment = {{-20.0f, 0.0f}, {20.0f, 0.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &segment);

    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_staticBody;
    bodyDef.position = (b2Vec2){-6.0f, 6.0f};
    b2BodyId staticPlatformId = b2CreateBody(worldId, &bodyDef);
    shapeDef = b2DefaultShapeDef();
    shapeDef.enablePreSolveEvents = true;
    b2Polygon box = b2MakeBox(2.0f, 0.5f);
    b2CreatePolygonShape(staticPlatformId, &shapeDef, &box);

    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_kinematicBody;
    bodyDef.position = (b2Vec2){0.0f, 6.0f};
    bodyDef.linearVelocity = (b2Vec2){2.0f, 0.0f};
    scene.movingPlatformId = b2CreateBody(worldId, &bodyDef);
    shapeDef = b2DefaultShapeDef();
    shapeDef.enablePreSolveEvents = true;
    box = b2MakeBox(3.0f, 0.5f);
    b2CreatePolygonShape(scene.movingPlatformId, &shapeDef, &box);

    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.fixedRotation = true;
    bodyDef.linearDamping = 0.5f;
    bodyDef.position = (b2Vec2){0.0f, 1.0f};
    scene.playerId = b2CreateBody(worldId, &bodyDef);

    scene.radius = 0.5f;
    b2Capsule capsule = {{0.0f, 0.0f}, {0.0f, 1.0f}, scene.radius};
    shapeDef = b2DefaultShapeDef();
    shapeDef.material.friction = 0.1f;
    scene.playerShapeId = b2CreateCapsuleShape(scene.playerId, &shapeDef, &capsule);

    scene.force = 25.0f;
    scene.impulse = 25.0f;
    scene.jumpDelay = 0.25f;
    scene.jumping = false;
    return scene;
}

static void print_contact_state(b2BodyId bodyId, int maxCapacity)
{
    int capacity = b2Body_GetContactCapacity(bodyId);
    capacity = capacity < maxCapacity ? capacity : maxCapacity;
    b2ContactData contactData[4] = {0};
    int count = b2Body_GetContactData(bodyId, contactData, capacity);
    int pointCount = 0;
    int normalChecksum = 0;
    for (int i = 0; i < count; ++i)
    {
        pointCount += contactData[i].manifold.pointCount;
        normalChecksum += (int)(1000.0f * contactData[i].manifold.normal.y + (contactData[i].manifold.normal.y >= 0.0f ? 0.5f : -0.5f));
    }
    printf(" %d %d %d %d", capacity, count, pointCount, normalChecksum);
}

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
    Scene scene = create_scene(worldId);

    for (int step = 0; step < stepCount; ++step)
    {
        scene.lastCanJump = false;
        b2Vec2 velocity = b2Body_GetLinearVelocity(scene.playerId);
        if (scene.jumpDelay == 0.0f && scene.jumping == false && velocity.y < 0.01f)
        {
            int capacity = b2Body_GetContactCapacity(scene.playerId);
            capacity = capacity < 4 ? capacity : 4;
            b2ContactData contactData[4];
            int count = b2Body_GetContactData(scene.playerId, contactData, capacity);
            for (int i = 0; i < count; ++i)
            {
                b2BodyId bodyIdA = b2Shape_GetBody(contactData[i].shapeIdA);
                float sign = B2_ID_EQUALS(bodyIdA, scene.playerId) ? -1.0f : 1.0f;
                if (sign * contactData[i].manifold.normal.y > 0.9f)
                {
                    scene.lastCanJump = true;
                    break;
                }
            }
        }

        b2Vec2 platformPosition = b2Body_GetPosition(scene.movingPlatformId);
        if (platformPosition.x < -15.0f)
        {
            b2Body_SetLinearVelocity(scene.movingPlatformId, (b2Vec2){2.0f, 0.0f});
        }
        else if (platformPosition.x > 15.0f)
        {
            b2Body_SetLinearVelocity(scene.movingPlatformId, (b2Vec2){-2.0f, 0.0f});
        }

        b2World_Step(worldId, 1.0f / 60.0f, 4);
        scene.jumpDelay = b2MaxFloat(0.0f, scene.jumpDelay - 1.0f / 60.0f);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("platformer %d %d %d %d %d %d %d %d %d %.9g",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           counters.jointCount,
           b2World_GetAwakeBodyCount(worldId),
           scene.preSolveCalls,
           scene.preSolveDisabled,
           scene.lastCanJump ? 1 : 0,
           scene.jumping ? 1 : 0,
           scene.jumpDelay);
    print_contact_state(scene.movingPlatformId, 1);
    print_contact_state(scene.playerId, 4);
    print_body(scene.playerId);
    print_body(scene.movingPlatformId);
    printf(" %d\n", b2Shape_IsValid(scene.playerShapeId) ? 1 : 0);

    b2DestroyWorld(worldId);
    return 0;
}
