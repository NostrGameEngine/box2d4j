// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>
#include <stdlib.h>

enum
{
    DEFAULT_STEP_COUNT = 120,
    SIDES = 7
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
    printf(" %.9g %.9g %.9g %.9g %.9g %.9g %.9g",
           b2Joint_GetLinearSeparation(jointId),
           b2Joint_GetAngularSeparation(jointId),
           b2WeldJoint_GetLinearHertz(jointId),
           b2WeldJoint_GetAngularHertz(jointId),
           force.x,
           force.y,
           b2Joint_GetConstraintTorque(jointId));
}

static void create_donut(b2WorldId worldId, b2Vec2 position, float scale, int groupIndex, bool enableSensorEvents,
                         b2BodyId bodyIds[SIDES], b2JointId jointIds[SIDES])
{
    float radius = 1.0f * scale;
    float deltaAngle = 2.0f * B2_PI / SIDES;
    float length = 2.0f * B2_PI * radius / SIDES;
    b2Capsule capsule = {{0.0f, -0.5f * length}, {0.0f, 0.5f * length}, 0.25f * scale};

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.enableSensorEvents = enableSensorEvents;
    shapeDef.filter.groupIndex = -groupIndex;
    shapeDef.material.friction = 0.3f;

    float angle = 0.0f;
    for (int i = 0; i < SIDES; ++i)
    {
        bodyDef.position = (b2Vec2){radius * cosf(angle) + position.x, radius * sinf(angle) + position.y};
        bodyDef.rotation = b2MakeRot(angle);
        bodyIds[i] = b2CreateBody(worldId, &bodyDef);
        b2CreateCapsuleShape(bodyIds[i], &shapeDef, &capsule);
        angle += deltaAngle;
    }

    b2WeldJointDef weldDef = b2DefaultWeldJointDef();
    weldDef.angularHertz = 5.0f;
    weldDef.angularDampingRatio = 0.0f;
    weldDef.localAnchorA = (b2Vec2){0.0f, 0.5f * length};
    weldDef.localAnchorB = (b2Vec2){0.0f, -0.5f * length};

    b2BodyId prevBodyId = bodyIds[SIDES - 1];
    for (int i = 0; i < SIDES; ++i)
    {
        weldDef.bodyIdA = prevBodyId;
        weldDef.bodyIdB = bodyIds[i];
        b2Rot rotA = b2Body_GetRotation(prevBodyId);
        b2Rot rotB = b2Body_GetRotation(bodyIds[i]);
        weldDef.referenceAngle = b2RelativeAngle(rotB, rotA);
        jointIds[i] = b2CreateWeldJoint(worldId, &weldDef);
        prevBodyId = weldDef.bodyIdB;
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
    b2Segment segment = {{-20.0f, 0.0f}, {20.0f, 0.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &segment);

    b2BodyId bodyIds[SIDES];
    b2JointId jointIds[SIDES];
    create_donut(worldId, (b2Vec2){0.0f, 10.0f}, 2.0f, 0, false, bodyIds, jointIds);

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("softBody %d %d %d %d %d %d %d",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           counters.jointCount,
           b2World_GetAwakeBodyCount(worldId),
           SIDES,
           SIDES);
    for (int i = 0; i < SIDES; ++i)
    {
        print_body(bodyIds[i]);
    }
    for (int i = 0; i < SIDES; ++i)
    {
        print_joint(jointIds[i]);
    }
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
