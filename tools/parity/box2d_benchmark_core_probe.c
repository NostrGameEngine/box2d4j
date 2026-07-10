// SPDX-License-Identifier: MIT

#include "benchmarks.h"
#include "body.h"
#include "random.h"
#include "world.h"

#include "box2d/box2d.h"

#include <inttypes.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>

static const uint64_t fnv_offset = UINT64_C(0xcbf29ce484222325);
static const uint64_t fnv_prime = UINT64_C(0x100000001b3);

typedef struct Checkpoint
{
    int step;
    b2Counters counters;
    int awakeBodyCount;
    int validBodyCount;
    uint64_t bodyHash;
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

static Checkpoint Capture(int step, b2WorldId worldId)
{
    Checkpoint checkpoint = {0};
    checkpoint.step = step;
    checkpoint.counters = b2World_GetCounters(worldId);
    checkpoint.awakeBodyCount = b2World_GetAwakeBodyCount(worldId);

    b2World* world = b2GetWorldFromId(worldId);
    uint64_t hash = fnv_offset;
    for (int bodyIndex = 0; bodyIndex < world->bodies.count; ++bodyIndex)
    {
        b2Body* body = world->bodies.data + bodyIndex;
        bool valid = body->setIndex != B2_NULL_INDEX;
        hash = Mix(hash, valid ? 1u : 0u);
        if (valid == false)
        {
            continue;
        }

        checkpoint.validBodyCount += 1;
        b2BodyId bodyId = {bodyIndex + 1, world->worldId, body->generation};
        b2Vec2 position = b2Body_GetPosition(bodyId);
        b2Rot rotation = b2Body_GetRotation(bodyId);
        b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
        hash = Mix(hash, (uint32_t)b2Body_GetType(bodyId));
        hash = Mix(hash, FloatBits(position.x));
        hash = Mix(hash, FloatBits(position.y));
        hash = Mix(hash, FloatBits(rotation.c));
        hash = Mix(hash, FloatBits(rotation.s));
        hash = Mix(hash, FloatBits(velocity.x));
        hash = Mix(hash, FloatBits(velocity.y));
        hash = Mix(hash, FloatBits(b2Body_GetAngularVelocity(bodyId)));
        hash = Mix(hash, b2Body_IsAwake(bodyId) ? 1u : 0u);
        hash = Mix(hash, (uint32_t)b2Body_GetShapeCount(bodyId));
        hash = Mix(hash, (uint32_t)b2Body_GetContactCapacity(bodyId));
    }
    checkpoint.bodyHash = hash;
    return checkpoint;
}

static void PrintCheckpoint(const Checkpoint* checkpoint)
{
    printf(" %d %d %d %d %d %d %d %d 12", checkpoint->step,
           checkpoint->counters.bodyCount, checkpoint->counters.shapeCount,
           checkpoint->counters.contactCount, checkpoint->counters.jointCount,
           checkpoint->counters.islandCount, checkpoint->awakeBodyCount,
           checkpoint->validBodyCount);
    for (int i = 0; i < 12; ++i)
    {
        printf(" %d", checkpoint->counters.colorCounts[i]);
    }
    printf(" %" PRIu64, checkpoint->bodyHash);
}

typedef void StepCallback(b2WorldId worldId, int stepCount, void* context);
typedef uint64_t WorkloadCallback(void* context);

static void SimulateAndPrintWithCallbacks(const char* name, b2WorldId worldId, int stepCount,
                                          StepCallback* beforeStep, StepCallback* afterStep, void* context,
                                          WorkloadCallback* getWorkloadHash)
{
    Checkpoint initial = Capture(0, worldId);
    for (int step = 0; step < stepCount; ++step)
    {
        if (beforeStep != NULL)
        {
            beforeStep(worldId, step, context);
        }
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        if (afterStep != NULL)
        {
            afterStep(worldId, step + 1, context);
        }
    }
    Checkpoint result = Capture(stepCount, worldId);
    uint64_t workloadHash = getWorkloadHash != NULL ? getWorkloadHash(context) : 0;
    printf("%s", name);
    PrintCheckpoint(&initial);
    PrintCheckpoint(&result);
    printf(" %" PRIu64 "\n", workloadHash);
    b2DestroyWorld(worldId);
}

static void SimulateAndPrint(const char* name, b2WorldId worldId, int stepCount)
{
    SimulateAndPrintWithCallbacks(name, worldId, stepCount, NULL, NULL, NULL, NULL);
}

static b2WorldId CreateBarrel(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyDef groundBodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &groundBodyDef);
    b2ShapeDef groundShapeDef = b2DefaultShapeDef();

    float x = -40.0f;
    for (int i = 0; i < 81; ++i)
    {
        b2Polygon box = b2MakeOffsetBox(0.5f, 0.5f, (b2Vec2){x, 0.0f}, b2Rot_identity);
        b2CreatePolygonShape(groundId, &groundShapeDef, &box);
        x += 1.0f;
    }
    float y = 1.0f;
    for (int i = 0; i < 100; ++i)
    {
        b2Polygon box = b2MakeOffsetBox(0.5f, 0.5f, (b2Vec2){-40.0f, y}, b2Rot_identity);
        b2CreatePolygonShape(groundId, &groundShapeDef, &box);
        y += 1.0f;
    }
    y = 1.0f;
    for (int i = 0; i < 100; ++i)
    {
        b2Polygon box = b2MakeOffsetBox(0.5f, 0.5f, (b2Vec2){40.0f, y}, b2Rot_identity);
        b2CreatePolygonShape(groundId, &groundShapeDef, &box);
        y += 1.0f;
    }
    b2Segment segment = {{-800.0f, -80.0f}, {800.0f, -80.0f}};
    b2CreateSegmentShape(groundId, &groundShapeDef, &segment);

    b2Vec2 vertices[3] = {{-1.0f, 0.0f}, {0.5f, 1.0f}, {0.0f, 2.0f}};
    b2Hull hull = b2ComputeHull(vertices, 3);
    b2Polygon left = b2MakePolygon(&hull, 0.0f);
    vertices[0] = (b2Vec2){1.0f, 0.0f};
    vertices[1] = (b2Vec2){-0.5f, 1.0f};
    vertices[2] = (b2Vec2){0.0f, 2.0f};
    hull = b2ComputeHull(vertices, 3);
    b2Polygon right = b2MakePolygon(&hull, 0.0f);

    int columnCount = 20;
    int rowCount = 150;
    float shift = 2.0f;
    float centerX = shift * columnCount / 2.0f - 1.0f;
    float centerY = 1.15f / 2.0f;
    float side = 0.25f;
    float extraY = 0.25f;
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.0f;
    shapeDef.material.friction = 0.5f;
    for (int i = 0; i < columnCount; ++i)
    {
        x = i * shift - centerX;
        for (int j = 0; j < rowCount; ++j)
        {
            y = j * (shift + extraY) + centerY + 100.0f;
            bodyDef.position = (b2Vec2){x + side, y};
            side = -side;
            b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
            b2CreatePolygonShape(bodyId, &shapeDef, &left);
            b2CreatePolygonShape(bodyId, &shapeDef, &right);
        }
    }
    return worldId;
}

