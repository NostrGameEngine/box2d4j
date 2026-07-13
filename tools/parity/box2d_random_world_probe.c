// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdint.h>
#include <stdio.h>

enum
{
    SCENE_COUNT = 8,
    BODY_COUNT = 12,
    STEP_COUNT = 600,
};

static int customFilterCount;
static int preSolveCount;
static int frictionCount;
static int restitutionCount;
static uint32_t customFilterHash;
static uint32_t preSolveHash;
static uint32_t frictionHash;
static uint32_t restitutionHash;

static uint32_t update_hash(uint32_t hash, int valueA, int valueB)
{
    uint64_t contribution = (uint64_t)valueA * 1000003u + (uint64_t)valueB * 97409u +
                            (uint64_t)valueA * (uint64_t)valueB * 389u;
    return (uint32_t)(((uint64_t)hash + contribution) % 1000000007u);
}

static bool custom_filter(b2ShapeId shapeIdA, b2ShapeId shapeIdB, void* context)
{
    (void)context;
    customFilterCount += 1;
    int materialA = b2Shape_GetMaterial(shapeIdA);
    int materialB = b2Shape_GetMaterial(shapeIdB);
    customFilterHash = update_hash(customFilterHash, materialA, materialB);
    return (materialA + materialB) % 7 != 0;
}

static bool pre_solve(b2ShapeId shapeIdA, b2ShapeId shapeIdB, b2Manifold* manifold, void* context)
{
    (void)manifold;
    (void)context;
    preSolveCount += 1;
    preSolveHash = update_hash(preSolveHash, shapeIdA.index1, shapeIdB.index1);
    return (shapeIdA.index1 + shapeIdB.index1) % 11 != 0;
}

static float mix_friction(float frictionA, int materialA, float frictionB, int materialB)
{
    (void)frictionA;
    (void)frictionB;
    frictionCount += 1;
    frictionHash = update_hash(frictionHash, materialA, materialB);
    return 0.1f + 0.02f * (float)(materialA + 2 * materialB);
}

static float mix_restitution(float restitutionA, int materialA, float restitutionB, int materialB)
{
    (void)restitutionA;
    (void)restitutionB;
    restitutionCount += 1;
    restitutionHash = update_hash(restitutionHash, materialA, materialB);
    return 0.05f * (float)((materialA + 3 * materialB) % 8);
}

static uint32_t random_u32(uint32_t* state)
{
    *state = *state * 1664525u + 1013904223u;
    return *state;
}

static float grid_value(uint32_t* state, int extent, int denominator)
{
    uint32_t width = (uint32_t)(2 * extent + 1);
    return ((int)(random_u32(state) % width) - extent) / (float)denominator;
}

