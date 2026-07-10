// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"
#include "human.h"

#include <math.h>
#include <stdint.h>
#include <stdio.h>

enum
{
    checkpoint_count = 31,
};

static const int checkpoints[checkpoint_count] = {
    1, 2, 4, 8, 12, 16, 24, 32, 60, 64, 68, 72, 76, 80, 84, 88, 92, 96,
    97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 120,
};

typedef struct BodyState
{
    int step;
    b2Vec2 position;
    b2Rot rotation;
    b2Vec2 velocity;
    float angularVelocity;
    int contactCount;
    int awake;
} BodyState;

static void SimulateAndPrintWithAction(const char* name, b2WorldId worldId, b2BodyId bodyId,
                                       void (*beforeStep)(int step))
{
    BodyState states[checkpoint_count] = {0};
    int checkpointIndex = 0;
    for (int step = 1; step <= checkpoints[checkpoint_count - 1]; ++step)
    {
        if (beforeStep != NULL)
        {
            beforeStep(step);
        }
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        if (step == checkpoints[checkpointIndex])
        {
            b2ContactData contactData[8];
            BodyState* state = states + checkpointIndex;
            state->step = step;
            state->position = b2Body_GetPosition(bodyId);
            state->rotation = b2Body_GetRotation(bodyId);
            state->velocity = b2Body_GetLinearVelocity(bodyId);
            state->angularVelocity = b2Body_GetAngularVelocity(bodyId);
            state->contactCount = b2Body_GetContactData(bodyId, contactData, 8);
            state->awake = b2Body_IsAwake(bodyId) ? 1 : 0;
            checkpointIndex += 1;
        }
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("%s %d %d %d %d %d %d", name, counters.bodyCount, counters.shapeCount,
           counters.contactCount, counters.islandCount, b2World_GetAwakeBodyCount(worldId), checkpoint_count);
    for (int i = 0; i < checkpoint_count; ++i)
    {
        BodyState* state = states + i;
        printf(" %d %.9g %.9g %.9g %.9g %.9g %.9g %.9g %d %d",
               state->step, state->position.x, state->position.y, state->rotation.c, state->rotation.s,
               state->velocity.x, state->velocity.y, state->angularVelocity, state->contactCount, state->awake);
    }
    printf("\n");

    b2DestroyWorld(worldId);
}

static void SimulateAndPrint(const char* name, b2WorldId worldId, b2BodyId bodyId)
{
    SimulateAndPrintWithAction(name, worldId, bodyId, NULL);
}

static void BounceHouse(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Segment segment = {{-10.0f, -10.0f}, {10.0f, -10.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &segment);
    segment = (b2Segment){{10.0f, -10.0f}, {10.0f, 10.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &segment);
    segment = (b2Segment){{10.0f, 10.0f}, {-10.0f, 10.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &segment);
    segment = (b2Segment){{-10.0f, 10.0f}, {-10.0f, -10.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &segment);

    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.linearVelocity = (b2Vec2){10.0f, 20.0f};
    bodyDef.position = (b2Vec2){0.0f, 0.0f};
    bodyDef.gravityScale = 0.0f;
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.0f;
    shapeDef.material.restitution = 1.2f;
    shapeDef.material.friction = 0.3f;
    shapeDef.enableHitEvents = true;
    float halfHeight = 0.1f;
    b2Polygon box = b2MakeBox(20.0f * halfHeight, halfHeight);
    b2CreatePolygonShape(bodyId, &shapeDef, &box);

    SimulateAndPrint("bounceHouse", worldId, bodyId);
}

static b2WorldId bounceHumansWorld;
static float bounceHumansTime;

static void BounceHumansBeforeStep(int step)
{
    (void)step;
    b2CosSin cs1 = b2ComputeCosSin(0.5f * bounceHumansTime);
    b2CosSin cs2 = b2ComputeCosSin(bounceHumansTime);
    b2Vec2 gravity = {10.0f * cs1.sine, 10.0f * cs2.cosine};
    b2World_SetGravity(bounceHumansWorld, gravity);
    bounceHumansTime += 1.0f / 60.0f;
}

static void BounceHumans(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.material.restitution = 1.3f;
    shapeDef.material.friction = 0.1f;
    b2Segment segment = {{-10.0f, -10.0f}, {10.0f, -10.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &segment);
    segment = (b2Segment){{10.0f, -10.0f}, {10.0f, 10.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &segment);
    segment = (b2Segment){{10.0f, 10.0f}, {-10.0f, 10.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &segment);
    segment = (b2Segment){{-10.0f, 10.0f}, {-10.0f, -10.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &segment);
    shapeDef.material.restitution = 2.0f;
    b2Circle circle = {{0.0f, 0.0f}, 2.0f};
    b2CreateCircleShape(groundId, &shapeDef, &circle);

    Human human = {0};
    CreateHuman(&human, worldId, (b2Vec2){0.0f, 5.0f}, 1.0f, 0.0f, 1.0f, 0.1f, 1, NULL, true);
    bounceHumansWorld = worldId;
    bounceHumansTime = 0.0f;
    SimulateAndPrintWithAction("bounceHumans", worldId, human.bones[bone_hip].bodyId, BounceHumansBeforeStep);
}

static void GhostBumps(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);

    float m = 1.0f / sqrtf(2.0f);
    float mm = 2.0f * (sqrtf(2.0f) - 1.0f);
    float hx = 4.0f;
    float hy = 0.25f;
    b2Vec2 points[20];
    points[0] = (b2Vec2){-3.0f * hx, hy};
    points[1] = b2Add(points[0], (b2Vec2){-2.0f * hx * m, 2.0f * hx * m});
    points[2] = b2Add(points[1], (b2Vec2){-2.0f * hx * m, 2.0f * hx * m});
    points[3] = b2Add(points[2], (b2Vec2){-2.0f * hx * m, 2.0f * hx * m});
    points[4] = b2Add(points[3], (b2Vec2){-2.0f * hy * m, -2.0f * hy * m});
    points[5] = b2Add(points[4], (b2Vec2){2.0f * hx * m, -2.0f * hx * m});
    points[6] = b2Add(points[5], (b2Vec2){2.0f * hx * m, -2.0f * hx * m});
    points[7] = b2Add(points[6], (b2Vec2){2.0f * hx * m + 2.0f * hy * (1.0f - m),
                                         -2.0f * hx * m - 2.0f * hy * (1.0f - m)});
    points[8] = b2Add(points[7], (b2Vec2){2.0f * hx + hy * mm, 0.0f});
    points[9] = b2Add(points[8], (b2Vec2){2.0f * hx, 0.0f});
    points[10] = b2Add(points[9], (b2Vec2){2.0f * hx + hy * mm, 0.0f});
    points[11] = b2Add(points[10], (b2Vec2){2.0f * hx * m + 2.0f * hy * (1.0f - m),
                                            2.0f * hx * m + 2.0f * hy * (1.0f - m)});
    points[12] = b2Add(points[11], (b2Vec2){2.0f * hx * m, 2.0f * hx * m});
    points[13] = b2Add(points[12], (b2Vec2){2.0f * hx * m, 2.0f * hx * m});
    points[14] = b2Add(points[13], (b2Vec2){-2.0f * hy * m, 2.0f * hy * m});
    points[15] = b2Add(points[14], (b2Vec2){-2.0f * hx * m, -2.0f * hx * m});
    points[16] = b2Add(points[15], (b2Vec2){-2.0f * hx * m, -2.0f * hx * m});
    points[17] = b2Add(points[16], (b2Vec2){-2.0f * hx * m, -2.0f * hx * m});
    points[18] = b2Add(points[17], (b2Vec2){-2.0f * hx, 0.0f});
    points[19] = b2Add(points[18], (b2Vec2){-2.0f * hx, 0.0f});

    b2SurfaceMaterial material = {0};
    material.friction = 0.2f;
    b2ChainDef chainDef = b2DefaultChainDef();
    chainDef.points = points;
    chainDef.count = 20;
    chainDef.isLoop = true;
    chainDef.materials = &material;
    chainDef.materialCount = 1;
    b2CreateChain(groundId, &chainDef);

    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){-28.0f, 18.0f};
    bodyDef.linearVelocity = (b2Vec2){0.0f, 0.0f};
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.0f;
    shapeDef.material.friction = 0.2f;
    b2Circle circle = {{0.0f, 0.0f}, 0.5f};
    b2CreateCircleShape(bodyId, &shapeDef, &circle);

    SimulateAndPrint("ghostBumps", worldId, bodyId);
}

static void Drop(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2World_EnableSleeping(worldId, false);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    float width = 0.25f;
    int count = 40;
    float x = -0.5f * count * width;
    float halfHeight = 0.05f;
    for (int i = 0; i <= count; ++i)
    {
        b2Polygon box = b2MakeOffsetBox(0.5f * width, halfHeight, (b2Vec2){x, 0.0f}, b2Rot_identity);
        b2CreatePolygonShape(groundId, &shapeDef, &box);
        x += width;
    }

    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){0.0f, 4.0f};
    bodyDef.linearVelocity = (b2Vec2){0.0f, -100.0f};
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    b2Circle circle = {{0.0f, 0.0f}, 0.125f};
    shapeDef = b2DefaultShapeDef();
    b2CreateCircleShape(bodyId, &shapeDef, &circle);

    SimulateAndPrint("drop", worldId, bodyId);
}

static void ChainDrop(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.position = (b2Vec2){0.0f, -6.0f};
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    b2Vec2 points[4] = {{-10.0f, -2.0f}, {10.0f, -2.0f}, {10.0f, 1.0f}, {-10.0f, 1.0f}};
    b2ChainDef chainDef = b2DefaultChainDef();
    chainDef.points = points;
    chainDef.count = 4;
    chainDef.isLoop = true;
    b2CreateChain(groundId, &chainDef);

    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.linearVelocity = (b2Vec2){0.0f, -42.0f};
    bodyDef.position = (b2Vec2){0.0f, 9.9f};
    bodyDef.rotation = b2MakeRot(0.5f * B2_PI);
    bodyDef.fixedRotation = true;
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    b2Circle circle = {{0.0f, 0.0f}, 0.5f};
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2CreateCircleShape(bodyId, &shapeDef, &circle);

    SimulateAndPrint("chainDrop", worldId, bodyId);
}

static void ChainSlide(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);

    b2Vec2 points[80];
    float width = 2.0f;
    float height = 1.0f;
    float x = 20.0f;
    float y = 0.0f;
    for (int i = 0; i < 20; ++i)
    {
        points[i] = (b2Vec2){x, y};
        x -= width;
    }
    for (int i = 20; i < 40; ++i)
    {
        points[i] = (b2Vec2){x, y};
        y += height;
    }
    for (int i = 40; i < 60; ++i)
    {
        points[i] = (b2Vec2){x, y};
        x += width;
    }
    for (int i = 60; i < 80; ++i)
    {
        points[i] = (b2Vec2){x, y};
        y -= height;
    }
    b2ChainDef chainDef = b2DefaultChainDef();
    chainDef.points = points;
    chainDef.count = 80;
    chainDef.isLoop = true;
    b2CreateChain(groundId, &chainDef);

    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.linearVelocity = (b2Vec2){100.0f, 0.0f};
    bodyDef.position = (b2Vec2){-19.5f, 0.5f};
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.material.friction = 0.0f;
    b2Circle circle = {{0.0f, 0.0f}, 0.5f};
    b2CreateCircleShape(bodyId, &shapeDef, &circle);

    SimulateAndPrint("chainSlide", worldId, bodyId);
}

static void SegmentSlide(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Segment segment = {{-40.0f, 0.0f}, {40.0f, 0.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &segment);
    segment = (b2Segment){{40.0f, 0.0f}, {40.0f, 10.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &segment);

    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.linearVelocity = (b2Vec2){100.0f, 0.0f};
    bodyDef.position = (b2Vec2){-20.0f, 0.7f};
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    shapeDef = b2DefaultShapeDef();
    b2Circle circle = {{0.0f, 0.0f}, 0.5f};
    b2CreateCircleShape(bodyId, &shapeDef, &circle);

    SimulateAndPrint("segmentSlide", worldId, bodyId);
}

static void SkinnyBox(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.material.friction = 0.9f;
    b2Segment segment = {{-10.0f, 0.0f}, {10.0f, 0.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &segment);
    b2Polygon obstacle = b2MakeOffsetBox(0.1f, 1.0f, (b2Vec2){0.0f, 1.0f}, b2Rot_identity);
    b2CreatePolygonShape(groundId, &shapeDef, &obstacle);

    uint32_t random = 12345;
    random ^= random << 13;
    random ^= random >> 17;
    random ^= random << 5;
    float angularVelocity = (float)(random & 32767);
    angularVelocity /= 32767.0f;
    angularVelocity = 100.0f * angularVelocity - 50.0f;

    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){0.0f, 8.0f};
    bodyDef.angularVelocity = angularVelocity;
    bodyDef.linearVelocity = (b2Vec2){0.0f, -100.0f};
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.0f;
    shapeDef.material.friction = 0.9f;
    b2Polygon box = b2MakeBox(2.0f, 0.05f);
    b2CreatePolygonShape(bodyId, &shapeDef, &box);

    SimulateAndPrint("skinnyBox", worldId, bodyId);
}

static void SpeculativeFallback(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Segment segment = {{-10.0f, 0.0f}, {10.0f, 0.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &segment);
    b2Vec2 points[5] = {{-2.0f, 4.0f}, {2.0f, 4.0f}, {2.0f, 4.1f}, {-0.5f, 4.2f}, {-2.0f, 4.2f}};
    b2Hull hull = b2ComputeHull(points, 5);
    b2Polygon polygon = b2MakePolygon(&hull, 0.0f);
    b2CreatePolygonShape(groundId, &shapeDef, &polygon);

    float offset = 8.0f;
    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){offset, 12.0f};
    bodyDef.linearVelocity = (b2Vec2){0.0f, -100.0f};
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    polygon = b2MakeOffsetBox(2.0f, 0.05f, (b2Vec2){-offset, 0.0f}, b2MakeRot(B2_PI));
    shapeDef = b2DefaultShapeDef();
    b2CreatePolygonShape(bodyId, &shapeDef, &polygon);

    SimulateAndPrint("speculativeFallback", worldId, bodyId);
}

static void SpeculativeSliver(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Segment segment = {{-10.0f, 0.0f}, {10.0f, 0.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &segment);

    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){0.0f, 12.0f};
    bodyDef.linearVelocity = (b2Vec2){0.0f, -100.0f};
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    b2Vec2 points[3] = {{-2.0f, 0.0f}, {-1.0f, 0.0f}, {2.0f, 0.5f}};
    b2Hull hull = b2ComputeHull(points, 3);
    b2Polygon polygon = b2MakePolygon(&hull, 0.0f);
    shapeDef = b2DefaultShapeDef();
    b2CreatePolygonShape(bodyId, &shapeDef, &polygon);

    SimulateAndPrint("speculativeSliver", worldId, bodyId);
}

static void SpeculativeGhost(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Segment segment = {{-10.0f, 0.0f}, {10.0f, 0.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &segment);
    b2Polygon obstacle = b2MakeOffsetBox(1.0f, 0.1f, (b2Vec2){0.0f, 0.9f}, b2Rot_identity);
    b2CreatePolygonShape(groundId, &shapeDef, &obstacle);

    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){0.015f, 2.515f};
    bodyDef.linearVelocity = (b2Vec2){0.1f * 1.25f * 60.0f, -0.1f * 1.25f * 60.0f};
    bodyDef.gravityScale = 0.0f;
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    b2Polygon box = b2MakeSquare(0.25f);
    shapeDef = b2DefaultShapeDef();
    b2CreatePolygonShape(bodyId, &shapeDef, &box);

    SimulateAndPrint("speculativeGhost", worldId, bodyId);
}

static void PixelImperfect(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    float pixelsPerMeter = 30.0f;

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_staticBody;
    bodyDef.position = (b2Vec2){175.0f / pixelsPerMeter, 150.0f / pixelsPerMeter};
    b2BodyId blockId = b2CreateBody(worldId, &bodyDef);
    b2Polygon block = b2MakeBox(20.0f / pixelsPerMeter, 10.0f / pixelsPerMeter);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.material.friction = 0.0f;
    b2CreatePolygonShape(blockId, &shapeDef, &block);

    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){200.0f / pixelsPerMeter, 275.0f / pixelsPerMeter};
    bodyDef.gravityScale = 0.0f;
    b2BodyId ballId = b2CreateBody(worldId, &bodyDef);
    b2Polygon ball = b2MakeRoundedBox(4.0f / pixelsPerMeter, 4.0f / pixelsPerMeter, 0.9f / pixelsPerMeter);
    shapeDef = b2DefaultShapeDef();
    shapeDef.material.friction = 0.0f;
    b2CreatePolygonShape(ballId, &shapeDef, &ball);
    b2Body_SetLinearVelocity(ballId, (b2Vec2){0.0f, -5.0f});
    b2Body_SetFixedRotation(ballId, true);

    SimulateAndPrint("pixelImperfect", worldId, ballId);
}

static void RestitutionThreshold(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    float pixelsPerMeter = 30.0f;
    b2World_SetRestitutionThreshold(worldId, 0.1f);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_staticBody;
    bodyDef.position = (b2Vec2){205.0f / pixelsPerMeter, 120.0f / pixelsPerMeter};
    bodyDef.rotation = b2MakeRot(70.0f * 3.14f / 180.0f);
    b2BodyId blockId = b2CreateBody(worldId, &bodyDef);
    b2Polygon block = b2MakeBox(50.0f / pixelsPerMeter, 5.0f / pixelsPerMeter);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.material.friction = 0.0f;
    b2CreatePolygonShape(blockId, &shapeDef, &block);

    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){200.0f / pixelsPerMeter, 250.0f / pixelsPerMeter};
    b2BodyId ballId = b2CreateBody(worldId, &bodyDef);
    b2Circle ball = {{0.0f, 0.0f}, 5.0f / pixelsPerMeter};
    shapeDef = b2DefaultShapeDef();
    shapeDef.material.friction = 0.0f;
    shapeDef.material.restitution = 1.0f;
    b2CreateCircleShape(ballId, &shapeDef, &ball);
    b2Body_SetLinearVelocity(ballId, (b2Vec2){0.0f, -2.9f});
    b2Body_SetFixedRotation(ballId, true);

    SimulateAndPrint("restitutionThreshold", worldId, ballId);
}

static void Wedge(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Segment segment = {{-4.0f, 8.0f}, {0.0f, 0.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &segment);
    segment = (b2Segment){{0.0f, 0.0f}, {0.0f, 8.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &segment);

    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){-0.45f, 10.75f};
    bodyDef.linearVelocity = (b2Vec2){0.0f, -200.0f};
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    b2Circle circle = {{0.0f, 0.0f}, 0.3f};
    shapeDef = b2DefaultShapeDef();
    shapeDef.material.friction = 0.2f;
    b2CreateCircleShape(bodyId, &shapeDef, &circle);

    SimulateAndPrint("wedge", worldId, bodyId);
}

static b2JointId pinballLeftJoint;
static b2JointId pinballRightJoint;

static void PinballBeforeStep(int step)
{
    if (step > 1)
    {
        b2RevoluteJoint_SetMotorSpeed(pinballLeftJoint, -10.0f);
        b2RevoluteJoint_SetMotorSpeed(pinballRightJoint, 10.0f);
    }
}

static void Pinball(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    b2Vec2 boundary[5] = {{-8.0f, 6.0f}, {-8.0f, 20.0f}, {8.0f, 20.0f}, {8.0f, 6.0f}, {0.0f, -2.0f}};
    b2ChainDef chainDef = b2DefaultChainDef();
    chainDef.points = boundary;
    chainDef.count = 5;
    chainDef.isLoop = true;
    b2CreateChain(groundId, &chainDef);

    b2Vec2 leftAnchor = {-2.0f, 0.0f};
    b2Vec2 rightAnchor = {2.0f, 0.0f};
    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.enableSleep = false;
    bodyDef.position = leftAnchor;
    b2BodyId leftFlipperId = b2CreateBody(worldId, &bodyDef);
    bodyDef.position = rightAnchor;
    b2BodyId rightFlipperId = b2CreateBody(worldId, &bodyDef);
    b2Polygon flipper = b2MakeBox(1.75f, 0.2f);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2CreatePolygonShape(leftFlipperId, &shapeDef, &flipper);
    b2CreatePolygonShape(rightFlipperId, &shapeDef, &flipper);

    b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
    jointDef.bodyIdA = groundId;
    jointDef.localAnchorB = b2Vec2_zero;
    jointDef.enableMotor = true;
    jointDef.maxMotorTorque = 1000.0f;
    jointDef.enableLimit = true;
    jointDef.localAnchorA = leftAnchor;
    jointDef.bodyIdB = leftFlipperId;
    jointDef.lowerAngle = -30.0f * B2_PI / 180.0f;
    jointDef.upperAngle = 5.0f * B2_PI / 180.0f;
    pinballLeftJoint = b2CreateRevoluteJoint(worldId, &jointDef);

    jointDef.localAnchorA = rightAnchor;
    jointDef.bodyIdB = rightFlipperId;
    jointDef.lowerAngle = -5.0f * B2_PI / 180.0f;
    jointDef.upperAngle = 30.0f * B2_PI / 180.0f;
    pinballRightJoint = b2CreateRevoluteJoint(worldId, &jointDef);

    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){-4.0f, 17.0f};
    b2BodyId spinnerId = b2CreateBody(worldId, &bodyDef);
    b2Polygon spinnerHorizontal = b2MakeBox(1.5f, 0.125f);
    b2Polygon spinnerVertical = b2MakeBox(0.125f, 1.5f);
    shapeDef = b2DefaultShapeDef();
    b2CreatePolygonShape(spinnerId, &shapeDef, &spinnerHorizontal);
    b2CreatePolygonShape(spinnerId, &shapeDef, &spinnerVertical);
    jointDef = b2DefaultRevoluteJointDef();
    jointDef.bodyIdA = groundId;
    jointDef.bodyIdB = spinnerId;
    jointDef.localAnchorA = bodyDef.position;
    jointDef.localAnchorB = b2Vec2_zero;
    jointDef.enableMotor = true;
    jointDef.maxMotorTorque = 0.1f;
    b2CreateRevoluteJoint(worldId, &jointDef);

    bodyDef.position = (b2Vec2){4.0f, 8.0f};
    spinnerId = b2CreateBody(worldId, &bodyDef);
    b2CreatePolygonShape(spinnerId, &shapeDef, &spinnerHorizontal);
    b2CreatePolygonShape(spinnerId, &shapeDef, &spinnerVertical);
    jointDef.bodyIdB = spinnerId;
    jointDef.localAnchorA = bodyDef.position;
    b2CreateRevoluteJoint(worldId, &jointDef);

    bodyDef = b2DefaultBodyDef();
    bodyDef.position = (b2Vec2){-4.0f, 8.0f};
    b2BodyId bumperId = b2CreateBody(worldId, &bodyDef);
    shapeDef = b2DefaultShapeDef();
    shapeDef.material.restitution = 1.5f;
    b2Circle bumper = {{0.0f, 0.0f}, 1.0f};
    b2CreateCircleShape(bumperId, &shapeDef, &bumper);
    bodyDef.position = (b2Vec2){4.0f, 17.0f};
    bumperId = b2CreateBody(worldId, &bodyDef);
    b2CreateCircleShape(bumperId, &shapeDef, &bumper);

    bodyDef = b2DefaultBodyDef();
    bodyDef.position = (b2Vec2){1.0f, 15.0f};
    bodyDef.type = b2_dynamicBody;
    bodyDef.isBullet = true;
    b2BodyId ballId = b2CreateBody(worldId, &bodyDef);
    shapeDef = b2DefaultShapeDef();
    b2Circle ball = {{0.0f, 0.0f}, 0.2f};
    b2CreateCircleShape(ballId, &shapeDef, &ball);

    SimulateAndPrintWithAction("pinball", worldId, ballId, PinballBeforeStep);
}

int main(void)
{
    BounceHouse();
    BounceHumans();
    GhostBumps();
    Drop();
    ChainDrop();
    ChainSlide();
    SegmentSlide();
    SkinnyBox();
    SpeculativeFallback();
    SpeculativeSliver();
    SpeculativeGhost();
    PixelImperfect();
    RestitutionThreshold();
    Wedge();
    Pinball();
    return 0;
}