static b2WorldId NewWorld(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    return b2CreateWorld(&worldDef);
}

typedef struct ManyTumblersState
{
    b2Vec2 positions[4];
    int bodyIndex;
} ManyTumblersState;

static void CreateSmallTumbler(b2WorldId worldId, b2Vec2 position)
{
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_kinematicBody;
    bodyDef.position = position;
    bodyDef.angularVelocity = (B2_PI / 180.0f) * 25.0f;
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 50.0f;
    b2Polygon polygon = b2MakeOffsetBox(0.25f, 2.0f, (b2Vec2){2.0f, 0.0f}, b2Rot_identity);
    b2CreatePolygonShape(bodyId, &shapeDef, &polygon);
    polygon = b2MakeOffsetBox(0.25f, 2.0f, (b2Vec2){-2.0f, 0.0f}, b2Rot_identity);
    b2CreatePolygonShape(bodyId, &shapeDef, &polygon);
    polygon = b2MakeOffsetBox(2.0f, 0.25f, (b2Vec2){0.0f, 2.0f}, b2Rot_identity);
    b2CreatePolygonShape(bodyId, &shapeDef, &polygon);
    polygon = b2MakeOffsetBox(2.0f, 0.25f, (b2Vec2){0.0f, -2.0f}, b2Rot_identity);
    b2CreatePolygonShape(bodyId, &shapeDef, &polygon);
}