int main(void)
{
    uint32_t state = 0xc41f2a9du;

    for (int sceneIndex = 0; sceneIndex < SCENE_COUNT; ++sceneIndex)
    {
        customFilterCount = 0;
        preSolveCount = 0;
        frictionCount = 0;
        restitutionCount = 0;
        customFilterHash = 0;
        preSolveHash = 0;
        frictionHash = 0;
        restitutionHash = 0;
        b2WorldDef worldDef = b2DefaultWorldDef();
        worldDef.gravity = (b2Vec2){ grid_value(&state, 2, 2), -8.0f - (float)(sceneIndex % 5) };
        worldDef.enableSleep = (sceneIndex & 1) == 0;
        b2WorldId worldId = b2CreateWorld(&worldDef);
        b2World_SetCustomFilterCallback(worldId, custom_filter, NULL);
        b2World_SetPreSolveCallback(worldId, pre_solve, NULL);
        b2World_SetFrictionCallback(worldId, mix_friction);
        b2World_SetRestitutionCallback(worldId, mix_restitution);

        b2BodyDef groundDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(worldId, &groundDef);
        b2ShapeDef groundShapeDef = b2DefaultShapeDef();
        groundShapeDef.material.userMaterialId = 90;
        groundShapeDef.enablePreSolveEvents = true;
        b2Segment ground = { { -15.0f, 0.0f }, { 15.0f, 0.0f } };
        b2CreateSegmentShape(groundId, &groundShapeDef, &ground);

        b2BodyDef sensorBodyDef = b2DefaultBodyDef();
        b2BodyId sensorBodyId = b2CreateBody(worldId, &sensorBodyDef);
        b2ShapeDef sensorShapeDef = b2DefaultShapeDef();
        sensorShapeDef.isSensor = true;
        sensorShapeDef.enableSensorEvents = true;
        sensorShapeDef.material.userMaterialId = 91;
        b2Circle sensorCircle = { { 0.0f, 2.5f }, 1.75f };
        b2ShapeId sensorShapeId = b2CreateCircleShape(sensorBodyId, &sensorShapeDef, &sensorCircle);

        printf("scene %d %.9g %.9g %d %d %d\n", sceneIndex, worldDef.gravity.x, worldDef.gravity.y,
               worldDef.enableSleep ? 1 : 0, STEP_COUNT, BODY_COUNT);

        b2BodyId bodies[BODY_COUNT];
        b2ShapeId shapes[BODY_COUNT];
        for (int bodyIndex = 0; bodyIndex < BODY_COUNT; ++bodyIndex)
        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = bodyIndex == 0 ? b2_kinematicBody : b2_dynamicBody;
            bodyDef.position = (b2Vec2){ -8.25f + 1.5f * bodyIndex + grid_value(&state, 8, 32),
                                        2.0f + (float)(bodyIndex % 4) * 1.4f + grid_value(&state, 8, 16) };
            bodyDef.rotation = b2MakeRot(grid_value(&state, 16, 16));
            bodyDef.linearVelocity = bodyIndex == 0
                                         ? (b2Vec2){ sceneIndex % 2 == 0 ? 1.25f : -1.25f, 0.0f }
                                         : (b2Vec2){ grid_value(&state, 8, 8), grid_value(&state, 4, 8) };
            bodyDef.angularVelocity = grid_value(&state, 8, 8);
            bodyDef.enableSleep = (random_u32(&state) & 3u) != 0u;
            bodyDef.isBullet = bodyIndex == 1 || (random_u32(&state) & 15u) == 0u;

            int shapeKind = bodyIndex % 3;
            float p0;
            float p1;
            float p2;
            float p3;
            float p4;
            if (shapeKind == 0)
            {
                p0 = grid_value(&state, 3, 16);
                p1 = grid_value(&state, 3, 16);
                p2 = 0.2f + (float)(random_u32(&state) % 5u) * 0.075f;
                p3 = 0.0f;
                p4 = 0.0f;
            }
            else if (shapeKind == 1)
            {
                float halfLength = 0.25f + (float)(random_u32(&state) % 5u) * 0.1f;
                p0 = -halfLength;
                p1 = 0.0f;
                p2 = halfLength;
                p3 = 0.0f;
                p4 = 0.12f + (float)(random_u32(&state) % 4u) * 0.05f;
            }
            else
            {
                p0 = 0.25f + (float)(random_u32(&state) % 5u) * 0.1f;
                p1 = 0.2f + (float)(random_u32(&state) % 5u) * 0.075f;
                p2 = 0.0f;
                p3 = 0.0f;
                p4 = 0.0f;
            }

            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.density = 0.5f + (float)(random_u32(&state) % 5u) * 0.5f;
            shapeDef.material.friction = (float)(random_u32(&state) % 6u) * 0.15f;
            shapeDef.material.restitution = (float)(random_u32(&state) % 4u) * 0.2f;
            shapeDef.material.userMaterialId = bodyIndex + 1;
            shapeDef.enablePreSolveEvents = true;

            printf("body %d %.9g %.9g %.9g %.9g %.9g %.9g %.9g %d %d %d %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g\n",
                   bodyDef.type, bodyDef.position.x, bodyDef.position.y, bodyDef.rotation.c, bodyDef.rotation.s,
                   bodyDef.linearVelocity.x, bodyDef.linearVelocity.y, bodyDef.angularVelocity,
                   bodyDef.enableSleep ? 1 : 0, bodyDef.isBullet ? 1 : 0, shapeKind, p0, p1, p2, p3, p4,
                   shapeDef.density, shapeDef.material.friction, shapeDef.material.restitution);

            bodies[bodyIndex] = b2CreateBody(worldId, &bodyDef);
            if (shapeKind == 0)
            {
                b2Circle circle = { { p0, p1 }, p2 };
                shapes[bodyIndex] = b2CreateCircleShape(bodies[bodyIndex], &shapeDef, &circle);
            }
            else if (shapeKind == 1)
            {
                b2Capsule capsule = { { p0, p1 }, { p2, p3 }, p4 };
                shapes[bodyIndex] = b2CreateCapsuleShape(bodies[bodyIndex], &shapeDef, &capsule);
            }
            else
            {
                b2Polygon box = b2MakeBox(p0, p1);
                shapes[bodyIndex] = b2CreatePolygonShape(bodies[bodyIndex], &shapeDef, &box);
            }
        }

        b2JointId joints[3];
        b2DistanceJointDef distanceDef = b2DefaultDistanceJointDef();
        distanceDef.bodyIdA = bodies[1];
        distanceDef.bodyIdB = bodies[2];
        distanceDef.length = 1.75f;
        distanceDef.enableSpring = true;
        distanceDef.hertz = 2.0f;
        distanceDef.dampingRatio = 0.5f;
        joints[0] = b2CreateDistanceJoint(worldId, &distanceDef);

        b2RevoluteJointDef revoluteDef = b2DefaultRevoluteJointDef();
        revoluteDef.bodyIdA = bodies[3];
        revoluteDef.bodyIdB = bodies[4];
        revoluteDef.enableMotor = true;
        revoluteDef.maxMotorTorque = 12.0f;
        revoluteDef.motorSpeed = 0.5f;
        joints[1] = b2CreateRevoluteJoint(worldId, &revoluteDef);

        b2WeldJointDef weldDef = b2DefaultWeldJointDef();
        weldDef.bodyIdA = bodies[9];
        weldDef.bodyIdB = bodies[10];
        weldDef.linearHertz = 2.5f;
        weldDef.angularHertz = 1.5f;
        weldDef.linearDampingRatio = 0.6f;
        weldDef.angularDampingRatio = 0.4f;
        joints[2] = b2CreateWeldJoint(worldId, &weldDef);

        for (int step = 0; step < STEP_COUNT; ++step)
        {
            if (step == 60)
            {
                b2Body_SetTransform(bodies[2], (b2Vec2){ (float)sceneIndex - 3.5f, 6.0f },
                                    b2MakeRot(0.125f * (float)(sceneIndex - 3)));
            }
            else if (step == 90)
            {
                b2DistanceJoint_SetLength(joints[0], 2.25f);
            }
            else if (step == 120)
            {
                b2Body_Disable(bodies[5]);
            }
            else if (step == 150)
            {
                b2Body_Enable(bodies[5]);
            }
            else if (step == 180)
            {
                b2RevoluteJoint_SetMotorSpeed(joints[1], -0.75f);
            }
            else if (step == 210)
            {
                b2Body_SetType(bodies[6], b2_kinematicBody);
                b2Body_SetLinearVelocity(bodies[6], (b2Vec2){ -0.75f, 0.25f });
            }
            else if (step == 240)
            {
                b2Shape_EnableSensorEvents(sensorShapeId, false);
            }
            else if (step == 255)
            {
                b2Shape_EnableSensorEvents(sensorShapeId, true);
            }
            else if (step == 270)
            {
                b2Body_SetType(bodies[6], b2_dynamicBody);
            }
            else if (step == 285)
            {
                b2WeldJoint_SetLinearHertz(joints[2], 3.5f);
            }
            else if (step == 300)
            {
                b2World_EnableSleeping(worldId, false);
            }
            else if (step == 315)
            {
                b2World_EnableSleeping(worldId, (sceneIndex & 1) == 0);
            }
            else if (step == 330)
            {
                b2Shape_SetFriction(shapes[7], 0.95f);
            }
            else if (step == 345)
            {
                b2Filter filter = b2Shape_GetFilter(shapes[7]);
                filter.maskBits = 0;
                b2Shape_SetFilter(shapes[7], filter);
            }
            else if (step == 360)
            {
                b2Filter filter = b2Shape_GetFilter(sensorShapeId);
                filter.maskBits = 0;
                b2Shape_SetFilter(sensorShapeId, filter);
            }
            else if (step == 375)
            {
                b2Shape_SetFilter(shapes[7], b2DefaultFilter());
            }
            else if (step == 380)
            {
                b2Shape_SetFilter(sensorShapeId, b2DefaultFilter());
            }
            else if (step == 390)
            {
                b2Body_ApplyLinearImpulseToCenter(bodies[8], (b2Vec2){ 1.5f, 2.25f }, true);
            }
            else if (step == 405)
            {
                b2Capsule capsule = { { -0.35f, 0.0f }, { 0.35f, 0.0f }, 0.22f };
                b2Shape_SetCapsule(shapes[7], &capsule);
            }
            else if (step == 420)
            {
                b2DestroyJoint(joints[2]);
            }
            else if (step == 435)
            {
                b2Body_SetFixedRotation(bodies[8], true);
            }
            else if (step == 450)
            {
                b2World_SetGravity(worldId, (b2Vec2){ -0.25f, -6.5f });
            }
            else if (step == 465)
            {
                b2Body_SetFixedRotation(bodies[8], false);
            }
            else if (step == 495)
            {
                b2World_EnableContinuous(worldId, false);
            }
            else if (step == 510)
            {
                b2Body_SetLinearVelocity(bodies[9], (b2Vec2){ 0.5f, 1.75f });
            }
            else if (step == 525)
            {
                b2World_EnableContinuous(worldId, true);
            }
            else if (step == 540)
            {
                b2Body_SetGravityScale(bodies[11], 0.35f);
            }
            else if (step == 555)
            {
                b2DestroyJoint(joints[1]);
            }
            else if (step == 570)
            {
                b2Body_SetAngularVelocity(bodies[10], -1.25f);
            }
            b2World_Step(worldId, 1.0f / 60.0f, 4);
            b2Counters checkpointCounters = b2World_GetCounters(worldId);
            int checkpointAwakeCount = 0;
            unsigned int checkpointAwakeMask = 0;
            for (int bodyIndex = 0; bodyIndex < BODY_COUNT; ++bodyIndex)
            {
                if (b2Body_IsAwake(bodies[bodyIndex]))
                {
                    checkpointAwakeCount += 1;
                    checkpointAwakeMask |= 1u << bodyIndex;
                }
            }
            b2Transform trackedTransform = b2Body_GetTransform(bodies[6]);
            b2Vec2 trackedVelocity = b2Body_GetLinearVelocity(bodies[6]);
            b2Transform peerTransform = b2Body_GetTransform(bodies[7]);
            b2Vec2 peerVelocity = b2Body_GetLinearVelocity(bodies[7]);
            b2Transform reenabledTransform = b2Body_GetTransform(bodies[5]);
            b2Vec2 reenabledVelocity = b2Body_GetLinearVelocity(bodies[5]);
            printf("checkpoint %d %d %d %.9g %.9g %.9g %.9g %.9g %.9g %.9g %d %.9g %.9g %.9g %.9g %.9g %.9g %.9g %d %d %d %d %d %d %d %u %.9g %.9g %.9g %.9g %.9g %.9g %.9g %u %u %u %u\n", step,
                   checkpointCounters.contactCount, checkpointAwakeCount, trackedTransform.p.x, trackedTransform.p.y,
                   trackedTransform.q.c, trackedTransform.q.s, trackedVelocity.x, trackedVelocity.y,
                   b2Body_GetAngularVelocity(bodies[6]), b2Body_GetType(bodies[6]), peerTransform.p.x, peerTransform.p.y,
                   peerTransform.q.c, peerTransform.q.s, peerVelocity.x, peerVelocity.y,
                   b2Body_GetAngularVelocity(bodies[7]), b2Joint_IsValid(joints[0]) ? 1 : 0,
                   b2Joint_IsValid(joints[1]) ? 1 : 0, b2Joint_IsValid(joints[2]) ? 1 : 0, customFilterCount,
                   preSolveCount, frictionCount, restitutionCount, checkpointAwakeMask, reenabledTransform.p.x,
                   reenabledTransform.p.y, reenabledTransform.q.c, reenabledTransform.q.s, reenabledVelocity.x,
                   reenabledVelocity.y, b2Body_GetAngularVelocity(bodies[5]), customFilterHash, preSolveHash,
                   frictionHash, restitutionHash);
            printf("capacities");
            for (int bodyIndex = 0; bodyIndex < BODY_COUNT; ++bodyIndex)
            {
                printf(" %d", b2Body_GetContactCapacity(bodies[bodyIndex]));
            }
            printf("\n");
            printf("touching");
            for (int bodyIndex = 0; bodyIndex < BODY_COUNT; ++bodyIndex)
            {
                b2ContactData contactData[32];
                int touchingCount = b2Body_GetContactData(bodies[bodyIndex], contactData, 32);
                printf(" %d", touchingCount);
            }
            printf("\n");
            int sensorCapacity = b2Shape_GetSensorCapacity(sensorShapeId);
            b2ShapeId sensorOverlaps[BODY_COUNT + 2];
            int sensorCount = b2Shape_GetSensorOverlaps(sensorShapeId, sensorOverlaps, BODY_COUNT + 2);
            int sensorChecksum = 0;
            for (int overlapIndex = 0; overlapIndex < sensorCount; ++overlapIndex)
            {
                sensorChecksum += sensorOverlaps[overlapIndex].index1;
            }
            b2SensorEvents sensorEvents = b2World_GetSensorEvents(worldId);
            printf("sensor %d %d %d %d %d\n", sensorCapacity, sensorCount, sensorChecksum, sensorEvents.beginCount,
                   sensorEvents.endCount);
        }

        b2Counters counters = b2World_GetCounters(worldId);
        int awakeBodyCount = 0;
        for (int bodyIndex = 0; bodyIndex < BODY_COUNT; ++bodyIndex)
        {
            awakeBodyCount += b2Body_IsAwake(bodies[bodyIndex]) ? 1 : 0;
        }
        printf("result %d %d %d %d\n", counters.bodyCount, counters.shapeCount, counters.contactCount,
               awakeBodyCount);
        for (int bodyIndex = 0; bodyIndex < BODY_COUNT; ++bodyIndex)
        {
            b2Transform transform = b2Body_GetTransform(bodies[bodyIndex]);
            b2Vec2 velocity = b2Body_GetLinearVelocity(bodies[bodyIndex]);
            printf("state %.9g %.9g %.9g %.9g %.9g %.9g %.9g %d %d\n", transform.p.x, transform.p.y,
                   transform.q.c, transform.q.s, velocity.x, velocity.y, b2Body_GetAngularVelocity(bodies[bodyIndex]),
                   b2Body_IsAwake(bodies[bodyIndex]) ? 1 : 0, b2Body_GetContactCapacity(bodies[bodyIndex]));
        }

        b2DestroyWorld(worldId);
    }

    return 0;
}
