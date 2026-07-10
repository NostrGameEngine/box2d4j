// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>
#include <stdlib.h>

enum
{
    DEFAULT_STEP_COUNT = 120,
    COUNT = 6
};

static const float BREAK_FORCE = 1000.0f;

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
    if (B2_IS_NULL(jointId))
    {
        printf(" 0 0 0 0 0 0 0 0");
        return;
    }

    int type = (int)b2Joint_GetType(jointId);
    float metricA = 0.0f;
    float metricB = 0.0f;

    switch (type)
    {
        case b2_distanceJoint:
            metricA = b2DistanceJoint_GetCurrentLength(jointId);
            metricB = b2DistanceJoint_GetMotorForce(jointId);
            break;
        case b2_motorJoint:
        {
            b2Vec2 linearOffset = b2MotorJoint_GetLinearOffset(jointId);
            metricA = linearOffset.x;
            metricB = linearOffset.y;
            break;
        }
        case b2_prismaticJoint:
            metricA = b2PrismaticJoint_GetTranslation(jointId);
            metricB = b2PrismaticJoint_GetSpeed(jointId);
            break;
        case b2_revoluteJoint:
            metricA = b2RevoluteJoint_GetAngle(jointId);
            metricB = b2RevoluteJoint_GetMotorTorque(jointId);
            break;
        case b2_weldJoint:
            metricA = b2WeldJoint_GetLinearHertz(jointId);
            metricB = b2WeldJoint_GetAngularHertz(jointId);
            break;
        case b2_wheelJoint:
            metricA = b2WheelJoint_GetMotorTorque(jointId);
            metricB = b2WheelJoint_GetMotorSpeed(jointId);
            break;
        default:
            break;
    }

    b2Vec2 force = b2Joint_GetConstraintForce(jointId);
    printf(" 1 %d %.9g %.9g %.9g %.9g %.9g %.9g",
           type,
           b2Joint_GetLinearSeparation(jointId),
           metricA,
           metricB,
           force.x,
           force.y,
           b2Joint_GetConstraintTorque(jointId));
}

