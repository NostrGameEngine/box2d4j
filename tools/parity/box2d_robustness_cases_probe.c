// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <inttypes.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>

enum
{
    checkpoint_count = 12,
};
static const int checkpoint_steps[checkpoint_count] = {1, 2, 4, 8, 16, 32, 38, 39, 60, 61, 62, 120};
static const uint64_t fnv_offset = UINT64_C(0xcbf29ce484222325);
static const uint64_t fnv_prime = UINT64_C(0x100000001b3);

typedef struct BodyState
{
    int index;
    b2Vec2 position;
    b2Rot rotation;
    b2Vec2 velocity;
    float angularVelocity;
    int contactCount;
    int awake;
} BodyState;

typedef struct Checkpoint
{
    int step;
    int contactCount;
    int islandCount;
    int awakeBodyCount;
    int colorCounts[12];
    uint64_t stateHash;
    BodyState representatives[3];
} Checkpoint;

static uint32_t FloatBits(float value)
{
    uint32_t bits;
    memcpy(&bits, &value, sizeof(bits));
    return bits;
}

static uint64_t Mix(uint64_t hash, uint32_t value)
{
    return (hash ^ value) * fnv_prime;
}

static BodyState GetBodyState(int index, b2BodyId bodyId)
{
    b2ContactData contacts[16];
    BodyState state = {0};
    state.index = index;
    state.position = b2Body_GetPosition(bodyId);
    state.rotation = b2Body_GetRotation(bodyId);
    state.velocity = b2Body_GetLinearVelocity(bodyId);
    state.angularVelocity = b2Body_GetAngularVelocity(bodyId);
    state.contactCount = b2Body_GetContactData(bodyId, contacts, 16);
    state.awake = b2Body_IsAwake(bodyId) ? 1 : 0;
    return state;
}

static Checkpoint CaptureCheckpoint(int step, b2WorldId worldId, b2BodyId* bodies, int bodyCount)
{
    Checkpoint checkpoint = {0};
    checkpoint.step = step;
    uint64_t hash = fnv_offset;
    for (int i = 0; i < bodyCount; ++i)
    {
        b2Vec2 position = b2Body_GetPosition(bodies[i]);
        b2Rot rotation = b2Body_GetRotation(bodies[i]);
        b2Vec2 velocity = b2Body_GetLinearVelocity(bodies[i]);
        hash = Mix(hash, FloatBits(position.x));
        hash = Mix(hash, FloatBits(position.y));
        hash = Mix(hash, FloatBits(rotation.c));
        hash = Mix(hash, FloatBits(rotation.s));
        hash = Mix(hash, FloatBits(velocity.x));
        hash = Mix(hash, FloatBits(velocity.y));
        hash = Mix(hash, FloatBits(b2Body_GetAngularVelocity(bodies[i])));
        hash = Mix(hash, b2Body_IsAwake(bodies[i]) ? 1u : 0u);
    }
    checkpoint.stateHash = hash;
    b2Counters counters = b2World_GetCounters(worldId);
    checkpoint.contactCount = counters.contactCount;
    checkpoint.islandCount = counters.islandCount;
    checkpoint.awakeBodyCount = b2World_GetAwakeBodyCount(worldId);
    for (int i = 0; i < 12; ++i)
    {
        checkpoint.colorCounts[i] = counters.colorCounts[i];
    }
    int indices[3] = {0, bodyCount / 2, bodyCount - 1};
    for (int i = 0; i < 3; ++i)
    {
        checkpoint.representatives[i] = GetBodyState(indices[i], bodies[indices[i]]);
    }
    return checkpoint;
}

static void PrintBodyState(const BodyState* state)
{
    printf(" %d %.9g %.9g %.9g %.9g %.9g %.9g %.9g %d %d",
           state->index, state->position.x, state->position.y, state->rotation.c, state->rotation.s,
           state->velocity.x, state->velocity.y, state->angularVelocity, state->contactCount, state->awake);
}

