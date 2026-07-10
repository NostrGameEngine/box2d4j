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
    groundDef.position = (b2Vec2){0.0f, -1.0f};
    b2BodyId groundId = b2CreateBody(worldId, &groundDef);

    b2Polygon groundBox = b2MakeBox(40.0f, 1.0f);
    b2ShapeDef groundShapeDef = b2DefaultShapeDef();
    b2CreatePolygonShape(groundId, &groundShapeDef, &groundBox);

    bool enableSpring = false;
    bool enableLimit = true;
    bool enableMotor = false;
    float hertz = 2.0f;
    float dampingRatio = 0.5f;
    float targetDegrees = 45.0f;
    float motorSpeed = 1.0f;
    float motorTorque = 1000.0f;

    b2BodyDef capsuleBodyDef = b2DefaultBodyDef();
    capsuleBodyDef.type = b2_dynamicBody;
    capsuleBodyDef.position = (b2Vec2){-10.0f, 20.0f};
    b2BodyId capsuleId = b2CreateBody(worldId, &capsuleBodyDef);

    b2ShapeDef capsuleShapeDef = b2DefaultShapeDef();
    capsuleShapeDef.density = 1.0f;
    b2Capsule capsule = {{0.0f, -1.0f}, {0.0f, 6.0f}, 0.5f};
    b2CreateCapsuleShape(capsuleId, &capsuleShapeDef, &capsule);

    b2Vec2 pivot1 = {-10.0f, 20.5f};
    b2RevoluteJointDef jointDef1 = b2DefaultRevoluteJointDef();
    jointDef1.bodyIdA = groundId;
    jointDef1.bodyIdB = capsuleId;
    jointDef1.localAnchorA = b2Body_GetLocalPoint(jointDef1.bodyIdA, pivot1);
    jointDef1.localAnchorB = b2Body_GetLocalPoint(jointDef1.bodyIdB, pivot1);
    jointDef1.targetAngle = B2_PI * targetDegrees / 180.0f;
    jointDef1.enableSpring = enableSpring;
    jointDef1.hertz = hertz;
    jointDef1.dampingRatio = dampingRatio;
    jointDef1.motorSpeed = motorSpeed;
    jointDef1.maxMotorTorque = motorTorque;
    jointDef1.enableMotor = enableMotor;
    jointDef1.referenceAngle = 0.5f * B2_PI;
    jointDef1.lowerAngle = -0.5f * B2_PI;
    jointDef1.upperAngle = 0.75f * B2_PI;
    jointDef1.enableLimit = enableLimit;
    b2JointId jointId1 = b2CreateRevoluteJoint(worldId, &jointDef1);

    b2Circle circle = {};
    circle.radius = 2.0f;

    b2BodyDef ballBodyDef = b2DefaultBodyDef();
    ballBodyDef.type = b2_dynamicBody;
    ballBodyDef.position = (b2Vec2){5.0f, 30.0f};
    b2BodyId ballId = b2CreateBody(worldId, &ballBodyDef);

    b2ShapeDef ballShapeDef = b2DefaultShapeDef();
    ballShapeDef.density = 1.0f;
    b2CreateCircleShape(ballId, &ballShapeDef, &circle);

    b2BodyDef leverBodyDef = b2DefaultBodyDef();
    leverBodyDef.position = (b2Vec2){20.0f, 10.0f};
    leverBodyDef.type = b2_dynamicBody;
    b2BodyId leverId = b2CreateBody(worldId, &leverBodyDef);

    b2Polygon leverBox = b2MakeOffsetBox(10.0f, 0.5f, (b2Vec2){-10.0f, 0.0f}, b2Rot_identity);
    b2ShapeDef leverShapeDef = b2DefaultShapeDef();
    leverShapeDef.density = 1.0f;
    b2CreatePolygonShape(leverId, &leverShapeDef, &leverBox);

    b2Vec2 pivot2 = {19.0f, 10.0f};
    b2RevoluteJointDef jointDef2 = b2DefaultRevoluteJointDef();
    jointDef2.bodyIdA = groundId;
    jointDef2.bodyIdB = leverId;
    jointDef2.localAnchorA = b2Body_GetLocalPoint(jointDef2.bodyIdA, pivot2);
    jointDef2.localAnchorB = b2Body_GetLocalPoint(jointDef2.bodyIdB, pivot2);
    jointDef2.lowerAngle = -0.25f * B2_PI;
    jointDef2.upperAngle = 0.1f * B2_PI;
    jointDef2.enableLimit = true;
    jointDef2.enableMotor = true;
    jointDef2.motorSpeed = 0.0f;
    jointDef2.maxMotorTorque = motorTorque;
    b2JointId jointId2 = b2CreateRevoluteJoint(worldId, &jointDef2);

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("revoluteJoint %d %d %d %d %d",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           counters.jointCount,
           b2World_GetAwakeBodyCount(worldId));
    print_body(capsuleId);
    print_body(ballId);
    print_body(leverId);
    print_joint(jointId1);
    print_joint(jointId2);
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
