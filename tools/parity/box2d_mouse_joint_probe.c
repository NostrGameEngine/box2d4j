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
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){2.0f, -1.0f};
    bodyDef.rotation = b2MakeRot(0.5f);
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    b2MouseJointDef def = b2DefaultMouseJointDef();
    def.bodyIdA = groundId;
    def.bodyIdB = bodyId;
    def.target = (b2Vec2){3.0f, 1.0f};
    def.hertz = 6.25f;
    def.dampingRatio = 0.45f;
    def.maxForce = 123.0f;
    def.collideConnected = true;
    b2JointId jointId = b2CreateMouseJoint(worldId, &def);

    print_vec("target0", b2MouseJoint_GetTarget(jointId));
    print_vec("anchorA", b2Joint_GetLocalAnchorA(jointId));
    print_vec("anchorB", b2Joint_GetLocalAnchorB(jointId));
    printf("initial %.9g %.9g %.9g %.9g %.9g %.9g\n",
           b2MouseJoint_GetSpringHertz(jointId), b2MouseJoint_GetSpringDampingRatio(jointId),
           b2MouseJoint_GetMaxForce(jointId), b2Joint_GetConstraintForce(jointId).x,
           b2Joint_GetConstraintForce(jointId).y, b2Joint_GetConstraintTorque(jointId));

    b2MouseJoint_SetTarget(jointId, (b2Vec2){-4.0f, 5.5f});
    b2MouseJoint_SetSpringHertz(jointId, 9.0f);
    b2MouseJoint_SetSpringDampingRatio(jointId, 0.8f);
    b2MouseJoint_SetMaxForce(jointId, 321.0f);
    print_vec("target1", b2MouseJoint_GetTarget(jointId));
    printf("updated %.9g %.9g %.9g\n", b2MouseJoint_GetSpringHertz(jointId),
           b2MouseJoint_GetSpringDampingRatio(jointId), b2MouseJoint_GetMaxForce(jointId));

    b2DestroyWorld(worldId);

    worldId = b2CreateWorld(&worldDef);

    b2BodyDef groundDef = b2DefaultBodyDef();
    b2BodyId anchorId = b2CreateBody(worldId, &groundDef);

    b2BodyDef dynamicDef = b2DefaultBodyDef();
    dynamicDef.type = b2_dynamicBody;
    dynamicDef.position = (b2Vec2){2.2f, -0.8f};
    dynamicDef.rotation = b2MakeRot(-0.35f);
    dynamicDef.linearVelocity = (b2Vec2){0.3f, -0.2f};
    dynamicDef.angularVelocity = 0.4f;
    b2BodyId dynamicId = b2CreateBody(worldId, &dynamicDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.4f;
    b2Polygon box = b2MakeBox(0.5f, 0.3f);
    b2CreatePolygonShape(dynamicId, &shapeDef, &box);

    b2MouseJointDef simDef = b2DefaultMouseJointDef();
    simDef.bodyIdA = anchorId;
    simDef.bodyIdB = dynamicId;
    simDef.target = (b2Vec2){-1.25f, 1.5f};
    simDef.hertz = 4.0f;
    simDef.dampingRatio = 0.65f;
    simDef.maxForce = 9.0f;
    b2JointId simJointId = b2CreateMouseJoint(worldId, &simDef);

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
