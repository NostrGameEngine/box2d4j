// SPDX-License-Identifier: MIT

#include "body.h"
#include "human.h"
#include "world.h"

#include "box2d/box2d.h"

#include <inttypes.h>
#include <math.h>
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
        if (!valid)
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

static void CreateDonut(b2WorldId worldId, b2Vec2 position, float scale)
{
    enum
    {
        sideCount = 7
    };
    b2BodyId bodies[sideCount];
    float radius = scale;
    float deltaAngle = 2.0f * B2_PI / sideCount;
    float length = 2.0f * B2_PI * radius / sideCount;
    b2Capsule capsule = {{0.0f, -0.5f * length}, {0.0f, 0.5f * length}, 0.25f * scale};
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.material.friction = 0.3f;
    float angle = 0.0f;
    for (int i = 0; i < sideCount; ++i)
    {
        bodyDef.position = (b2Vec2){radius * cosf(angle) + position.x, radius * sinf(angle) + position.y};
        bodyDef.rotation = b2MakeRot(angle);
        bodies[i] = b2CreateBody(worldId, &bodyDef);
        b2CreateCapsuleShape(bodies[i], &shapeDef, &capsule);
        angle += deltaAngle;
    }
    b2WeldJointDef weldDef = b2DefaultWeldJointDef();
    weldDef.angularHertz = 5.0f;
    weldDef.angularDampingRatio = 0.0f;
    weldDef.localAnchorA = (b2Vec2){0.0f, 0.5f * length};
    weldDef.localAnchorB = (b2Vec2){0.0f, -0.5f * length};
    b2BodyId previous = bodies[sideCount - 1];
    for (int i = 0; i < sideCount; ++i)
    {
        weldDef.bodyIdA = previous;
        weldDef.bodyIdB = bodies[i];
        weldDef.referenceAngle = b2RelativeAngle(b2Body_GetRotation(bodies[i]), b2Body_GetRotation(previous));
        b2CreateWeldJoint(worldId, &weldDef);
        previous = bodies[i];
    }
}

static void CreateCar(b2WorldId worldId, b2Vec2 position, float scale, float hertz, float dampingRatio, float torque)
{
    b2Vec2 vertices[6] = {
        {-1.5f, -0.5f}, {1.5f, -0.5f}, {1.5f, 0.0f},
        {0.0f, 0.9f}, {-1.15f, 0.9f}, {-1.5f, 0.2f},
    };
    for (int i = 0; i < 6; ++i)
    {
        vertices[i].x *= 0.85f * scale;
        vertices[i].y *= 0.85f * scale;
    }
    b2Hull hull = b2ComputeHull(vertices, 6);
    b2Polygon chassis = b2MakePolygon(&hull, 0.15f * scale);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.0f / scale;
    shapeDef.material.friction = 0.2f;
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = b2Add((b2Vec2){0.0f, scale}, position);
    b2BodyId chassisId = b2CreateBody(worldId, &bodyDef);
    b2CreatePolygonShape(chassisId, &shapeDef, &chassis);

    shapeDef.density = 2.0f / scale;
    shapeDef.material.friction = 1.5f;
    shapeDef.material.rollingResistance = 0.1f;
    b2Circle circle = {{0.0f, 0.0f}, 0.4f * scale};
    bodyDef.position = b2Add((b2Vec2){-scale, 0.35f * scale}, position);
    bodyDef.allowFastRotation = true;
    b2BodyId rearWheelId = b2CreateBody(worldId, &bodyDef);
    b2CreateCircleShape(rearWheelId, &shapeDef, &circle);
    bodyDef.position = b2Add((b2Vec2){scale, 0.4f * scale}, position);
    b2BodyId frontWheelId = b2CreateBody(worldId, &bodyDef);
    b2CreateCircleShape(frontWheelId, &shapeDef, &circle);

    b2Vec2 axis = {0.0f, 1.0f};
    b2Vec2 pivot = b2Body_GetPosition(rearWheelId);
    b2WheelJointDef jointDef = b2DefaultWheelJointDef();
    jointDef.bodyIdA = chassisId;
    jointDef.bodyIdB = rearWheelId;
    jointDef.localAxisA = b2Body_GetLocalVector(chassisId, axis);
    jointDef.localAnchorA = b2Body_GetLocalPoint(chassisId, pivot);
    jointDef.localAnchorB = b2Body_GetLocalPoint(rearWheelId, pivot);
    jointDef.maxMotorTorque = torque;
    jointDef.enableMotor = true;
    jointDef.hertz = hertz;
    jointDef.dampingRatio = dampingRatio;
    jointDef.lowerTranslation = -0.25f * scale;
    jointDef.upperTranslation = 0.25f * scale;
    jointDef.enableLimit = true;
    b2CreateWheelJoint(worldId, &jointDef);

    pivot = b2Body_GetPosition(frontWheelId);
    jointDef.bodyIdB = frontWheelId;
    jointDef.localAxisA = b2Body_GetLocalVector(chassisId, axis);
    jointDef.localAnchorA = b2Body_GetLocalPoint(chassisId, pivot);
    jointDef.localAnchorB = b2Body_GetLocalPoint(frontWheelId, pivot);
    b2CreateWheelJoint(worldId, &jointDef);
}