static b2WorldId CreateManyTumblers(ManyTumblersState* state)
{
    memset(state, 0, sizeof(*state));
    b2WorldId worldId = NewWorld();
    b2BodyDef groundDef = b2DefaultBodyDef();
    b2CreateBody(worldId, &groundDef);
    int index = 0;
    float x = -8.0f;
    for (int i = 0; i < 2; ++i)
    {
        float y = -8.0f;
        for (int j = 0; j < 2; ++j)
        {
            state->positions[index] = (b2Vec2){x, y};
            CreateSmallTumbler(worldId, state->positions[index]);
            index += 1;
            y += 8.0f;
        }
        x += 8.0f;
    }
    return worldId;
}

static void StepManyTumblers(b2WorldId worldId, int stepCount, void* context)
{
    ManyTumblersState* state = context;
    if (state->bodyIndex >= 32 || (stepCount & 0x7) != 0)
    {
        return;
    }
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Capsule capsule = {{-0.1f, 0.0f}, {0.1f, 0.0f}, 0.075f};
    for (int i = 0; i < 4; ++i)
    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = state->positions[i];
        b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
        b2CreateCapsuleShape(bodyId, &shapeDef, &capsule);
        state->bodyIndex += 1;
    }
}

typedef struct CreateDestroyState
{
    b2BodyId bodies[5050];
    int bodyCount;
} CreateDestroyState;

static b2WorldId CreateCreateDestroy(void)
{
    b2WorldId worldId = NewWorld();
    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    b2Polygon box = b2MakeBox(100.0f, 1.0f);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2CreatePolygonShape(groundId, &shapeDef, &box);
    return worldId;
}

static void StepCreateDestroy(b2WorldId worldId, int step, void* context)
{
    (void)step;
    CreateDestroyState* state = context;
    for (int i = 0; i < state->bodyCount; ++i)
    {
        b2DestroyBody(state->bodies[i]);
    }

    int count = 40;
    float shift = 1.0f;
    float centerX = shift * count / 2.0f;
    float centerY = shift / 2.0f + 1.0f;
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.0f;
    shapeDef.material.friction = 0.5f;
    b2Polygon box = b2MakeRoundedBox(0.5f, 0.5f, 0.0f);
    int index = 0;
    for (int i = 0; i < count; ++i)
    {
        float y = i * shift + centerY;
        for (int j = i; j < count; ++j)
        {
            float x = 0.5f * i * shift + (j - i) * shift - centerX;
            bodyDef.position = (b2Vec2){x, y};
            state->bodies[index] = b2CreateBody(worldId, &bodyDef);
            b2CreatePolygonShape(state->bodies[index], &shapeDef, &box);
            index += 1;
        }
    }
    state->bodyCount = index;
    b2World_Step(worldId, 1.0f / 60.0f, 4);
}

typedef struct SleepState
{
    b2BodyId bodies[5050];
    bool awake;
} SleepState;

static b2WorldId CreateSleep(SleepState* state)
{
    memset(state, 0, sizeof(*state));
    b2WorldId worldId = CreateCreateDestroy();
    int count = 40;
    float shift = 1.0f;
    float centerX = shift * count / 2.0f;
    float centerY = shift / 2.0f + 1.0f;
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.0f;
    shapeDef.material.friction = 0.5f;
    b2Polygon box = b2MakeRoundedBox(0.5f, 0.5f, 0.0f);
    int index = 0;
    for (int i = 0; i < count; ++i)
    {
        float y = i * shift + centerY;
        for (int j = i; j < count; ++j)
        {
            float x = 0.5f * i * shift + (j - i) * shift - centerX;
            bodyDef.position = (b2Vec2){x, y};
            state->bodies[index] = b2CreateBody(worldId, &bodyDef);
            b2CreatePolygonShape(state->bodies[index], &shapeDef, &box);
            index += 1;
        }
    }
    return worldId;
}

static void StepSleep(b2WorldId worldId, int step, void* context)
{
    (void)worldId;
    (void)step;
    SleepState* state = context;
    b2Body_SetAwake(state->bodies[0], state->awake);
    state->awake = !state->awake;
}

