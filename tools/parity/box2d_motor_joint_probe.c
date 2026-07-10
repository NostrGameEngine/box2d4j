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
    printf("%s %d %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g\n",
           label, step, transform.p.x, transform.p.y, transform.q.c, transform.q.s,
           linearVelocity.x, linearVelocity.y, b2Body_GetAngularVelocity(bodyId),
           force.x, force.y, b2Joint_GetConstraintTorque(jointId));
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    b2BodyId bodyA = b2CreateBody(worldId, &bodyDef);
    bodyDef.position = (b2Vec2){2.0f, -1.0f};
    bodyDef.rotation = b2MakeRot(0.25f);
    b2BodyId bodyB = b2CreateBody(worldId, &bodyDef);

    b2MotorJointDef def = b2DefaultMotorJointDef();
    def.bodyIdA = bodyA;
    def.bodyIdB = bodyB;
    def.linearOffset = (b2Vec2){1.25f, -0.75f};
    def.angularOffset = 2.5f;
    def.maxForce = -4.0f;
    def.maxTorque = -5.0f;
    def.correctionFactor = 1.75f;
    def.collideConnected = true;
    b2JointId jointId = b2CreateMotorJoint(worldId, &def);

    print_vec("linear0", b2MotorJoint_GetLinearOffset(jointId));
    printf("initial %.9g %.9g %.9g %.9g %.9g %.9g %.9g\n",
           b2MotorJoint_GetAngularOffset(jointId), b2MotorJoint_GetMaxForce(jointId),
           b2MotorJoint_GetMaxTorque(jointId), b2MotorJoint_GetCorrectionFactor(jointId),
           b2Joint_GetConstraintForce(jointId).x, b2Joint_GetConstraintForce(jointId).y,
           b2Joint_GetConstraintTorque(jointId));

    b2MotorJoint_SetLinearOffset(jointId, (b2Vec2){-3.0f, 4.5f});
    b2MotorJoint_SetAngularOffset(jointId, -7.0f);
    print_vec("linear1", b2MotorJoint_GetLinearOffset(jointId));
    printf("angular1 %.9g\n", b2MotorJoint_GetAngularOffset(jointId));

    b2MotorJoint_SetMaxForce(jointId, -8.0f);
    b2MotorJoint_SetMaxTorque(jointId, -9.0f);
    printf("maxClamp0 %.9g %.9g\n", b2MotorJoint_GetMaxForce(jointId), b2MotorJoint_GetMaxTorque(jointId));

    b2MotorJoint_SetMaxForce(jointId, 8.25f);
    b2MotorJoint_SetMaxTorque(jointId, 9.5f);
    printf("maxClamp1 %.9g %.9g\n", b2MotorJoint_GetMaxForce(jointId), b2MotorJoint_GetMaxTorque(jointId));

    b2MotorJoint_SetCorrectionFactor(jointId, -0.25f);
    printf("correction0 %.9g\n", b2MotorJoint_GetCorrectionFactor(jointId));
    b2MotorJoint_SetCorrectionFactor(jointId, 0.42f);
    printf("correction1 %.9g\n", b2MotorJoint_GetCorrectionFactor(jointId));
    b2MotorJoint_SetCorrectionFactor(jointId, 2.0f);
    printf("correction2 %.9g\n", b2MotorJoint_GetCorrectionFactor(jointId));

    b2DestroyWorld(worldId);

    worldId = b2CreateWorld(&worldDef);

    b2BodyDef anchorDef = b2DefaultBodyDef();
    anchorDef.position = (b2Vec2){-0.5f, 1.25f};
    anchorDef.rotation = b2MakeRot(-0.2f);
    b2BodyId anchorId = b2CreateBody(worldId, &anchorDef);

    b2BodyDef dynamicDef = b2DefaultBodyDef();
    dynamicDef.type = b2_dynamicBody;
    dynamicDef.position = (b2Vec2){2.5f, -0.75f};
    dynamicDef.rotation = b2MakeRot(0.6f);
    dynamicDef.linearVelocity = (b2Vec2){-0.35f, 0.65f};
    dynamicDef.angularVelocity = -0.45f;
    b2BodyId dynamicId = b2CreateBody(worldId, &dynamicDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.75f;
    b2Polygon box = b2MakeBox(0.45f, 0.35f);
    b2CreatePolygonShape(dynamicId, &shapeDef, &box);

    b2MotorJointDef simDef = b2DefaultMotorJointDef();
    simDef.bodyIdA = anchorId;
    simDef.bodyIdB = dynamicId;
    simDef.linearOffset = (b2Vec2){1.0f, -0.25f};
    simDef.angularOffset = 0.4f;
    simDef.maxForce = 6.25f;
    simDef.maxTorque = 3.5f;
    simDef.correctionFactor = 0.65f;
    b2JointId simJointId = b2CreateMotorJoint(worldId, &simDef);

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
