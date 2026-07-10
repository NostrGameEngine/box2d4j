// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>
#include <stdlib.h>

enum
{
    DEFAULT_STEP_COUNT = 120,
    COUNT = 160,
    BODY_COUNT = COUNT + 5,
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

    float springHertz = 2.0f;
    float springDampingRatio = 0.7f;
    float frictionTorque = 200.0f;

    b2BodyId bodies[BODY_COUNT];
    b2JointId joints[JOINT_COUNT];
    int bodyIndex = 0;
    int jointIndex = 0;

    b2Polygon box = b2MakeBox(0.5f, 0.125f);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 20.0f;

    b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
    jointDef.enableMotor = true;
    jointDef.maxMotorTorque = frictionTorque;
    jointDef.enableSpring = true;
    jointDef.hertz = springHertz;
    jointDef.dampingRatio = springDampingRatio;

    float xbase = -80.0f;
    b2BodyId prevBodyId = groundId;
    for (int i = 0; i < COUNT; ++i)
    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = (b2Vec2){xbase + 0.5f + 1.0f * i, 20.0f};
        bodyDef.linearDamping = 0.1f;
        bodyDef.angularDamping = 0.1f;

        b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
        bodies[bodyIndex++] = bodyId;
        b2CreatePolygonShape(bodyId, &shapeDef, &box);

        b2Vec2 pivot = {xbase + 1.0f * i, 20.0f};
        jointDef.bodyIdA = prevBodyId;
        jointDef.bodyIdB = bodyId;
        jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
        jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
        joints[jointIndex++] = b2CreateRevoluteJoint(worldId, &jointDef);

        prevBodyId = bodyId;
    }

    b2Vec2 bridgeEndPivot = {xbase + 1.0f * COUNT, 20.0f};
    jointDef.bodyIdA = prevBodyId;
    jointDef.bodyIdB = groundId;
    jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, bridgeEndPivot);
    jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, bridgeEndPivot);
    joints[jointIndex++] = b2CreateRevoluteJoint(worldId, &jointDef);

    for (int i = 0; i < 2; ++i)
    {
        b2Vec2 vertices[3] = {{-0.5f, 0.0f}, {0.5f, 0.0f}, {0.0f, 1.5f}};
        b2Hull hull = b2ComputeHull(vertices, 3);
        b2Polygon triangle = b2MakePolygon(&hull, 0.0f);

        b2ShapeDef triangleShapeDef = b2DefaultShapeDef();
        triangleShapeDef.density = 20.0f;

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = (b2Vec2){-8.0f + 8.0f * i, 22.0f};
        b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
        bodies[bodyIndex++] = bodyId;
        b2CreatePolygonShape(bodyId, &triangleShapeDef, &triangle);
    }

    for (int i = 0; i < 3; ++i)
    {
        b2Circle circle = {{0.0f, 0.0f}, 0.5f};

        b2ShapeDef circleShapeDef = b2DefaultShapeDef();
        circleShapeDef.density = 20.0f;

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = (b2Vec2){-6.0f + 6.0f * i, 25.0f};
        b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
        bodies[bodyIndex++] = bodyId;
        b2CreateCircleShape(bodyId, &circleShapeDef, &circle);
    }

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("bridge %d %d %d %d %d %d %d",
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