static b2WorldId CreateLargeWorld(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    float period = 40.0f;
    float omega = 2.0 * B2_PI / period;
    int cycleCount = 10;
    int gridCount = (int)(cycleCount * period);
    float xStart = -0.5f * cycleCount * period;
    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.invokeContactCreation = false;
    float xBody = xStart;
    float xShape = xStart;
    b2BodyId groundId = b2_nullBodyId;
    for (int i = 0; i < gridCount; ++i)
    {
        if (i % 10 == 0)
        {
            bodyDef.position.x = xBody;
            groundId = b2CreateBody(worldId, &bodyDef);
            xShape = 0.0f;
        }
        float y = 0.0f;
        int yCount = (int)roundf(4.0f * cosf(omega * xBody)) + 12;
        for (int j = 0; j < yCount; ++j)
        {
            b2Polygon square = b2MakeOffsetBox(0.4f, 0.4f, (b2Vec2){xShape, y}, b2Rot_identity);
            square.radius = 0.1f;
            b2CreatePolygonShape(groundId, &shapeDef, &square);
            y += 1.0f;
        }
        xBody += 1.0f;
        xShape += 1.0f;
    }

    int humanIndex = 0;
    for (int cycle = 0; cycle < cycleCount; ++cycle)
    {
        float baseX = (0.5f + cycle) * period + xStart;
        int remainder = cycle % 3;
        if (remainder == 0)
        {
            bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = (b2Vec2){baseX - 3.0f, 10.0f};
            shapeDef = b2DefaultShapeDef();
            b2Polygon box = b2MakeBox(0.3f, 0.2f);
            for (int i = 0; i < 10; ++i)
            {
                bodyDef.position.y = 10.0f;
                for (int j = 0; j < 5; ++j)
                {
                    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
                    b2CreatePolygonShape(bodyId, &shapeDef, &box);
                    bodyDef.position.y += 0.5f;
                }
                bodyDef.position.x += 0.6f;
            }
        }
        else if (remainder == 1)
        {
            b2Vec2 position = {baseX - 2.0f, 10.0f};
            for (int i = 0; i < 5; ++i)
            {
                Human human = {0};
                CreateHuman(&human, worldId, position, 1.5f, 0.05f, 0.0f, 0.0f, humanIndex + 1, NULL, false);
                humanIndex += 1;
                position.x += 1.0f;
            }
        }
        else
        {
            b2Vec2 position = {baseX - 4.0f, 12.0f};
            for (int i = 0; i < 5; ++i)
            {
                CreateDonut(worldId, position, 0.75f);
                position.x += 2.0f;
            }
        }
    }
    CreateCar(worldId, (b2Vec2){xStart + 20.0f, 40.0f}, 10.0f, 2.0f, 0.7f, 2000.0f);
    return worldId;
}

int main(void)
{
    b2WorldId worldId = CreateLargeWorld();
    Checkpoint initial = Capture(0, worldId);
    int cycleIndex = 0;
    for (int step = 0; step < 4; ++step)
    {
        if ((step & 1) == 1)
        {
            b2ExplosionDef def = b2DefaultExplosionDef();
            def.position = (b2Vec2){(0.5f + cycleIndex) * 40.0f - 200.0f, 7.0f};
            def.radius = 2.0f;
            def.falloff = 0.1f;
            def.impulsePerLength = 1.0f;
            b2World_Explode(worldId, &def);
            cycleIndex = (cycleIndex + 1) % 10;
        }
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }
    Checkpoint result = Capture(4, worldId);
    printf("largeWorld");
    PrintCheckpoint(&initial);
    PrintCheckpoint(&result);
    printf(" 0\n");
    b2DestroyWorld(worldId);
    return 0;
}