static void break_overloaded_joints(b2JointId joints[COUNT])
{
    float breakForceSquared = BREAK_FORCE * BREAK_FORCE;
    for (int i = 0; i < COUNT; ++i)
    {
        if (B2_IS_NULL(joints[i]))
        {
            continue;
        }

        b2Vec2 force = b2Joint_GetConstraintForce(joints[i]);
        if (b2LengthSquared(force) > breakForceSquared)
        {
            b2DestroyJoint(joints[i]);
            joints[i] = b2_nullJointId;
        }
    }
}

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : DEFAULT_STEP_COUNT;
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef groundDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &groundDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Segment segment = {{-40.0f, 0.0f}, {40.0f, 0.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &segment);

    b2Vec2 position = {-12.5f, 10.0f};
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.enableSleep = false;

    b2Polygon box = b2MakeBox(1.0f, 1.0f);
    b2BodyId bodies[COUNT];
    b2JointId joints[COUNT];
    int index = 0;

    for (int i = 0; i < COUNT; ++i)
    {
        joints[i] = b2_nullJointId;
    }

    bodyDef.position = position;
    bodies[index] = b2CreateBody(worldId, &bodyDef);
    b2CreatePolygonShape(bodies[index], &shapeDef, &box);

    float length = 2.0f;
    b2Vec2 pivot1 = {position.x, position.y + 1.0f + length};
    b2Vec2 pivot2 = {position.x, position.y + 1.0f};
    b2DistanceJointDef distanceDef = b2DefaultDistanceJointDef();
    distanceDef.bodyIdA = groundId;
    distanceDef.bodyIdB = bodies[index];
    distanceDef.localAnchorA = b2Body_GetLocalPoint(distanceDef.bodyIdA, pivot1);
    distanceDef.localAnchorB = b2Body_GetLocalPoint(distanceDef.bodyIdB, pivot2);
    distanceDef.length = length;
    distanceDef.collideConnected = true;
    joints[index] = b2CreateDistanceJoint(worldId, &distanceDef);

    position.x += 5.0f;
    ++index;
    bodyDef.position = position;
    bodies[index] = b2CreateBody(worldId, &bodyDef);
    b2CreatePolygonShape(bodies[index], &shapeDef, &box);

    b2MotorJointDef motorDef = b2DefaultMotorJointDef();
    motorDef.bodyIdA = groundId;
    motorDef.bodyIdB = bodies[index];
    motorDef.linearOffset = position;
    motorDef.maxForce = 1000.0f;
    motorDef.maxTorque = 20.0f;
    motorDef.collideConnected = true;
    joints[index] = b2CreateMotorJoint(worldId, &motorDef);

    position.x += 5.0f;
    ++index;
    bodyDef.position = position;
    bodies[index] = b2CreateBody(worldId, &bodyDef);
    b2CreatePolygonShape(bodies[index], &shapeDef, &box);

    b2Vec2 pivot = {position.x - 1.0f, position.y};
    b2PrismaticJointDef prismaticDef = b2DefaultPrismaticJointDef();
    prismaticDef.bodyIdA = groundId;
    prismaticDef.bodyIdB = bodies[index];
    prismaticDef.localAnchorA = b2Body_GetLocalPoint(prismaticDef.bodyIdA, pivot);
    prismaticDef.localAnchorB = b2Body_GetLocalPoint(prismaticDef.bodyIdB, pivot);
    prismaticDef.localAxisA = b2Body_GetLocalVector(prismaticDef.bodyIdA, (b2Vec2){1.0f, 0.0f});
    prismaticDef.collideConnected = true;
    joints[index] = b2CreatePrismaticJoint(worldId, &prismaticDef);

    position.x += 5.0f;
    ++index;
    bodyDef.position = position;
    bodies[index] = b2CreateBody(worldId, &bodyDef);
    b2CreatePolygonShape(bodies[index], &shapeDef, &box);

    pivot = (b2Vec2){position.x - 1.0f, position.y};
    b2RevoluteJointDef revoluteDef = b2DefaultRevoluteJointDef();
    revoluteDef.bodyIdA = groundId;
    revoluteDef.bodyIdB = bodies[index];
    revoluteDef.localAnchorA = b2Body_GetLocalPoint(revoluteDef.bodyIdA, pivot);
    revoluteDef.localAnchorB = b2Body_GetLocalPoint(revoluteDef.bodyIdB, pivot);
    revoluteDef.collideConnected = true;
    joints[index] = b2CreateRevoluteJoint(worldId, &revoluteDef);

    position.x += 5.0f;
    ++index;
    bodyDef.position = position;
    bodies[index] = b2CreateBody(worldId, &bodyDef);
    b2CreatePolygonShape(bodies[index], &shapeDef, &box);

    pivot = (b2Vec2){position.x - 1.0f, position.y};
    b2WeldJointDef weldDef = b2DefaultWeldJointDef();
    weldDef.bodyIdA = groundId;
    weldDef.bodyIdB = bodies[index];
    weldDef.localAnchorA = b2Body_GetLocalPoint(weldDef.bodyIdA, pivot);
    weldDef.localAnchorB = b2Body_GetLocalPoint(weldDef.bodyIdB, pivot);
    weldDef.angularHertz = 2.0f;
    weldDef.angularDampingRatio = 0.5f;
    weldDef.linearHertz = 2.0f;
    weldDef.linearDampingRatio = 0.5f;
    weldDef.collideConnected = true;
    joints[index] = b2CreateWeldJoint(worldId, &weldDef);

    position.x += 5.0f;
    ++index;
    bodyDef.position = position;
    bodies[index] = b2CreateBody(worldId, &bodyDef);
    b2CreatePolygonShape(bodies[index], &shapeDef, &box);

    pivot = (b2Vec2){position.x - 1.0f, position.y};
    b2WheelJointDef wheelDef = b2DefaultWheelJointDef();
    wheelDef.bodyIdA = groundId;
    wheelDef.bodyIdB = bodies[index];
    wheelDef.localAnchorA = b2Body_GetLocalPoint(wheelDef.bodyIdA, pivot);
    wheelDef.localAnchorB = b2Body_GetLocalPoint(wheelDef.bodyIdB, pivot);
    wheelDef.localAxisA = b2Body_GetLocalVector(wheelDef.bodyIdA, (b2Vec2){1.0f, 0.0f});
    wheelDef.hertz = 1.0f;
    wheelDef.dampingRatio = 0.7f;
    wheelDef.lowerTranslation = -1.0f;
    wheelDef.upperTranslation = 1.0f;
    wheelDef.enableLimit = true;
    wheelDef.enableMotor = true;
    wheelDef.maxMotorTorque = 10.0f;
    wheelDef.motorSpeed = 1.0f;
    wheelDef.collideConnected = true;
    joints[index] = b2CreateWheelJoint(worldId, &wheelDef);

    for (int step = 0; step < stepCount; ++step)
    {
        break_overloaded_joints(joints);
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    int destroyedCount = 0;
    for (int i = 0; i < COUNT; ++i)
    {
        if (B2_IS_NULL(joints[i]))
        {
            ++destroyedCount;
        }
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("breakableJoint %d %d %d %d %d %d %d %d",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           counters.jointCount,
           b2World_GetAwakeBodyCount(worldId),
           destroyedCount,
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
