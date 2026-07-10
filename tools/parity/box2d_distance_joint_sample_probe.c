// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>
#include <stdlib.h>

enum
{
    DEFAULT_STEP_COUNT = 120,
    DEFAULT_COUNT = 1
};

static void print_body(b2BodyId bodyId)
{
    b2Transform transform = b2Body_GetTransform(bodyId);
    b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
    printf(" %.9g %.9g %.9g %.9g %.9g %.9g %.9g %d",
           transform.p.x,
           transform.p.y,
           transform.q.c,
           transform.q.s,
           v.x,
           v.y,
           b2Body_GetAngularVelocity(bodyId),
           b2Body_GetContactCapacity(bodyId));
}

static void print_joint(b2JointId jointId)
{
    b2Vec2 force = b2Joint_GetConstraintForce(jointId);
    printf(" %.9g %.9g %.9g %.9g %.9g",
           b2DistanceJoint_GetCurrentLength(jointId),
           b2DistanceJoint_GetMotorForce(jointId),
           force.x,
           force.y,
           b2Joint_GetConstraintTorque(jointId));
}

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : DEFAULT_STEP_COUNT;
    int count = argc > 2 ? atoi(argv[2]) : DEFAULT_COUNT;

    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyDef groundDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &groundDef);

    float hertz = 2.0f;
    float dampingRatio = 0.5f;
    float length = 1.0f;
    float minLength = length;
    float maxLength = length;
    bool enableSpring = false;
    bool enableLimit = false;

    b2Circle circle = {{0.0f, 0.0f}, 0.25f};
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 20.0f;

    float yOffset = 20.0f;
    b2DistanceJointDef jointDef = b2DefaultDistanceJointDef();
    jointDef.hertz = hertz;
    jointDef.dampingRatio = dampingRatio;
    jointDef.length = length;
    jointDef.minLength = minLength;
    jointDef.maxLength = maxLength;
    jointDef.enableSpring = enableSpring;
    jointDef.enableLimit = enableLimit;

    b2BodyId bodyIds[10] = {0};
    b2JointId jointIds[10] = {0};
    if (count < 0)
    {
        count = 0;
    }
    if (count > 10)
    {
        count = 10;
    }

    b2BodyId prevBodyId = groundId;
    for (int i = 0; i < count; ++i)
    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.angularDamping = 0.1f;
        bodyDef.position = (b2Vec2){length * (i + 1.0f), yOffset};
        bodyIds[i] = b2CreateBody(worldId, &bodyDef);
        b2CreateCircleShape(bodyIds[i], &shapeDef, &circle);

        b2Vec2 pivotA = {length * i, yOffset};
        b2Vec2 pivotB = {length * (i + 1.0f), yOffset};
        jointDef.bodyIdA = prevBodyId;
        jointDef.bodyIdB = bodyIds[i];
        jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivotA);
        jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivotB);
        jointIds[i] = b2CreateDistanceJoint(worldId, &jointDef);
        prevBodyId = bodyIds[i];
    }

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("distanceJoint %d %d %d %d %d %d %d",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           counters.jointCount,
           b2World_GetAwakeBodyCount(worldId),
           count,
           count);
    for (int i = 0; i < count; ++i)
    {
        print_body(bodyIds[i]);
    }
    for (int i = 0; i < count; ++i)
    {
        print_joint(jointIds[i]);
    }
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
