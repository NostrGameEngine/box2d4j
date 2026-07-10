// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>

static void print_vec(const char* label, b2Vec2 v)
{
    printf("%s %.9g %.9g\n", label, v.x, v.y);
}

static void print_sim(const char* label, int step, b2BodyId bodyA, b2BodyId bodyB, b2JointId jointId)
{
    b2Transform transformA = b2Body_GetTransform(bodyA);
    b2Transform transformB = b2Body_GetTransform(bodyB);
    b2Vec2 linearVelocityB = b2Body_GetLinearVelocity(bodyB);
    b2Vec2 force = b2Joint_GetConstraintForce(jointId);
    printf("%s %d %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g\n",
           label, step, transformA.p.x, transformA.p.y, transformA.q.c, transformA.q.s,
           transformB.p.x, transformB.p.y, transformB.q.c, transformB.q.s,
           linearVelocityB.x, linearVelocityB.y, b2Body_GetAngularVelocity(bodyB),
           force.x, force.y, b2Joint_GetConstraintTorque(jointId));
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    b2BodyId bodyA = b2CreateBody(worldId, &bodyDef);
    bodyDef.position = (b2Vec2){1.0f, 2.0f};
    bodyDef.rotation = b2MakeRot(0.25f);
    b2BodyId bodyB = b2CreateBody(worldId, &bodyDef);

    b2WeldJointDef def = b2DefaultWeldJointDef();
    def.bodyIdA = bodyA;
    def.bodyIdB = bodyB;
    def.localAnchorA = (b2Vec2){0.2f, -0.1f};
    def.localAnchorB = (b2Vec2){-0.3f, 0.4f};
    def.referenceAngle = 0.75f;
    def.linearHertz = 2.5f;
    def.linearDampingRatio = 0.35f;
    def.angularHertz = 4.5f;
    def.angularDampingRatio = 0.65f;
    def.collideConnected = true;
    b2JointId jointId = b2CreateWeldJoint(worldId, &def);

    print_vec("anchorA", b2Joint_GetLocalAnchorA(jointId));
    print_vec("anchorB", b2Joint_GetLocalAnchorB(jointId));
    printf("initial %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g\n",
           b2Joint_GetReferenceAngle(jointId), b2WeldJoint_GetLinearHertz(jointId),
           b2WeldJoint_GetLinearDampingRatio(jointId), b2WeldJoint_GetAngularHertz(jointId),
           b2WeldJoint_GetAngularDampingRatio(jointId), b2Joint_GetConstraintForce(jointId).x,
           b2Joint_GetConstraintForce(jointId).y, b2Joint_GetConstraintTorque(jointId));

    b2Joint_SetReferenceAngle(jointId, -1.25f);
    b2WeldJoint_SetLinearHertz(jointId, 3.25f);
    b2WeldJoint_SetLinearDampingRatio(jointId, 0.55f);
    b2WeldJoint_SetAngularHertz(jointId, 7.5f);
    b2WeldJoint_SetAngularDampingRatio(jointId, 0.85f);
    printf("updated %.9g %.9g %.9g %.9g %.9g\n", b2Joint_GetReferenceAngle(jointId),
           b2WeldJoint_GetLinearHertz(jointId), b2WeldJoint_GetLinearDampingRatio(jointId),
           b2WeldJoint_GetAngularHertz(jointId), b2WeldJoint_GetAngularDampingRatio(jointId));

    b2DestroyWorld(worldId);

    worldId = b2CreateWorld(&worldDef);

    b2BodyDef simBodyDef = b2DefaultBodyDef();
    simBodyDef.type = b2_dynamicBody;
    simBodyDef.position = (b2Vec2){-0.75f, 1.2f};
    simBodyDef.rotation = b2MakeRot(-0.25f);
    simBodyDef.linearVelocity = (b2Vec2){0.25f, -0.1f};
    simBodyDef.angularVelocity = 0.15f;
    b2BodyId simBodyA = b2CreateBody(worldId, &simBodyDef);
    simBodyDef.position = (b2Vec2){1.1f, -0.4f};
    simBodyDef.rotation = b2MakeRot(0.45f);
    simBodyDef.linearVelocity = (b2Vec2){-0.35f, 0.3f};
    simBodyDef.angularVelocity = -0.25f;
    b2BodyId simBodyB = b2CreateBody(worldId, &simBodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.5f;
    b2Polygon boxA = b2MakeBox(0.4f, 0.3f);
    b2Polygon boxB = b2MakeBox(0.35f, 0.45f);
    b2CreatePolygonShape(simBodyA, &shapeDef, &boxA);
    b2CreatePolygonShape(simBodyB, &shapeDef, &boxB);

    b2WeldJointDef simDef = b2DefaultWeldJointDef();
    simDef.bodyIdA = simBodyA;
    simDef.bodyIdB = simBodyB;
    simDef.localAnchorA = (b2Vec2){0.15f, -0.05f};
    simDef.localAnchorB = (b2Vec2){-0.2f, 0.1f};
    simDef.referenceAngle = 0.35f;
    simDef.linearHertz = 3.0f;
    simDef.linearDampingRatio = 0.4f;
    simDef.angularHertz = 5.0f;
    simDef.angularDampingRatio = 0.7f;
    b2JointId simJointId = b2CreateWeldJoint(worldId, &simDef);

    for (int step = 1; step <= 24; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        if (step == 1 || step == 6 || step == 24)
        {
            print_sim("sim", step, simBodyA, simBodyB, simJointId);
        }
    }

    b2DestroyWorld(worldId);
    return 0;
}