static b2WorldId CreateCompound(void)
{
    b2WorldId worldId = NewWorld();
    float grid = 1.0f;
    int height = 100;
    int width = 100;
    b2BodyDef groundDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &groundDef);
    b2ShapeDef groundShapeDef = b2DefaultShapeDef();
    for (int i = 0; i < height; ++i)
    {
        float y = grid * i;
        for (int j = i; j < width; ++j)
        {
            float x = grid * j;
            b2Polygon square = b2MakeOffsetBox(0.5f * grid, 0.5f * grid, (b2Vec2){x, y}, b2Rot_identity);
            b2CreatePolygonShape(groundId, &groundShapeDef, &square);
        }
    }
    for (int i = 0; i < height; ++i)
    {
        float y = grid * i;
        for (int j = i; j < width; ++j)
        {
            float x = -grid * j;
            b2Polygon square = b2MakeOffsetBox(0.5f * grid, 0.5f * grid, (b2Vec2){x, y}, b2Rot_identity);
            b2CreatePolygonShape(groundId, &groundShapeDef, &square);
        }
    }

    int span = 5;
    int count = 5;
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.updateBodyMass = false;
    for (int m = 0; m < count; ++m)
    {
        float bodyY = (100.0f + m * span) * grid;
        for (int n = 0; n < count; ++n)
        {
            float bodyX = -0.5f * grid * count * span + n * span * grid;
            bodyDef.position = (b2Vec2){bodyX, bodyY};
            b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
            for (int i = 0; i < span; ++i)
            {
                float y = i * grid;
                for (int j = 0; j < span; ++j)
                {
                    float x = j * grid;
                    b2Polygon square = b2MakeOffsetBox(0.5f * grid, 0.5f * grid, (b2Vec2){x, y}, b2Rot_identity);
                    b2CreatePolygonShape(bodyId, &shapeDef, &square);
                }
            }
            b2Body_ApplyMassFromShapes(bodyId);
        }
    }
    return worldId;
}

static b2WorldId CreateKinematic(void)
{
    b2WorldId worldId = NewWorld();
    float grid = 1.0f;
    int span = 20;
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_kinematicBody;
    bodyDef.angularVelocity = 1.0f;
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.filter.categoryBits = 1;
    shapeDef.filter.maskBits = 2;
    shapeDef.updateBodyMass = false;
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    for (int i = -span; i < span; ++i)
    {
        float y = i * grid;
        for (int j = -span; j < span; ++j)
        {
            float x = j * grid;
            b2Polygon square = b2MakeOffsetBox(0.5f * grid, 0.5f * grid, (b2Vec2){x, y}, b2Rot_identity);
            b2CreatePolygonShape(bodyId, &shapeDef, &square);
        }
    }
    b2Body_ApplyMassFromShapes(bodyId);
    return worldId;
}

static uint64_t MixLong(uint64_t hash, uint64_t value)
{
    hash = Mix(hash, (uint32_t)value);
    return Mix(hash, (uint32_t)(value >> 32));
}

typedef struct CastResult
{
    b2Vec2 point;
    float fraction;
    bool hit;
} CastResult;

typedef struct CastState
{
    b2Vec2 origins[100];
    b2Vec2 translations[100];
    uint64_t hash;
} CastState;

static float BenchmarkCastCallback(b2ShapeId shapeId, b2Vec2 point, b2Vec2 normal, float fraction, void* context)
{
    (void)shapeId;
    (void)normal;
    CastResult* result = context;
    result->point = point;
    result->fraction = fraction;
    result->hit = true;
    return fraction;
}

static b2WorldId CreateCast(CastState* state)
{
    memset(state, 0, sizeof(*state));
    state->hash = fnv_offset;
    g_randomSeed = 1234;
    for (int i = 0; i < 100; ++i)
    {
        b2Vec2 start = RandomVec2(0.0f, 100.0f);
        b2Vec2 end = RandomVec2(0.0f, 100.0f);
        state->origins[i] = start;
        state->translations[i] = b2Sub(end, start);
    }

    b2WorldId worldId = NewWorld();
    g_randomSeed = 1234;
    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    float y = 0.0f;
    for (int i = 0; i < 100; ++i)
    {
        float x = 0.0f;
        for (int j = 0; j < 100; ++j)
        {
            if (RandomFloatRange(0.0f, 1.0f) <= 0.1f)
            {
                bodyDef.position = (b2Vec2){x, y};
                b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
                float ratio = RandomFloatRange(1.0f, 5.0f);
                float halfWidth = RandomFloatRange(0.05f, 0.25f);
                b2Polygon box = RandomFloat() > 0.0f ? b2MakeBox(ratio * halfWidth, halfWidth)
                                                       : b2MakeBox(halfWidth, ratio * halfWidth);
                int category = RandomIntRange(0, 2);
                shapeDef.filter.categoryBits = UINT64_C(1) << category;
                shapeDef.material.customColor = category == 0 ? b2_colorBox2DBlue
                                                : category == 1 ? b2_colorBox2DYellow : b2_colorBox2DGreen;
                b2CreatePolygonShape(bodyId, &shapeDef, &box);
            }
            x += 1.0f;
        }
        y += 1.0f;
    }
    return worldId;
}

