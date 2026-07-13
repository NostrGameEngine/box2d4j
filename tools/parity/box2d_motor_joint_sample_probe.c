// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <math.h>
#include <stdio.h>
#include <stdlib.h>

enum
{
    DEFAULT_STEP_COUNT = 110
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

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : DEFAULT_STEP_COUNT;
    bool deterministicTarget = argc > 2 && atoi(argv[2]) != 0;
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyId groundId;
    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        groundId = b2CreateBody(worldId, &bodyDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2Segment segment = {{-20.0f, 0.0f}, {20.0f, 0.0f}};
        b2CreateSegmentShape(groundId, &shapeDef, &segment);
    }

    b2BodyId bodyId;
    b2JointId jointId;
    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = (b2Vec2){0.0f, 8.0f};
        bodyId = b2CreateBody(worldId, &bodyDef);

        b2Polygon box = b2MakeBox(2.0f, 0.5f);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        b2CreatePolygonShape(bodyId, &shapeDef, &box);

        b2MotorJointDef jointDef = b2DefaultMotorJointDef();
        jointDef.bodyIdA = groundId;
        jointDef.bodyIdB = bodyId;
        jointDef.maxForce = 500.0f;
        jointDef.maxTorque = 500.0f;
        jointDef.correctionFactor = 0.3f;
        jointId = b2CreateMotorJoint(worldId, &jointDef);
    }

    float time = 0.0f;
    b2Vec2 linearOffset = {0.0f, 0.0f};
    float angularOffset = 0.0f;
    for (int step = 0; step < stepCount; ++step)
    {
        time += 1.0f / 60.0f;
        if (deterministicTarget)
        {
            linearOffset.x = 6.0f * b2ComputeCosSin(2.0f * time).sine;
            linearOffset.y = 8.0f + 4.0f * b2ComputeCosSin(time).sine;
        }
        else
        {
            linearOffset.x = 6.0f * sinf(2.0f * time);
            linearOffset.y = 8.0f + 4.0f * sinf(time);
        }
        angularOffset = 2.0f * time;
        b2MotorJoint_SetLinearOffset(jointId, linearOffset);
        b2MotorJoint_SetAngularOffset(jointId, angularOffset);
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    b2Vec2 force = b2Joint_GetConstraintForce(jointId);
    printf("motorJoint %d %d %d %d %d %.9g %.9g %.9g %.9g %.9g %.9g %.9g",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           counters.jointCount,
           b2World_GetAwakeBodyCount(worldId),
           time,
           linearOffset.x,
           linearOffset.y,
           angularOffset,
           force.x,
           force.y,
           b2Joint_GetConstraintTorque(jointId));
    print_body(bodyId);
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