static void SimulateAndPrint(const char* name, b2WorldId worldId, b2BodyId* bodies, int bodyCount)
{
    Checkpoint states[checkpoint_count] = {0};
    int checkpointIndex = 0;
    for (int step = 1; step <= checkpoint_steps[checkpoint_count - 1]; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        if (step == checkpoint_steps[checkpointIndex])
        {
            states[checkpointIndex] = CaptureCheckpoint(step, worldId, bodies, bodyCount);
            checkpointIndex += 1;
        }
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("%s %d %d %d %d %d %d %d %d", name, counters.bodyCount, counters.shapeCount,
           counters.contactCount, counters.jointCount, counters.islandCount, b2World_GetAwakeBodyCount(worldId),
           bodyCount, checkpoint_count);
    for (int i = 0; i < checkpoint_count; ++i)
    {
        Checkpoint* checkpoint = states + i;
        printf(" %d %d %d %d 12", checkpoint->step, checkpoint->contactCount,
               checkpoint->islandCount, checkpoint->awakeBodyCount);
        for (int j = 0; j < 12; ++j)
        {
            printf(" %d", checkpoint->colorCounts[j]);
        }
        printf(" %" PRIu64 " 3", checkpoint->stateHash);
        for (int j = 0; j < 3; ++j)
        {
            PrintBodyState(checkpoint->representatives + j);
        }
    }
    printf("\n");
    b2DestroyWorld(worldId);
}

static void HighMassRatio1(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Polygon ground = b2MakeOffsetBox(50.0f, 1.0f, (b2Vec2){0.0f, -1.0f}, b2Rot_identity);
    b2CreatePolygonShape(groundId, &shapeDef, &ground);

    float extent = 1.0f;
    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    b2Polygon box = b2MakeBox(extent, extent);
    shapeDef = b2DefaultShapeDef();
    b2BodyId bodies[165];
    int bodyIndex = 0;
    for (int pyramidIndex = 0; pyramidIndex < 3; ++pyramidIndex)
    {
        int count = 10;
        float offset = -20.0f * extent + 2.0f * (count + 1.0f) * extent * pyramidIndex;
        float y = extent;
        while (count > 0)
        {
            for (int i = 0; i < count; ++i)
            {
                float coefficient = i - 0.5f * count;
                float bodyY = count == 1 ? y + 2.0f : y;
                bodyDef.position = (b2Vec2){2.0f * coefficient * extent + offset, bodyY};
                bodies[bodyIndex] = b2CreateBody(worldId, &bodyDef);
                shapeDef.density = count == 1 ? (pyramidIndex + 1.0f) * 100.0f : 1.0f;
                b2CreatePolygonShape(bodies[bodyIndex], &shapeDef, &box);
                bodyIndex += 1;
            }
            count -= 1;
            y += 2.0f * extent;
        }
    }
    SimulateAndPrint("highMassRatio1", worldId, bodies, bodyIndex);
}

static void HighMassRatio2(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Polygon ground = b2MakeOffsetBox(50.0f, 1.0f, (b2Vec2){0.0f, -1.0f}, b2Rot_identity);
    b2CreatePolygonShape(groundId, &shapeDef, &ground);

    float extent = 1.0f;
    b2Polygon smallBox = b2MakeBox(0.5f * extent, 0.5f * extent);
    b2Polygon bigBox = b2MakeBox(10.0f * extent, 10.0f * extent);
    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    shapeDef = b2DefaultShapeDef();
    b2BodyId bodies[3];
    bodyDef.position = (b2Vec2){-9.0f * extent, 0.5f * extent};
    bodies[0] = b2CreateBody(worldId, &bodyDef);
    b2CreatePolygonShape(bodies[0], &shapeDef, &smallBox);
    bodyDef.position = (b2Vec2){9.0f * extent, 0.5f * extent};
    bodies[1] = b2CreateBody(worldId, &bodyDef);
    b2CreatePolygonShape(bodies[1], &shapeDef, &smallBox);
    bodyDef.position = (b2Vec2){0.0f, (10.0f + 16.0f) * extent};
    bodies[2] = b2CreateBody(worldId, &bodyDef);
    b2CreatePolygonShape(bodies[2], &shapeDef, &bigBox);
    SimulateAndPrint("highMassRatio2", worldId, bodies, 3);
}

static void HighMassRatio3(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Polygon ground = b2MakeOffsetBox(50.0f, 1.0f, (b2Vec2){0.0f, -1.0f}, b2Rot_identity);
    b2CreatePolygonShape(groundId, &shapeDef, &ground);

    float extent = 1.0f;
    b2Vec2 points[3] = {{-0.5f * extent, 0.0f}, {0.5f * extent, 0.0f}, {0.0f, extent}};
    b2Hull hull = b2ComputeHull(points, 3);
    b2Polygon triangle = b2MakePolygon(&hull, 0.0f);
    b2Polygon bigBox = b2MakeBox(10.0f * extent, 10.0f * extent);
    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    shapeDef = b2DefaultShapeDef();
    b2BodyId bodies[3];
    bodyDef.position = (b2Vec2){-9.0f * extent, 0.5f * extent};
    bodies[0] = b2CreateBody(worldId, &bodyDef);
    b2CreatePolygonShape(bodies[0], &shapeDef, &triangle);
    bodyDef.position = (b2Vec2){9.0f * extent, 0.5f * extent};
    bodies[1] = b2CreateBody(worldId, &bodyDef);
    b2CreatePolygonShape(bodies[1], &shapeDef, &triangle);
    bodyDef.position = (b2Vec2){0.0f, (10.0f + 4.0f) * extent};
    bodies[2] = b2CreateBody(worldId, &bodyDef);
    b2CreatePolygonShape(bodies[2], &shapeDef, &bigBox);
    SimulateAndPrint("highMassRatio3", worldId, bodies, 3);
}

static void OverlapRecovery(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.0f;
    b2Segment segment = {{-40.0f, 0.0f}, {40.0f, 0.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &segment);

    int baseCount = 4;
    float overlap = 0.25f;
    float extent = 0.5f;
    b2World_SetContactTuning(worldId, 30.0f, 10.0f, 3.0f);
    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    b2Polygon box = b2MakeBox(extent, extent);
    shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.0f;
    b2BodyId bodies[10];
    int bodyIndex = 0;
    float fraction = 1.0f - overlap;
    float y = extent;
    for (int i = 0; i < baseCount; ++i)
    {
        float x = fraction * extent * (i - baseCount);
        for (int j = i; j < baseCount; ++j)
        {
            bodyDef.position = (b2Vec2){x, y};
            bodies[bodyIndex] = b2CreateBody(worldId, &bodyDef);
            b2CreatePolygonShape(bodies[bodyIndex], &shapeDef, &box);
            bodyIndex += 1;
            x += 2.0f * fraction * extent;
        }
        y += 2.0f * fraction * extent;
    }
    SimulateAndPrint("overlapRecovery", worldId, bodies, bodyIndex);
}

static void TinyPyramid(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Polygon ground = b2MakeOffsetBox(5.0f, 1.0f, (b2Vec2){0.0f, -1.0f}, b2Rot_identity);
    b2CreatePolygonShape(groundId, &shapeDef, &ground);

    float extent = 0.025f;
    int baseCount = 30;
    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    shapeDef = b2DefaultShapeDef();
    b2Polygon box = b2MakeSquare(extent);
    b2BodyId bodies[465];
    int bodyIndex = 0;
    for (int i = 0; i < baseCount; ++i)
    {
        float y = (2.0f * i + 1.0f) * extent;
        for (int j = i; j < baseCount; ++j)
        {
            float x = (i + 1.0f) * extent + 2.0f * (j - i) * extent - baseCount * extent;
            bodyDef.position = (b2Vec2){x, y};
            bodies[bodyIndex] = b2CreateBody(worldId, &bodyDef);
            b2CreatePolygonShape(bodies[bodyIndex], &shapeDef, &box);
            bodyIndex += 1;
        }
    }
    SimulateAndPrint("tinyPyramid", worldId, bodies, bodyIndex);
}

static void Cart(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.position = (b2Vec2){0.0f, -1.0f};
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Polygon ground = b2MakeBox(20.0f, 1.0f);
    b2CreatePolygonShape(groundId, &shapeDef, &ground);

    b2World_SetGravity(worldId, (b2Vec2){0.0f, -22.0f});
    b2World_SetContactTuning(worldId, 30.0f, 10.0f, 3.0f);
    float yBase = 2.0f;
    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){0.0f, yBase};
    b2BodyId chassisId = b2CreateBody(worldId, &bodyDef);
    shapeDef = b2DefaultShapeDef();
    shapeDef.density = 100.0f;
    b2Polygon chassis = b2MakeOffsetBox(0.5f, 0.25f, (b2Vec2){0.0f, 0.25f}, b2Rot_identity);
    b2CreatePolygonShape(chassisId, &shapeDef, &chassis);

    shapeDef = b2DefaultShapeDef();
    shapeDef.material.rollingResistance = 0.02f;
    shapeDef.density = 10.0f;
    b2Circle wheel = {b2Vec2_zero, 0.1f};
    bodyDef.position = (b2Vec2){-0.4f, yBase - 0.15f};
    b2BodyId wheel1Id = b2CreateBody(worldId, &bodyDef);
    b2CreateCircleShape(wheel1Id, &shapeDef, &wheel);
    bodyDef.position = (b2Vec2){0.4f, yBase - 0.15f};
    b2BodyId wheel2Id = b2CreateBody(worldId, &bodyDef);
    b2CreateCircleShape(wheel2Id, &shapeDef, &wheel);

    b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
    jointDef.bodyIdA = chassisId;
    jointDef.bodyIdB = wheel1Id;
    jointDef.localAnchorA = (b2Vec2){-0.4f, -0.15f};
    jointDef.localAnchorB = b2Vec2_zero;
    b2JointId joint1Id = b2CreateRevoluteJoint(worldId, &jointDef);
    b2Joint_SetConstraintTuning(joint1Id, 60.0f, 1.0f);
    jointDef.bodyIdB = wheel2Id;
    jointDef.localAnchorA = (b2Vec2){0.4f, -0.15f};
    b2JointId joint2Id = b2CreateRevoluteJoint(worldId, &jointDef);
    b2Joint_SetConstraintTuning(joint2Id, 60.0f, 1.0f);

    b2BodyId bodies[3] = {chassisId, wheel1Id, wheel2Id};
    SimulateAndPrint("cart", worldId, bodies, 3);
}

int main(void)
{
    HighMassRatio1();
    HighMassRatio2();
    HighMassRatio3();
    OverlapRecovery();
    TinyPyramid();
    Cart();
    return 0;
}