static void StepCast(b2WorldId worldId, int step, void* context)
{
    (void)step;
    CastState* state = context;
    b2QueryFilter filter = b2DefaultQueryFilter();
    filter.maskBits = 1;
    for (int i = 0; i < 100; ++i)
    {
        b2ShapeProxy proxy = b2MakeProxy(state->origins + i, 1, 0.1f);
        CastResult result = {0};
        b2TreeStats stats = b2World_CastShape(worldId, &proxy, state->translations[i], filter,
                                              BenchmarkCastCallback, &result);
        state->hash = Mix(state->hash, result.hit ? 1u : 0u);
        state->hash = Mix(state->hash, (uint32_t)stats.nodeVisits);
        state->hash = Mix(state->hash, (uint32_t)stats.leafVisits);
        if (result.hit)
        {
            state->hash = Mix(state->hash, FloatBits(result.point.x));
            state->hash = Mix(state->hash, FloatBits(result.point.y));
            state->hash = Mix(state->hash, FloatBits(result.fraction));
        }
    }
}

static uint64_t GetCastHash(void* context)
{
    return ((CastState*)context)->hash;
}

typedef struct ShapeDistanceState
{
    b2Polygon polygonA;
    b2Polygon polygonB;
    b2Transform transformsA[100];
    b2Transform transformsB[100];
    uint64_t hash;
} ShapeDistanceState;

static b2Polygon MakeBenchmarkOctagon(float radius)
{
    b2Vec2 points[8] = {0};
    b2Rot rotation = b2MakeRot(2.0f * B2_PI / 8.0f);
    points[0] = (b2Vec2){0.5f, 0.0f};
    for (int i = 1; i < 8; ++i)
    {
        points[i] = b2RotateVector(rotation, points[i - 1]);
    }
    b2Hull hull = b2ComputeHull(points, 8);
    return b2MakePolygon(&hull, radius);
}

static b2WorldId CreateShapeDistance(ShapeDistanceState* state)
{
    memset(state, 0, sizeof(*state));
    state->hash = fnv_offset;
    state->polygonA = MakeBenchmarkOctagon(0.0f);
    state->polygonB = MakeBenchmarkOctagon(0.1f);
    g_randomSeed = 42;
    for (int i = 0; i < 100; ++i)
    {
        state->transformsA[i] = (b2Transform){RandomVec2(-0.1f, 0.1f), RandomRot()};
        state->transformsB[i] = (b2Transform){RandomVec2(0.25f, 2.0f), RandomRot()};
    }
    return NewWorld();
}

static void StepShapeDistance(b2WorldId worldId, int step, void* context)
{
    (void)worldId;
    (void)step;
    ShapeDistanceState* state = context;
    b2DistanceInput input = {0};
    input.proxyA = b2MakeProxy(state->polygonA.vertices, state->polygonA.count, state->polygonA.radius);
    input.proxyB = b2MakeProxy(state->polygonB.vertices, state->polygonB.count, state->polygonB.radius);
    input.useRadii = true;
    for (int i = 0; i < 100; ++i)
    {
        input.transformA = state->transformsA[i];
        input.transformB = state->transformsB[i];
        b2SimplexCache cache = {0};
        b2DistanceOutput output = b2ShapeDistance(&input, &cache, NULL, 0);
        state->hash = Mix(state->hash, FloatBits(output.pointA.x));
        state->hash = Mix(state->hash, FloatBits(output.pointA.y));
        state->hash = Mix(state->hash, FloatBits(output.pointB.x));
        state->hash = Mix(state->hash, FloatBits(output.pointB.y));
        state->hash = Mix(state->hash, FloatBits(output.normal.x));
        state->hash = Mix(state->hash, FloatBits(output.normal.y));
        state->hash = Mix(state->hash, FloatBits(output.distance));
        state->hash = Mix(state->hash, (uint32_t)output.iterations);
        state->hash = Mix(state->hash, (uint32_t)output.simplexCount);
    }
}

