// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>
#include <stdlib.h>

enum
{
    DEFAULT_STEP_COUNT = 120,
    COUNT = 4,
    BODIES_PER_DOOHICKEY = 4,
    JOINTS_PER_DOOHICKEY = 3
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
    int type = (int)b2Joint_GetType(jointId);
    float metricA = 0.0f;
    float metricB = 0.0f;
    float metricC = 0.0f;
    if (type == b2_revoluteJoint)
    {
        metricA = b2RevoluteJoint_GetAngle(jointId);
        metricB = b2RevoluteJoint_GetMotorTorque(jointId);
        metricC = b2RevoluteJoint_GetMaxMotorTorque(jointId);
    }
    else if (type == b2_prismaticJoint)
    {
        metricA = b2PrismaticJoint_GetTranslation(jointId);
        metricB = b2PrismaticJoint_GetMotorForce(jointId);
        metricC = b2PrismaticJoint_GetSpringHertz(jointId);
    }
    b2Vec2 force = b2Joint_GetConstraintForce(jointId);
    printf(" %d %.9g %.9g %.9g %.9g %.9g %.9g %.9g",
           type,
           b2Joint_GetLinearSeparation(jointId),
           metricA,
           metricB,
           metricC,
           force.x,
           force.y,
           b2Joint_GetConstraintTorque(jointId));
}

static void spawn_doohickey(b2WorldId worldId, b2Vec2 position, float scale,
                            b2BodyId bodyIds[BODIES_PER_DOOHICKEY], b2JointId jointIds[JOINTS_PER_DOOHICKEY])
{
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.material.rollingResistance = 0.1f;

    b2Circle circle = {{0.0f, 0.0f}, 1.0f * scale};
    b2Capsule capsule = {{-3.5f * scale, 0.0f}, {3.5f * scale, 0.0f}, 0.15f * scale};

    bodyDef.position = b2MulAdd(position, scale, (b2Vec2){-5.0f, 3.0f});
    bodyIds[0] = b2CreateBody(worldId, &bodyDef);
    b2CreateCircleShape(bodyIds[0], &shapeDef, &circle);

    bodyDef.position = b2MulAdd(position, scale, (b2Vec2){5.0f, 3.0f});
    bodyIds[1] = b2CreateBody(worldId, &bodyDef);
    b2CreateCircleShape(bodyIds[1], &shapeDef, &circle);

    bodyDef.position = b2MulAdd(position, scale, (b2Vec2){-1.5f, 3.0f});
    bodyIds[2] = b2CreateBody(worldId, &bodyDef);
    b2CreateCapsuleShape(bodyIds[2], &shapeDef, &capsule);

    bodyDef.position = b2MulAdd(position, scale, (b2Vec2){1.5f, 3.0f});
    bodyIds[3] = b2CreateBody(worldId, &bodyDef);
    b2CreateCapsuleShape(bodyIds[3], &shapeDef, &capsule);

    b2RevoluteJointDef revoluteDef = b2DefaultRevoluteJointDef();
    revoluteDef.bodyIdA = bodyIds[0];
    revoluteDef.bodyIdB = bodyIds[2];
    revoluteDef.localAnchorA = (b2Vec2){0.0f, 0.0f};
    revoluteDef.localAnchorB = (b2Vec2){-3.5f * scale, 0.0f};
    revoluteDef.enableMotor = true;
    revoluteDef.maxMotorTorque = 2.0f * scale;
    jointIds[0] = b2CreateRevoluteJoint(worldId, &revoluteDef);

    revoluteDef.bodyIdA = bodyIds[1];
    revoluteDef.bodyIdB = bodyIds[3];
    revoluteDef.localAnchorA = (b2Vec2){0.0f, 0.0f};
    revoluteDef.localAnchorB = (b2Vec2){3.5f * scale, 0.0f};
    revoluteDef.enableMotor = true;
    revoluteDef.maxMotorTorque = 2.0f * scale;
    jointIds[1] = b2CreateRevoluteJoint(worldId, &revoluteDef);

    b2PrismaticJointDef prismaticDef = b2DefaultPrismaticJointDef();
    prismaticDef.bodyIdA = bodyIds[2];
    prismaticDef.bodyIdB = bodyIds[3];
    prismaticDef.localAxisA = (b2Vec2){1.0f, 0.0f};
    prismaticDef.localAnchorA = (b2Vec2){2.0f * scale, 0.0f};
    prismaticDef.localAnchorB = (b2Vec2){-2.0f * scale, 0.0f};
    prismaticDef.lowerTranslation = -2.0f * scale;
    prismaticDef.upperTranslation = 2.0f * scale;
    prismaticDef.enableLimit = true;
    prismaticDef.enableMotor = true;
    prismaticDef.maxMotorForce = 2.0f * scale;
    prismaticDef.enableSpring = true;
    prismaticDef.hertz = 1.0f;
    prismaticDef.dampingRatio = 0.5f;
    jointIds[2] = b2CreatePrismaticJoint(worldId, &prismaticDef);
}

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : DEFAULT_STEP_COUNT;
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Segment segment = {{-20.0f, 0.0f}, {20.0f, 0.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &segment);
    b2Polygon box = b2MakeOffsetBox(1.0f, 1.0f, (b2Vec2){0.0f, 1.0f}, b2Rot_identity);
    b2CreatePolygonShape(groundId, &shapeDef, &box);

    b2BodyId bodies[COUNT * BODIES_PER_DOOHICKEY];
    b2JointId joints[COUNT * JOINTS_PER_DOOHICKEY];
    float y = 4.0f;
    for (int i = 0; i < COUNT; ++i)
    {
        spawn_doohickey(worldId, (b2Vec2){0.0f, y}, 0.5f, bodies + i * BODIES_PER_DOOHICKEY,
                        joints + i * JOINTS_PER_DOOHICKEY);
        y += 2.0f;
    }

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("doohickey %d %d %d %d %d %d %d",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           counters.jointCount,
           b2World_GetAwakeBodyCount(worldId),
           COUNT * BODIES_PER_DOOHICKEY,
           COUNT * JOINTS_PER_DOOHICKEY);
    for (int i = 0; i < COUNT * BODIES_PER_DOOHICKEY; ++i)
    {
        print_body(bodies[i]);
    }
    for (int i = 0; i < COUNT * JOINTS_PER_DOOHICKEY; ++i)
    {
        print_joint(joints[i]);
    }
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
