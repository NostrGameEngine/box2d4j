// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>

static void PrintSimLine(const char* label, int step, b2BodyId bodyId, b2JointId jointId)
{
    b2Transform transform = b2Body_GetTransform(bodyId);
    b2Vec2 linearVelocity = b2Body_GetLinearVelocity(bodyId);
    b2Vec2 force = b2Joint_GetConstraintForce(jointId);
    printf("%s %d %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g\n",
           label, step, transform.p.x, transform.p.y, transform.q.c, transform.q.s,
           linearVelocity.x, linearVelocity.y, b2Body_GetAngularVelocity(bodyId),
           b2DistanceJoint_GetCurrentLength(jointId), b2DistanceJoint_GetMotorForce(jointId),
           force.x, force.y);
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.position = (b2Vec2){1.0f, 2.0f};
    bodyDef.rotation = b2MakeRot(0.25f);
    b2BodyId bodyA = b2CreateBody(worldId, &bodyDef);
    bodyDef.position = (b2Vec2){4.0f, -1.0f};
    bodyDef.rotation = b2MakeRot(-0.5f);
    b2BodyId bodyB = b2CreateBody(worldId, &bodyDef);

    b2DistanceJointDef def = b2DefaultDistanceJointDef();
    def.bodyIdA = bodyA;
    def.bodyIdB = bodyB;
    def.localAnchorA = (b2Vec2){0.3f, -0.2f};
    def.localAnchorB = (b2Vec2){-0.4f, 0.6f};
    def.length = 2.25f;
    def.enableSpring = true;
    def.hertz = 5.5f;
    def.dampingRatio = 0.33f;
    def.enableLimit = true;
    def.minLength = 0.75f;
    def.maxLength = 3.5f;
    def.enableMotor = true;
    def.maxMotorForce = 12.0f;
    def.motorSpeed = -1.25f;
    def.collideConnected = true;
    b2JointId jointId = b2CreateDistanceJoint(worldId, &def);

    printf("initial %.9g %d %.9g %.9g %d %.9g %.9g %.9g %d %.9g %.9g\n",
           b2DistanceJoint_GetLength(jointId), b2DistanceJoint_IsSpringEnabled(jointId) ? 1 : 0,
           b2DistanceJoint_GetSpringHertz(jointId), b2DistanceJoint_GetSpringDampingRatio(jointId),
           b2DistanceJoint_IsLimitEnabled(jointId) ? 1 : 0, b2DistanceJoint_GetMinLength(jointId),
           b2DistanceJoint_GetMaxLength(jointId), b2DistanceJoint_GetCurrentLength(jointId),
           b2DistanceJoint_IsMotorEnabled(jointId) ? 1 : 0, b2DistanceJoint_GetMotorSpeed(jointId),
           b2DistanceJoint_GetMaxMotorForce(jointId));

    b2DistanceJoint_SetLength(jointId, -10.0f);
    printf("lengthClamp %.9g %.9g\n", b2DistanceJoint_GetLength(jointId), b2DistanceJoint_GetMotorForce(jointId));
    b2DistanceJoint_SetLength(jointId, 200000.0f);
    printf("lengthHuge %.9g\n", b2DistanceJoint_GetLength(jointId));

    b2DistanceJoint_SetLengthRange(jointId, 5.0f, 1.0f);
    printf("rangeSwap %.9g %.9g\n", b2DistanceJoint_GetMinLength(jointId), b2DistanceJoint_GetMaxLength(jointId));
    b2DistanceJoint_SetLengthRange(jointId, -2.0f, 200000.0f);
    printf("rangeClamp %.9g %.9g\n", b2DistanceJoint_GetMinLength(jointId), b2DistanceJoint_GetMaxLength(jointId));

    b2DistanceJoint_EnableSpring(jointId, false);
    b2DistanceJoint_SetSpringHertz(jointId, 7.25f);
    b2DistanceJoint_SetSpringDampingRatio(jointId, 0.85f);
    b2DistanceJoint_EnableLimit(jointId, false);
    printf("springLimit %d %.9g %.9g %d\n", b2DistanceJoint_IsSpringEnabled(jointId) ? 1 : 0,
           b2DistanceJoint_GetSpringHertz(jointId), b2DistanceJoint_GetSpringDampingRatio(jointId),
           b2DistanceJoint_IsLimitEnabled(jointId) ? 1 : 0);

    b2DistanceJoint_EnableMotor(jointId, false);
    b2DistanceJoint_SetMotorSpeed(jointId, 3.75f);
    b2DistanceJoint_SetMaxMotorForce(jointId, 44.0f);
    printf("motor0 %d %.9g %.9g %.9g\n", b2DistanceJoint_IsMotorEnabled(jointId) ? 1 : 0,
           b2DistanceJoint_GetMotorSpeed(jointId), b2DistanceJoint_GetMaxMotorForce(jointId),
           b2DistanceJoint_GetMotorForce(jointId));
    b2DistanceJoint_EnableMotor(jointId, true);
    printf("motor1 %d\n", b2DistanceJoint_IsMotorEnabled(jointId) ? 1 : 0);

    b2Body_SetTransform(bodyB, (b2Vec2){2.0f, 4.0f}, b2MakeRot(0.75f));
    printf("current1 %.9g\n", b2DistanceJoint_GetCurrentLength(jointId));

    b2DestroyWorld(worldId);

    worldId = b2CreateWorld(&worldDef);

    b2BodyDef anchorDef = b2DefaultBodyDef();
    anchorDef.position = (b2Vec2){0.0f, 3.25f};
    b2BodyId anchorId = b2CreateBody(worldId, &anchorDef);

    b2BodyDef dynamicDef = b2DefaultBodyDef();
    dynamicDef.type = b2_dynamicBody;
    dynamicDef.position = (b2Vec2){2.0f, 1.0f};
    dynamicDef.rotation = b2MakeRot(0.2f);
    dynamicDef.linearVelocity = (b2Vec2){0.5f, -0.25f};
    dynamicDef.angularVelocity = 0.35f;
    b2BodyId dynamicId = b2CreateBody(worldId, &dynamicDef);
    b2ShapeDef dynamicShapeDef = b2DefaultShapeDef();
    dynamicShapeDef.density = 2.0f;
    b2Polygon box = b2MakeBox(0.4f, 0.6f);
    b2CreatePolygonShape(dynamicId, &dynamicShapeDef, &box);

    b2DistanceJointDef simDef = b2DefaultDistanceJointDef();
    simDef.bodyIdA = anchorId;
    simDef.bodyIdB = dynamicId;
    simDef.localAnchorA = (b2Vec2){0.0f, 0.0f};
    simDef.localAnchorB = (b2Vec2){0.2f, -0.1f};
    simDef.length = 2.0f;
    simDef.enableSpring = true;
    simDef.hertz = 2.5f;
    simDef.dampingRatio = 0.35f;
    simDef.enableLimit = true;
    simDef.minLength = 1.25f;
    simDef.maxLength = 3.0f;
    simDef.enableMotor = true;
    simDef.motorSpeed = -0.4f;
    simDef.maxMotorForce = 7.5f;
    b2JointId simJointId = b2CreateDistanceJoint(worldId, &simDef);

    for (int step = 1; step <= 30; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        if (step == 1 || step == 8 || step == 30)
        {
            PrintSimLine("sim", step, dynamicId, simJointId);
        }
    }

    b2DestroyWorld(worldId);
    return 0;
}
