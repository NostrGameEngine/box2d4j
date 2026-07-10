// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>

static void print_vec(const char* label, b2Vec2 v)
{
    printf("%s %.9g %.9g\n", label, v.x, v.y);
}

static void print_sim(const char* label, int step, b2BodyId bodyId, b2JointId jointId)
{
    b2Transform transform = b2Body_GetTransform(bodyId);
    b2Vec2 linearVelocity = b2Body_GetLinearVelocity(bodyId);
    b2Vec2 force = b2Joint_GetConstraintForce(jointId);
    printf("%s %d %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g\n",
           label, step, transform.p.x, transform.p.y, transform.q.c, transform.q.s,
           linearVelocity.x, linearVelocity.y, b2Body_GetAngularVelocity(bodyId),
           b2PrismaticJoint_GetTranslation(jointId), b2PrismaticJoint_GetSpeed(jointId),
           b2PrismaticJoint_GetMotorForce(jointId), force.x, force.y,
           b2Joint_GetConstraintTorque(jointId));
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){1.0f, 2.0f};
    bodyDef.rotation = b2MakeRot(0.25f);
    bodyDef.linearVelocity = (b2Vec2){0.4f, -0.2f};
    bodyDef.angularVelocity = 0.7f;
    b2BodyId bodyA = b2CreateBody(worldId, &bodyDef);
    bodyDef.position = (b2Vec2){3.0f, -1.0f};
    bodyDef.rotation = b2MakeRot(-0.5f);
    bodyDef.linearVelocity = (b2Vec2){-1.2f, 0.9f};
    bodyDef.angularVelocity = -0.35f;
    b2BodyId bodyB = b2CreateBody(worldId, &bodyDef);

    b2PrismaticJointDef def = b2DefaultPrismaticJointDef();
    def.bodyIdA = bodyA;
    def.bodyIdB = bodyB;
    def.localAnchorA = (b2Vec2){0.2f, -0.3f};
    def.localAnchorB = (b2Vec2){-0.4f, 0.5f};
    def.localAxisA = (b2Vec2){2.0f, 0.0f};
    def.referenceAngle = 0.75f;
    def.targetTranslation = 1.5f;
    def.enableSpring = true;
    def.hertz = 3.5f;
    def.dampingRatio = 0.45f;
    def.enableLimit = true;
    def.lowerTranslation = -0.75f;
    def.upperTranslation = 1.2f;
    def.enableMotor = true;
    def.maxMotorForce = 11.0f;
    def.motorSpeed = -2.25f;
    def.collideConnected = true;
    b2JointId jointId = b2CreatePrismaticJoint(worldId, &def);

    print_vec("axis0", b2Joint_GetLocalAxisA(jointId));
    print_vec("anchorA", b2Joint_GetLocalAnchorA(jointId));
    print_vec("anchorB", b2Joint_GetLocalAnchorB(jointId));
    printf("initial %.9g %.9g %d %.9g %.9g %.9g %.9g %d %.9g %.9g %.9g %d %.9g %.9g %.9g %.9g %.9g %.9g\n",
           b2Joint_GetReferenceAngle(jointId), b2PrismaticJoint_GetTargetTranslation(jointId),
           b2PrismaticJoint_IsSpringEnabled(jointId) ? 1 : 0, b2PrismaticJoint_GetSpringHertz(jointId),
           b2PrismaticJoint_GetSpringDampingRatio(jointId), b2PrismaticJoint_GetTranslation(jointId),
           b2PrismaticJoint_GetSpeed(jointId), b2PrismaticJoint_IsLimitEnabled(jointId) ? 1 : 0,
           b2PrismaticJoint_GetLowerLimit(jointId), b2PrismaticJoint_GetUpperLimit(jointId),
           b2PrismaticJoint_GetMotorSpeed(jointId), b2PrismaticJoint_IsMotorEnabled(jointId) ? 1 : 0,
           b2PrismaticJoint_GetMaxMotorForce(jointId), b2PrismaticJoint_GetMotorForce(jointId),
           b2Joint_GetConstraintForce(jointId).x, b2Joint_GetConstraintForce(jointId).y,
           b2Joint_GetConstraintTorque(jointId));

    b2PrismaticJoint_EnableSpring(jointId, false);
    b2PrismaticJoint_SetSpringHertz(jointId, 8.0f);
    b2PrismaticJoint_SetSpringDampingRatio(jointId, 0.8f);
    b2PrismaticJoint_SetTargetTranslation(jointId, -1.25f);
    printf("spring %.9g %d %.9g %.9g\n", b2PrismaticJoint_GetTargetTranslation(jointId),
           b2PrismaticJoint_IsSpringEnabled(jointId) ? 1 : 0, b2PrismaticJoint_GetSpringHertz(jointId),
           b2PrismaticJoint_GetSpringDampingRatio(jointId));

    b2PrismaticJoint_EnableLimit(jointId, false);
    b2PrismaticJoint_SetLimits(jointId, 0.6f, 1.1f);
    printf("limit %d %.9g %.9g\n", b2PrismaticJoint_IsLimitEnabled(jointId) ? 1 : 0,
           b2PrismaticJoint_GetLowerLimit(jointId), b2PrismaticJoint_GetUpperLimit(jointId));

    b2PrismaticJoint_EnableMotor(jointId, false);
    b2PrismaticJoint_SetMotorSpeed(jointId, 4.25f);
    b2PrismaticJoint_SetMaxMotorForce(jointId, -13.0f);
    printf("motor %d %.9g %.9g %.9g\n", b2PrismaticJoint_IsMotorEnabled(jointId) ? 1 : 0,
           b2PrismaticJoint_GetMotorSpeed(jointId), b2PrismaticJoint_GetMaxMotorForce(jointId),
           b2PrismaticJoint_GetMotorForce(jointId));

    b2Joint_SetLocalAxisA(jointId, (b2Vec2){0.0f, 1.0f});
    print_vec("axis1", b2Joint_GetLocalAxisA(jointId));
    printf("translation1 %.9g %.9g\n", b2PrismaticJoint_GetTranslation(jointId), b2PrismaticJoint_GetSpeed(jointId));

    b2DestroyWorld(worldId);

    worldId = b2CreateWorld(&worldDef);

    b2BodyDef anchorDef = b2DefaultBodyDef();
    anchorDef.position = (b2Vec2){-0.5f, 1.5f};
    anchorDef.rotation = b2MakeRot(0.35f);
    b2BodyId anchorId = b2CreateBody(worldId, &anchorDef);

    b2BodyDef dynamicDef = b2DefaultBodyDef();
    dynamicDef.type = b2_dynamicBody;
    dynamicDef.position = (b2Vec2){1.2f, 0.4f};
    dynamicDef.rotation = b2MakeRot(-0.2f);
    dynamicDef.linearVelocity = (b2Vec2){-0.1f, 0.3f};
    dynamicDef.angularVelocity = 0.4f;
    b2BodyId dynamicId = b2CreateBody(worldId, &dynamicDef);
    b2ShapeDef dynamicShapeDef = b2DefaultShapeDef();
    dynamicShapeDef.density = 1.7f;
    b2Polygon box = b2MakeBox(0.45f, 0.35f);
    b2CreatePolygonShape(dynamicId, &dynamicShapeDef, &box);

    b2PrismaticJointDef simDef = b2DefaultPrismaticJointDef();
    simDef.bodyIdA = anchorId;
    simDef.bodyIdB = dynamicId;
    simDef.localAnchorA = (b2Vec2){0.1f, -0.2f};
    simDef.localAnchorB = (b2Vec2){-0.15f, 0.05f};
    simDef.localAxisA = (b2Vec2){0.6f, 0.8f};
    simDef.referenceAngle = -0.45f;
    simDef.targetTranslation = 0.2f;
    simDef.enableSpring = true;
    simDef.hertz = 2.7f;
    simDef.dampingRatio = 0.45f;
    simDef.enableLimit = true;
    simDef.lowerTranslation = -0.3f;
    simDef.upperTranslation = 0.8f;
    simDef.enableMotor = true;
    simDef.motorSpeed = 0.6f;
    simDef.maxMotorForce = 5.0f;
    b2JointId simJointId = b2CreatePrismaticJoint(worldId, &simDef);

    for (int step = 1; step <= 24; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        if (step == 1 || step == 6 || step == 24)
        {
            print_sim("sim", step, dynamicId, simJointId);
        }
    }

    b2DestroyWorld(worldId);
    return 0;
}
