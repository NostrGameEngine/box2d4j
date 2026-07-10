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
    printf("%s %d %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g\n",
           label, step, transform.p.x, transform.p.y, transform.q.c, transform.q.s,
           linearVelocity.x, linearVelocity.y, b2Body_GetAngularVelocity(bodyId),
           b2Joint_GetLinearSeparation(jointId), b2WheelJoint_GetMotorTorque(jointId),
           force.x, force.y, b2Joint_GetConstraintTorque(jointId));
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){1.0f, 2.0f};
    bodyDef.rotation = b2MakeRot(0.25f);
    b2BodyId bodyA = b2CreateBody(worldId, &bodyDef);
    bodyDef.position = (b2Vec2){3.0f, -1.0f};
    bodyDef.rotation = b2MakeRot(-0.5f);
    b2BodyId bodyB = b2CreateBody(worldId, &bodyDef);

    b2WheelJointDef def = b2DefaultWheelJointDef();
    def.bodyIdA = bodyA;
    def.bodyIdB = bodyB;
    def.localAnchorA = (b2Vec2){0.2f, -0.3f};
    def.localAnchorB = (b2Vec2){-0.4f, 0.5f};
    def.localAxisA = (b2Vec2){0.0f, 2.0f};
    def.enableSpring = false;
    def.hertz = 3.5f;
    def.dampingRatio = 0.45f;
    def.enableLimit = true;
    def.lowerTranslation = -0.75f;
    def.upperTranslation = 1.2f;
    def.enableMotor = true;
    def.maxMotorTorque = 11.0f;
    def.motorSpeed = -2.25f;
    def.collideConnected = true;
    b2JointId jointId = b2CreateWheelJoint(worldId, &def);

    print_vec("axis0", b2Joint_GetLocalAxisA(jointId));
    print_vec("anchorA", b2Joint_GetLocalAnchorA(jointId));
    print_vec("anchorB", b2Joint_GetLocalAnchorB(jointId));
    printf("initial %d %.9g %.9g %d %.9g %.9g %.9g %d %.9g %.9g %.9g %.9g %.9g\n",
           b2WheelJoint_IsSpringEnabled(jointId) ? 1 : 0, b2WheelJoint_GetSpringHertz(jointId),
           b2WheelJoint_GetSpringDampingRatio(jointId), b2WheelJoint_IsLimitEnabled(jointId) ? 1 : 0,
           b2WheelJoint_GetLowerLimit(jointId), b2WheelJoint_GetUpperLimit(jointId),
           b2WheelJoint_GetMotorSpeed(jointId), b2WheelJoint_IsMotorEnabled(jointId) ? 1 : 0,
           b2WheelJoint_GetMaxMotorTorque(jointId), b2WheelJoint_GetMotorTorque(jointId),
           b2Joint_GetConstraintForce(jointId).x, b2Joint_GetConstraintForce(jointId).y,
           b2Joint_GetConstraintTorque(jointId));

    b2WheelJoint_EnableSpring(jointId, true);
    b2WheelJoint_SetSpringHertz(jointId, 8.0f);
    b2WheelJoint_SetSpringDampingRatio(jointId, 0.8f);
    printf("spring %d %.9g %.9g\n", b2WheelJoint_IsSpringEnabled(jointId) ? 1 : 0,
           b2WheelJoint_GetSpringHertz(jointId), b2WheelJoint_GetSpringDampingRatio(jointId));

    b2WheelJoint_EnableLimit(jointId, false);
    b2WheelJoint_SetLimits(jointId, 0.6f, 1.1f);
    printf("limit %d %.9g %.9g\n", b2WheelJoint_IsLimitEnabled(jointId) ? 1 : 0,
           b2WheelJoint_GetLowerLimit(jointId), b2WheelJoint_GetUpperLimit(jointId));

    b2WheelJoint_EnableMotor(jointId, false);
    b2WheelJoint_SetMotorSpeed(jointId, 4.25f);
    b2WheelJoint_SetMaxMotorTorque(jointId, -13.0f);
    printf("motor %d %.9g %.9g %.9g\n", b2WheelJoint_IsMotorEnabled(jointId) ? 1 : 0,
           b2WheelJoint_GetMotorSpeed(jointId), b2WheelJoint_GetMaxMotorTorque(jointId),
           b2WheelJoint_GetMotorTorque(jointId));

    b2Joint_SetLocalAxisA(jointId, (b2Vec2){1.0f, 0.0f});
    print_vec("axis1", b2Joint_GetLocalAxisA(jointId));

    b2DestroyWorld(worldId);

    worldId = b2CreateWorld(&worldDef);

    b2BodyDef anchorDef = b2DefaultBodyDef();
    anchorDef.position = (b2Vec2){-0.35f, 1.4f};
    anchorDef.rotation = b2MakeRot(0.15f);
    b2BodyId anchorId = b2CreateBody(worldId, &anchorDef);

    b2BodyDef dynamicDef = b2DefaultBodyDef();
    dynamicDef.type = b2_dynamicBody;
    dynamicDef.position = (b2Vec2){1.1f, 0.55f};
    dynamicDef.rotation = b2MakeRot(-0.3f);
    dynamicDef.linearVelocity = (b2Vec2){0.2f, -0.15f};
    dynamicDef.angularVelocity = 0.55f;
    b2BodyId dynamicId = b2CreateBody(worldId, &dynamicDef);
    b2ShapeDef dynamicShapeDef = b2DefaultShapeDef();
    dynamicShapeDef.density = 1.4f;
    b2Polygon box = b2MakeBox(0.42f, 0.28f);
    b2CreatePolygonShape(dynamicId, &dynamicShapeDef, &box);

    b2WheelJointDef simDef = b2DefaultWheelJointDef();
    simDef.bodyIdA = anchorId;
    simDef.bodyIdB = dynamicId;
    simDef.localAnchorA = (b2Vec2){0.05f, -0.1f};
    simDef.localAnchorB = (b2Vec2){-0.12f, 0.08f};
    simDef.localAxisA = (b2Vec2){0.7f, 0.3f};
    simDef.enableSpring = true;
    simDef.hertz = 3.2f;
    simDef.dampingRatio = 0.55f;
    simDef.enableLimit = true;
    simDef.lowerTranslation = -0.25f;
    simDef.upperTranslation = 0.65f;
    simDef.enableMotor = true;
    simDef.motorSpeed = -1.4f;
    simDef.maxMotorTorque = 2.75f;
    b2JointId simJointId = b2CreateWheelJoint(worldId, &simDef);

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
