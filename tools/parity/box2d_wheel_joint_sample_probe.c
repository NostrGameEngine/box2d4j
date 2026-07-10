// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>
#include <stdlib.h>

enum
{
    DEFAULT_STEP_COUNT = 120
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
           b2Joint_GetLinearSeparation(jointId),
           b2WheelJoint_GetMotorTorque(jointId),
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

    bool enableSpring = true;
    bool enableLimit = true;
    bool enableMotor = true;
    float motorSpeed = 2.0f;
    float motorTorque = 5.0f;
    float hertz = 1.0f;
    float dampingRatio = 0.7f;

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.position = (b2Vec2){0.0f, 10.25f};
    bodyDef.type = b2_dynamicBody;
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Capsule capsule = {{0.0f, -0.5f}, {0.0f, 0.5f}, 0.5f};
    b2CreateCapsuleShape(bodyId, &shapeDef, &capsule);

    b2Vec2 pivot = {0.0f, 10.0f};
    b2Vec2 axis = b2Normalize((b2Vec2){1.0f, 1.0f});
    b2WheelJointDef jointDef = b2DefaultWheelJointDef();
    jointDef.bodyIdA = groundId;
    jointDef.bodyIdB = bodyId;
    jointDef.localAxisA = b2Body_GetLocalVector(jointDef.bodyIdA, axis);
    jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
    jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
    jointDef.motorSpeed = motorSpeed;
    jointDef.maxMotorTorque = motorTorque;
    jointDef.enableMotor = enableMotor;
    jointDef.lowerTranslation = -3.0f;
    jointDef.upperTranslation = 3.0f;
    jointDef.enableLimit = enableLimit;
    jointDef.enableSpring = enableSpring;
    jointDef.hertz = hertz;
    jointDef.dampingRatio = dampingRatio;
    b2JointId jointId = b2CreateWheelJoint(worldId, &jointDef);

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("wheelJoint %d %d %d %d %d",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           counters.jointCount,
           b2World_GetAwakeBodyCount(worldId));
    print_body(bodyId);
    print_joint(jointId);
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
