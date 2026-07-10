// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>
#include <stdlib.h>

enum
{
    DEFAULT_STEP_COUNT = 120,
    COUNT = 30,
    BODY_COUNT = COUNT + 1,
    JOINT_COUNT = COUNT + 1
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
           b2RevoluteJoint_GetAngle(jointId),
           b2RevoluteJoint_GetMotorTorque(jointId),
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

    float frictionTorque = 100.0f;
    float hx = 0.5f;
    b2Capsule capsule = {{-hx, 0.0f}, {hx, 0.0f}, 0.125f};

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 20.0f;
    shapeDef.filter.categoryBits = 0x1;
    shapeDef.filter.maskBits = 0x2;

    b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
    b2BodyId bodies[BODY_COUNT];
    b2JointId joints[JOINT_COUNT];
    int bodyIndex = 0;
    int jointIndex = 0;

    b2BodyId prevBodyId = groundId;
    for (int i = 0; i < COUNT; ++i)
    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = (b2Vec2){(1.0f + 2.0f * i) * hx, COUNT * hx};
        b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
        bodies[bodyIndex++] = bodyId;
        b2CreateCapsuleShape(bodyId, &shapeDef, &capsule);

        b2Vec2 pivot = {(2.0f * i) * hx, COUNT * hx};
        jointDef.bodyIdA = prevBodyId;
        jointDef.bodyIdB = bodyId;
        jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
        jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
        jointDef.enableMotor = true;
        jointDef.maxMotorTorque = frictionTorque;
        jointDef.enableSpring = i > 0;
        jointDef.hertz = 4.0f;
        joints[jointIndex++] = b2CreateRevoluteJoint(worldId, &jointDef);

        prevBodyId = bodyId;
    }

    b2Circle circle = {{0.0f, 0.0f}, 4.0f};
    b2BodyDef ballDef = b2DefaultBodyDef();
    ballDef.type = b2_dynamicBody;
    ballDef.position = (b2Vec2){(1.0f + 2.0f * COUNT) * hx + circle.radius - hx, COUNT * hx};
    b2BodyId ballId = b2CreateBody(worldId, &ballDef);
    bodies[bodyIndex++] = ballId;

    shapeDef.filter.categoryBits = 0x2;
    shapeDef.filter.maskBits = 0x1;
    b2CreateCircleShape(ballId, &shapeDef, &circle);

    b2Vec2 pivot = {(2.0f * COUNT) * hx, COUNT * hx};
    jointDef.bodyIdA = prevBodyId;
    jointDef.bodyIdB = ballId;
    jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
    jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
    jointDef.enableMotor = true;
    jointDef.maxMotorTorque = frictionTorque;
    jointDef.enableSpring = true;
    jointDef.hertz = 4.0f;
    joints[jointIndex++] = b2CreateRevoluteJoint(worldId, &jointDef);

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("ballAndChain %d %d %d %d %d %d %d",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           counters.jointCount,
           b2World_GetAwakeBodyCount(worldId),
           BODY_COUNT,
           JOINT_COUNT);
    for (int i = 0; i < BODY_COUNT; ++i)
    {
        print_body(bodies[i]);
    }
    for (int i = 0; i < JOINT_COUNT; ++i)
    {
        print_joint(joints[i]);
    }
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
