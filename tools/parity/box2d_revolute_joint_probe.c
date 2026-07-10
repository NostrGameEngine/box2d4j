// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>

static void print_vec(const char* label, b2Vec2 v)
{
    printf("%s %.9g %.9g\n", label, v.x, v.y);
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.rotation = b2MakeRot(0.25f);
    b2BodyId bodyA = b2CreateBody(worldId, &bodyDef);
    bodyDef.position = (b2Vec2){1.0f, -2.0f};
    bodyDef.rotation = b2MakeRot(1.5f);
    b2BodyId bodyB = b2CreateBody(worldId, &bodyDef);

    b2RevoluteJointDef def = b2DefaultRevoluteJointDef();
    def.bodyIdA = bodyA;
    def.bodyIdB = bodyB;
    def.localAnchorA = (b2Vec2){0.2f, -0.3f};
    def.localAnchorB = (b2Vec2){-0.4f, 0.5f};
    def.referenceAngle = 4.0f;
    def.targetAngle = -4.0f;
    def.enableSpring = true;
    def.hertz = 3.5f;
    def.dampingRatio = 0.45f;
    def.enableLimit = true;
    def.lowerAngle = -0.75f;
    def.upperAngle = 0.9f;
    def.enableMotor = true;
    def.maxMotorTorque = 11.0f;
    def.motorSpeed = -2.25f;
    def.collideConnected = true;
    b2JointId jointId = b2CreateRevoluteJoint(worldId, &def);

    print_vec("anchorA", b2Joint_GetLocalAnchorA(jointId));
    print_vec("anchorB", b2Joint_GetLocalAnchorB(jointId));
    printf("initial %.9g %.9g %d %.9g %.9g %.9g %d %.9g %.9g %.9g %d %.9g %.9g %.9g %.9g %.9g\n",
           b2Joint_GetReferenceAngle(jointId), b2RevoluteJoint_GetTargetAngle(jointId),
           b2RevoluteJoint_IsSpringEnabled(jointId) ? 1 : 0, b2RevoluteJoint_GetSpringHertz(jointId),
           b2RevoluteJoint_GetSpringDampingRatio(jointId), b2RevoluteJoint_GetAngle(jointId),
           b2RevoluteJoint_IsLimitEnabled(jointId) ? 1 : 0, b2RevoluteJoint_GetLowerLimit(jointId),
           b2RevoluteJoint_GetUpperLimit(jointId), b2RevoluteJoint_GetMotorSpeed(jointId),
           b2RevoluteJoint_IsMotorEnabled(jointId) ? 1 : 0, b2RevoluteJoint_GetMaxMotorTorque(jointId),
           b2RevoluteJoint_GetMotorTorque(jointId), b2Joint_GetConstraintForce(jointId).x,
           b2Joint_GetConstraintForce(jointId).y, b2Joint_GetConstraintTorque(jointId));

    b2RevoluteJoint_EnableSpring(jointId, false);
    b2RevoluteJoint_SetSpringHertz(jointId, 8.0f);
    b2RevoluteJoint_SetSpringDampingRatio(jointId, 0.8f);
    b2RevoluteJoint_SetTargetAngle(jointId, 1.25f);
    printf("spring %.9g %d %.9g %.9g\n", b2RevoluteJoint_GetTargetAngle(jointId),
           b2RevoluteJoint_IsSpringEnabled(jointId) ? 1 : 0, b2RevoluteJoint_GetSpringHertz(jointId),
           b2RevoluteJoint_GetSpringDampingRatio(jointId));

    b2RevoluteJoint_EnableLimit(jointId, false);
    b2RevoluteJoint_SetLimits(jointId, 0.6f, 1.1f);
    printf("limit %d %.9g %.9g\n", b2RevoluteJoint_IsLimitEnabled(jointId) ? 1 : 0,
           b2RevoluteJoint_GetLowerLimit(jointId), b2RevoluteJoint_GetUpperLimit(jointId));

    b2RevoluteJoint_EnableMotor(jointId, false);
    b2RevoluteJoint_SetMotorSpeed(jointId, 4.25f);
    b2RevoluteJoint_SetMaxMotorTorque(jointId, -13.0f);
    printf("motor %d %.9g %.9g %.9g\n", b2RevoluteJoint_IsMotorEnabled(jointId) ? 1 : 0,
           b2RevoluteJoint_GetMotorSpeed(jointId), b2RevoluteJoint_GetMaxMotorTorque(jointId),
           b2RevoluteJoint_GetMotorTorque(jointId));

    b2Body_SetTransform(bodyB, (b2Vec2){2.0f, 3.0f}, b2MakeRot(-2.75f));
    printf("angle1 %.9g\n", b2RevoluteJoint_GetAngle(jointId));

    b2DestroyWorld(worldId);
    return 0;
}