static uint64_t GetShapeDistanceHash(void* context)
{
    return ((ShapeDistanceState*)context)->hash;
}

static void StepRainCallback(b2WorldId worldId, int stepCount, void* context)
{
    (void)context;
    StepRain(worldId, stepCount);
}

typedef struct ShapeUserData
{
    bool shouldDestroyVisitors;
} ShapeUserData;

typedef struct SensorState
{
    ShapeUserData passiveSensor;
    ShapeUserData activeSensor;
    uint64_t hash;
    int lastStepCount;
} SensorState;

static b2WorldId CreateSensor(SensorState* state)
{
    memset(state, 0, sizeof(*state));
    state->activeSensor.shouldDestroyVisitors = true;
    state->hash = fnv_offset;
    b2WorldId worldId = NewWorld();
    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.isSensor = true;
    shapeDef.enableSensorEvents = true;
    shapeDef.userData = &state->activeSensor;
    float x = -120.0f;
    for (int i = 0; i < 81; ++i)
    {
        b2Polygon box = b2MakeOffsetBox(1.5f, 1.5f, (b2Vec2){x, 0.0f}, b2Rot_identity);
        b2CreatePolygonShape(groundId, &shapeDef, &box);
        x += 3.0f;
    }

    g_randomSeed = 42;
    shapeDef = b2DefaultShapeDef();
    shapeDef.isSensor = true;
    shapeDef.enableSensorEvents = true;
    shapeDef.userData = &state->passiveSensor;
    for (int row = 0; row < 40; ++row)
    {
        float y = row * 5.0f + 10.0f;
        for (int column = 0; column < 40; ++column)
        {
            x = column * 5.0f - 100.0f;
            float yOffset = RandomFloatRange(-1.0f, 1.0f);
            b2Polygon box = b2MakeOffsetRoundedBox(0.5f, 0.5f, (b2Vec2){x, y + yOffset}, RandomRot(), 0.1f);
            b2CreatePolygonShape(groundId, &shapeDef, &box);
        }
    }
    return worldId;
}

static void CreateSensorRow(b2WorldId worldId, float y)
{
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.gravityScale = 0.0f;
    bodyDef.linearVelocity = (b2Vec2){0.0f, -5.0f};
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.enableSensorEvents = true;
    b2Circle circle = {{0.0f, 0.0f}, 0.5f};
    for (int i = 0; i < 40; ++i)
    {
        bodyDef.position = (b2Vec2){5.0f * i - 100.0f, y};
        b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
        b2CreateCircleShape(bodyId, &shapeDef, &circle);
    }
}

static void StepSensor(b2WorldId worldId, int stepCount, void* context)
{
    SensorState* state = context;
    if (stepCount == state->lastStepCount)
    {
        return;
    }
    b2SensorEvents events = b2World_GetSensorEvents(worldId);
    state->hash = Mix(state->hash, (uint32_t)stepCount);
    state->hash = Mix(state->hash, (uint32_t)events.beginCount);
    state->hash = Mix(state->hash, (uint32_t)events.endCount);
    b2BodyId zombies[256];
    uint64_t zombieKeys[256];
    int zombieCount = 0;
    for (int i = 0; i < events.beginCount; ++i)
    {
        b2SensorBeginTouchEvent* event = events.beginEvents + i;
        state->hash = MixLong(state->hash, b2StoreShapeId(event->sensorShapeId));
        state->hash = MixLong(state->hash, b2StoreShapeId(event->visitorShapeId));
        ShapeUserData* userData = b2Shape_GetUserData(event->sensorShapeId);
        if (userData->shouldDestroyVisitors)
        {
            b2BodyId bodyId = b2Shape_GetBody(event->visitorShapeId);
            uint64_t key = b2StoreBodyId(bodyId);
            bool duplicate = false;
            for (int j = 0; j < zombieCount; ++j)
            {
                duplicate = duplicate || zombieKeys[j] == key;
            }
            if (!duplicate && zombieCount < 256)
            {
                int insertion = zombieCount;
                while (insertion > 0 && key < zombieKeys[insertion - 1])
                {
                    zombieKeys[insertion] = zombieKeys[insertion - 1];
                    zombies[insertion] = zombies[insertion - 1];
                    insertion -= 1;
                }
                zombieKeys[insertion] = key;
                zombies[insertion] = bodyId;
                zombieCount += 1;
            }
        }
        else
        {
            b2SurfaceMaterial material = b2Shape_GetSurfaceMaterial(event->visitorShapeId);
            material.customColor = b2_colorLime;
            b2Shape_SetSurfaceMaterial(event->visitorShapeId, material);
        }
    }
    for (int i = 0; i < events.endCount; ++i)
    {
        b2SensorEndTouchEvent* event = events.endEvents + i;
        state->hash = MixLong(state->hash, b2StoreShapeId(event->sensorShapeId));
        state->hash = MixLong(state->hash, b2StoreShapeId(event->visitorShapeId));
        if (b2Shape_IsValid(event->visitorShapeId))
        {
            b2SurfaceMaterial material = b2Shape_GetSurfaceMaterial(event->visitorShapeId);
            material.customColor = 0;
            b2Shape_SetSurfaceMaterial(event->visitorShapeId, material);
        }
    }
    for (int i = 0; i < zombieCount; ++i)
    {
        b2DestroyBody(zombies[i]);
    }
    if ((stepCount & 0x1F) == 0)
    {
        CreateSensorRow(worldId, 210.0f);
    }
    state->lastStepCount = stepCount;
}

