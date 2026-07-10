// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>
#include <stdlib.h>

enum
{
    DEFAULT_STEP_COUNT = 120,
    COUNT = 8
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
    printf(" %.9g %.9g %.9g %.9g %.9g %.9g %.9g",
           b2WeldJoint_GetLinearHertz(jointId),
           b2WeldJoint_GetLinearDampingRatio(jointId),
           b2WeldJoint_GetAngularHertz(jointId),
           b2WeldJoint_GetAngularDampingRatio(jointId),
           force.x,
           force.y,
           b2Joint_GetConstraintTorque(jointId));
}

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : DEFAULT_STEP_COUNT;
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef groundDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &groundDef);

    float linearHertz = 15.0f;
    float linearDampingRatio = 0.5f;
    float angularHertz = 5.0f;
    float angularDampingRatio = 0.5f;
    bool collideConnected = false;

    float hx = 0.5f;
    b2Capsule capsule = {{-hx, 0.0f}, {hx, 0.0f}, 0.125f};
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 20.0f;

    b2WeldJointDef jointDef = b2DefaultWeldJointDef();
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.isAwake = false;

    b2BodyId bodies[COUNT];
    b2JointId joints[COUNT];
    b2BodyId prevBodyId = groundId;
    for (int i = 0; i < COUNT; ++i)
    {
        bodyDef.position = (b2Vec2){(1.0f + 2.0f * i) * hx, 0.0f};
        bodies[i] = b2CreateBody(worldId, &bodyDef);
        b2CreateCapsuleShape(bodies[i], &shapeDef, &capsule);

        b2Vec2 pivot = {(2.0f * i) * hx, 0.0f};
        jointDef.bodyIdA = prevBodyId;
        jointDef.bodyIdB = bodies[i];
        jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
        jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
        jointDef.linearHertz = linearHertz;
        jointDef.linearDampingRatio = linearDampingRatio;
        jointDef.angularHertz = angularHertz;
        jointDef.angularDampingRatio = angularDampingRatio;
        jointDef.collideConnected = collideConnected;
        joints[i] = b2CreateWeldJoint(worldId, &jointDef);

        prevBodyId = bodies[i];
    }

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("cantilever %d %d %d %d %d %d %d",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           counters.jointCount,
           b2World_GetAwakeBodyCount(worldId),
           COUNT,
           COUNT);
    for (int i = 0; i < COUNT; ++i)
    {
        print_body(bodies[i]);
    }
    for (int i = 0; i < COUNT; ++i)
    {
        print_joint(joints[i]);
    }
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
