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
    b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
    printf(" %.9g %.9g %.9g %.9g %.9g %.9g %.9g %d",
           transform.p.x,
           transform.p.y,
           transform.q.c,
           transform.q.s,
           velocity.x,
           velocity.y,
           b2Body_GetAngularVelocity(bodyId),
           b2Body_GetContactCapacity(bodyId));
}

static void print_joint(b2JointId jointId)
{
    b2Vec2 force = b2Joint_GetConstraintForce(jointId);
    float constraintHertz = 0.0f;
    float constraintDampingRatio = 0.0f;
    b2Joint_GetConstraintTuning(jointId, &constraintHertz, &constraintDampingRatio);
    printf(" %.9g %.9g %d %.9g %.9g %d %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g",
           b2RevoluteJoint_GetAngle(jointId),
           b2RevoluteJoint_GetTargetAngle(jointId),
           b2RevoluteJoint_IsSpringEnabled(jointId) ? 1 : 0,
           b2RevoluteJoint_GetSpringHertz(jointId),
           b2RevoluteJoint_GetSpringDampingRatio(jointId),
           b2RevoluteJoint_IsLimitEnabled(jointId) ? 1 : 0,
           b2RevoluteJoint_GetLowerLimit(jointId),
           b2RevoluteJoint_GetUpperLimit(jointId),
           b2Joint_GetLinearSeparation(jointId),
           b2Joint_GetAngularSeparation(jointId),
           b2RevoluteJoint_GetMotorTorque(jointId),
           b2RevoluteJoint_GetMaxMotorTorque(jointId),
           force.x,
           force.y,
           b2Joint_GetConstraintTorque(jointId),
           constraintHertz,
           constraintDampingRatio);
}

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : DEFAULT_STEP_COUNT;
    bool applyImpulse = argc <= 2 || atoi(argv[2]) != 0;

    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef groundDef = b2DefaultBodyDef();
    groundDef.position = (b2Vec2){0.0f, 0.0f};
    b2BodyId groundId = b2CreateBody(worldId, &groundDef);

    bool enableLimit = true;
    float impulse = 50000.0f;
    float translationError = 0.0f;
    float jointHertz = 240.0f;
    float jointDampingRatio = 1.0f;

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){0.0f, 1.5f};
    bodyDef.gravityScale = 0.0f;
    b2BodyId doorId = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1000.0f;
    b2Polygon box = b2MakeBox(0.1f, 1.5f);
    b2CreatePolygonShape(doorId, &shapeDef, &box);

    b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
    jointDef.bodyIdA = groundId;
    jointDef.bodyIdB = doorId;
    jointDef.localAnchorA = (b2Vec2){0.0f, 0.0f};
    jointDef.localAnchorB = (b2Vec2){0.0f, -1.5f};
    jointDef.targetAngle = 0.0f;
    jointDef.enableSpring = true;
    jointDef.hertz = 1.0f;
    jointDef.dampingRatio = 0.5f;
    jointDef.motorSpeed = 0.0f;
    jointDef.maxMotorTorque = 0.0f;
    jointDef.enableMotor = false;
    jointDef.referenceAngle = 0.0f;
    jointDef.lowerAngle = -0.5f * B2_PI;
    jointDef.upperAngle = 0.5f * B2_PI;
    jointDef.enableLimit = enableLimit;
    b2JointId jointId = b2CreateRevoluteJoint(worldId, &jointDef);
    b2Joint_SetConstraintTuning(jointId, jointHertz, jointDampingRatio);

    if (applyImpulse)
    {
        b2Vec2 p = b2Body_GetWorldPoint(doorId, (b2Vec2){0.0f, 1.5f});
        b2Body_ApplyLinearImpulse(doorId, (b2Vec2){impulse, 0.0f}, p, true);
        translationError = 0.0f;
    }

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        translationError = b2MaxFloat(translationError, b2Joint_GetLinearSeparation(jointId));
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("door %d %d %d %d %d %.9g",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           counters.jointCount,
           b2World_GetAwakeBodyCount(worldId),
           translationError);
    print_body(doorId);
    print_joint(jointId);
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