static uint64_t GetSensorHash(void* context)
{
    return ((SensorState*)context)->hash;
}

int main(void)
{
    SimulateAndPrint("benchmarkBarrel", CreateBarrel(), 1);

    b2WorldId worldId = NewWorld();
    CreateTumbler(worldId);
    SimulateAndPrint("benchmarkTumbler", worldId, 2);

    worldId = NewWorld();
    CreateLargePyramid(worldId);
    SimulateAndPrint("benchmarkLargePyramid", worldId, 2);

    worldId = NewWorld();
    CreateManyPyramids(worldId);
    SimulateAndPrint("benchmarkManyPyramids", worldId, 1);

    worldId = NewWorld();
    CreateJointGrid(worldId);
    SimulateAndPrint("benchmarkJointGrid", worldId, 2);

    worldId = NewWorld();
    CreateSmash(worldId);
    SimulateAndPrint("benchmarkSmash", worldId, 2);

    worldId = NewWorld();
    CreateSpinner(worldId);
    SimulateAndPrint("benchmarkSpinner", worldId, 2);

    ManyTumblersState manyTumblersState;
    worldId = CreateManyTumblers(&manyTumblersState);
    SimulateAndPrintWithCallbacks("benchmarkManyTumblers", worldId, 8, NULL, StepManyTumblers,
                                  &manyTumblersState, NULL);

    CreateDestroyState createDestroyState = {0};
    worldId = CreateCreateDestroy();
    SimulateAndPrintWithCallbacks("benchmarkCreateDestroy", worldId, 2, StepCreateDestroy, NULL,
                                  &createDestroyState, NULL);

    SleepState sleepState;
    worldId = CreateSleep(&sleepState);
    SimulateAndPrintWithCallbacks("benchmarkSleep", worldId, 2, StepSleep, NULL, &sleepState, NULL);

    SimulateAndPrint("benchmarkCompound", CreateCompound(), 1);
    SimulateAndPrint("benchmarkKinematic", CreateKinematic(), 1);

    CastState castState;
    worldId = CreateCast(&castState);
    SimulateAndPrintWithCallbacks("benchmarkCast", worldId, 1, NULL, StepCast, &castState, GetCastHash);

    worldId = NewWorld();
    CreateRain(worldId);
    SimulateAndPrintWithCallbacks("benchmarkRain", worldId, 33, StepRainCallback, NULL, NULL, NULL);

    ShapeDistanceState shapeDistanceState;
    worldId = CreateShapeDistance(&shapeDistanceState);
    SimulateAndPrintWithCallbacks("benchmarkShapeDistance", worldId, 1, StepShapeDistance, NULL,
                                  &shapeDistanceState, GetShapeDistanceHash);

    SensorState sensorState;
    worldId = CreateSensor(&sensorState);
    SimulateAndPrintWithCallbacks("benchmarkSensor", worldId, 120, NULL, StepSensor,
                                  &sensorState, GetSensorHash);
    return 0;
}
