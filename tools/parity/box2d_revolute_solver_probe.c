// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>

static void PrintBody(const char* label, b2BodyId bodyId)
{
    b2Transform transform = b2Body_GetTransform(bodyId);
    b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
    printf("%s %.9g %.9g %.9g %.9g %.9g %.9g %.9g\n",
           label,
           transform.p.x,
           transform.p.y,
           transform.q.c,
           transform.q.s,
           velocity.x,
           velocity.y,
           b2Body_GetAngularVelocity(bodyId));
}

static void RunIsolatedRevolute(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = (b2Vec2){0.0f, 0.0f};
    worldDef.enableSleep = false;
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.enableSleep = false;
    bodyDef.linearDamping = 0.0f;
    bodyDef.angularDamping = 0.0f;
    bodyDef.position = (b2Vec2){-0.6f, 0.0f};
    bodyDef.rotation = b2MakeRot(0.25f);
    b2BodyId bodyA = b2CreateBody(worldId, &bodyDef);
    b2Body_SetLinearVelocity(bodyA, (b2Vec2){0.7f, -0.2f});
    b2Body_SetAngularVelocity(bodyA, 0.4f);

    bodyDef.position = (b2Vec2){0.8f, 0.2f};
    bodyDef.rotation = b2MakeRot(-0.35f);
    b2BodyId bodyB = b2CreateBody(worldId, &bodyDef);
    b2Body_SetLinearVelocity(bodyB, (b2Vec2){-0.3f, 0.5f});
    b2Body_SetAngularVelocity(bodyB, -0.7f);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.3f;
    b2Polygon boxA = b2MakeBox(0.5f, 0.3f);
    b2Polygon boxB = b2MakeBox(0.4f, 0.6f);
    b2CreatePolygonShape(bodyA, &shapeDef, &boxA);
    b2CreatePolygonShape(bodyB, &shapeDef, &boxB);

    b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
    jointDef.bodyIdA = bodyA;
    jointDef.bodyIdB = bodyB;
    jointDef.localAnchorA = (b2Vec2){0.25f, 0.1f};
    jointDef.localAnchorB = (b2Vec2){-0.2f, -0.15f};
    jointDef.referenceAngle = -0.6f;
    jointDef.targetAngle = 0.2f;
    jointDef.enableSpring = true;
    jointDef.hertz = 2.0f;
    jointDef.dampingRatio = 0.3f;
    jointDef.enableLimit = true;
    jointDef.lowerAngle = -0.4f;
    jointDef.upperAngle = 0.25f;
    jointDef.enableMotor = true;
    jointDef.motorSpeed = 1.1f;
    jointDef.maxMotorTorque = 3.5f;
    b2JointId jointId = b2CreateRevoluteJoint(worldId, &jointDef);

    for (int i = 0; i < 6; ++i)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    PrintBody("bodyA", bodyA);
    PrintBody("bodyB", bodyB);
    b2Vec2 force = b2Joint_GetConstraintForce(jointId);
    printf("joint %.9g %.9g %.9g %.9g\n",
           force.x,
           force.y,
           b2Joint_GetConstraintTorque(jointId),
           b2RevoluteJoint_GetAngle(jointId));

    b2DestroyWorld(worldId);
}

static void RunGroundedRevolute(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = (b2Vec2){0.0f, -10.0f};
    worldDef.enableSleep = false;
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef groundDef = b2DefaultBodyDef();
    groundDef.position = (b2Vec2){0.0f, -0.5f};
    b2BodyId groundId = b2CreateBody(worldId, &groundDef);
    b2Polygon groundBox = b2MakeBox(4.0f, 0.5f);
    b2ShapeDef groundShapeDef = b2DefaultShapeDef();
    groundShapeDef.material.friction = 0.4f;
    b2CreatePolygonShape(groundId, &groundShapeDef, &groundBox);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.enableSleep = false;
    bodyDef.position = (b2Vec2){0.0f, 1.2f};
    bodyDef.rotation = b2MakeRot(0.35f);
    bodyDef.linearDamping = 0.0f;
    bodyDef.angularDamping = 0.0f;
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    b2Body_SetLinearVelocity(bodyId, (b2Vec2){0.2f, 0.0f});
    b2Body_SetAngularVelocity(bodyId, 0.1f);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.0f;
    shapeDef.material.friction = 0.35f;
    b2Polygon box = b2MakeBox(0.25f, 0.75f);
    b2CreatePolygonShape(bodyId, &shapeDef, &box);

    b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
    jointDef.bodyIdA = groundId;
    jointDef.bodyIdB = bodyId;
    jointDef.localAnchorA = (b2Vec2){0.0f, 1.7f};
    jointDef.localAnchorB = (b2Vec2){0.0f, 0.75f};
    jointDef.referenceAngle = 0.0f;
    jointDef.enableLimit = true;
    jointDef.lowerAngle = -0.55f;
    jointDef.upperAngle = 0.45f;
    jointDef.enableMotor = true;
    jointDef.motorSpeed = -0.6f;
    jointDef.maxMotorTorque = 2.0f;
    b2JointId jointId = b2CreateRevoluteJoint(worldId, &jointDef);

    for (int i = 0; i < 80; ++i)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    PrintBody("groundedBody", bodyId);
    b2Vec2 force = b2Joint_GetConstraintForce(jointId);
    printf("groundedJoint %.9g %.9g %.9g %.9g %d\n",
           force.x,
           force.y,
           b2Joint_GetConstraintTorque(jointId),
           b2RevoluteJoint_GetAngle(jointId),
           b2World_GetCounters(worldId).contactCount);

    b2DestroyWorld(worldId);
}

static void RunMiniFallingHingeFreeFall(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.enableSleep = false;
    b2WorldId worldId = b2CreateWorld(&worldDef);

    float h = 0.25f;
    float r = 0.1f * h;
    float offset = 0.4f * h;
    b2Polygon box = b2MakeRoundedBox(h - r, h - r, r);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.material.friction = 0.3f;

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.enableSleep = false;
    bodyDef.position = (b2Vec2){0.0f, h};
    bodyDef.rotation = b2MakeRot(-1.0f);
    b2BodyId bodyA = b2CreateBody(worldId, &bodyDef);
    b2CreatePolygonShape(bodyA, &shapeDef, &box);

    bodyDef.position = (b2Vec2){offset, h + 2.0f * h};
    bodyDef.rotation = b2MakeRot(-0.9f);
    b2BodyId bodyB = b2CreateBody(worldId, &bodyDef);
    b2CreatePolygonShape(bodyB, &shapeDef, &box);

    b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
    jointDef.bodyIdA = bodyA;
    jointDef.bodyIdB = bodyB;
    jointDef.enableLimit = true;
    jointDef.lowerAngle = -0.1f * B2_PI;
    jointDef.upperAngle = 0.2f * B2_PI;
    jointDef.enableSpring = true;
    jointDef.hertz = 0.5f;
    jointDef.dampingRatio = 0.5f;
    jointDef.localAnchorA = (b2Vec2){h, h};
    jointDef.localAnchorB = (b2Vec2){offset, -h};
    b2JointId jointId = b2CreateRevoluteJoint(worldId, &jointDef);

    for (int i = 0; i < 60; ++i)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    PrintBody("miniFreeBodyA", bodyA);
    PrintBody("miniFreeBodyB", bodyB);
    b2Vec2 force = b2Joint_GetConstraintForce(jointId);
    printf("miniFreeJoint %.9g %.9g %.9g %.9g %d\n",
           force.x,
           force.y,
           b2Joint_GetConstraintTorque(jointId),
           b2RevoluteJoint_GetAngle(jointId),
           b2World_GetCounters(worldId).contactCount);

    b2DestroyWorld(worldId);
}

static void RunHighAngleFallingHingePair(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.enableSleep = false;
    b2WorldId worldId = b2CreateWorld(&worldDef);

    float h = 0.25f;
    float r = 0.1f * h;
    float offset = 0.4f * h;
    b2Polygon box = b2MakeRoundedBox(h - r, h - r, r);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.material.friction = 0.3f;

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.enableSleep = false;
    bodyDef.position = (b2Vec2){offset * 42.0f, h + 2.0f * h * 42.0f};
    bodyDef.rotation = b2MakeRot(0.1f * 42.0f - 1.0f);
    b2BodyId bodyA = b2CreateBody(worldId, &bodyDef);
    b2CreatePolygonShape(bodyA, &shapeDef, &box);

    bodyDef.position = (b2Vec2){offset * 43.0f, h + 2.0f * h * 43.0f};
    bodyDef.rotation = b2MakeRot(0.1f * 43.0f - 1.0f);
    b2BodyId bodyB = b2CreateBody(worldId, &bodyDef);
    b2CreatePolygonShape(bodyB, &shapeDef, &box);

    b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
    jointDef.bodyIdA = bodyA;
    jointDef.bodyIdB = bodyB;
    jointDef.enableLimit = true;
    jointDef.lowerAngle = -0.1f * B2_PI;
    jointDef.upperAngle = 0.2f * B2_PI;
    jointDef.enableSpring = true;
    jointDef.hertz = 0.5f;
    jointDef.dampingRatio = 0.5f;
    jointDef.localAnchorA = (b2Vec2){h, h};
    jointDef.localAnchorB = (b2Vec2){offset, -h};
    b2JointId jointId = b2CreateRevoluteJoint(worldId, &jointDef);

    b2World_Step(worldId, 1.0f / 60.0f, 4);

    PrintBody("highAngleBodyA", bodyA);
    PrintBody("highAngleBodyB", bodyB);
    b2Vec2 force = b2Joint_GetConstraintForce(jointId);
    printf("highAngleJoint %.9g %.9g %.9g %.9g %d\n",
           force.x,
           force.y,
           b2Joint_GetConstraintTorque(jointId),
           b2RevoluteJoint_GetAngle(jointId),
           b2World_GetCounters(worldId).contactCount);

    b2DestroyWorld(worldId);
}

static void RunMiniFallingHingeGrounded(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.enableSleep = false;
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef groundDef = b2DefaultBodyDef();
    groundDef.position = (b2Vec2){0.0f, -1.0f};
    b2BodyId groundId = b2CreateBody(worldId, &groundDef);
    b2Polygon groundBox = b2MakeBox(20.0f, 1.0f);
    b2ShapeDef groundShapeDef = b2DefaultShapeDef();
    b2CreatePolygonShape(groundId, &groundShapeDef, &groundBox);

    float h = 0.25f;
    float r = 0.1f * h;
    float offset = 0.4f * h;
    b2Polygon box = b2MakeRoundedBox(h - r, h - r, r);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.material.friction = 0.3f;

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.enableSleep = false;
    bodyDef.position = (b2Vec2){0.0f, h};
    bodyDef.rotation = b2MakeRot(-1.0f);
    b2BodyId bodyA = b2CreateBody(worldId, &bodyDef);
    b2CreatePolygonShape(bodyA, &shapeDef, &box);

    bodyDef.position = (b2Vec2){offset, h + 2.0f * h};
    bodyDef.rotation = b2MakeRot(-0.9f);
    b2BodyId bodyB = b2CreateBody(worldId, &bodyDef);
    b2CreatePolygonShape(bodyB, &shapeDef, &box);

    b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
    jointDef.bodyIdA = bodyA;
    jointDef.bodyIdB = bodyB;
    jointDef.enableLimit = true;
    jointDef.lowerAngle = -0.1f * B2_PI;
    jointDef.upperAngle = 0.2f * B2_PI;
    jointDef.enableSpring = true;
    jointDef.hertz = 0.5f;
    jointDef.dampingRatio = 0.5f;
    jointDef.localAnchorA = (b2Vec2){h, h};
    jointDef.localAnchorB = (b2Vec2){offset, -h};
    b2JointId jointId = b2CreateRevoluteJoint(worldId, &jointDef);

    for (int i = 0; i < 60; ++i)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    PrintBody("miniGroundBodyA", bodyA);
    PrintBody("miniGroundBodyB", bodyB);
    b2Vec2 force = b2Joint_GetConstraintForce(jointId);
    printf("miniGroundJoint %.9g %.9g %.9g %.9g %d\n",
           force.x,
           force.y,
           b2Joint_GetConstraintTorque(jointId),
           b2RevoluteJoint_GetAngle(jointId),
           b2World_GetCounters(worldId).contactCount);

    b2DestroyWorld(worldId);
}

int main(void)
{
    RunIsolatedRevolute();
    RunGroundedRevolute();
    RunMiniFallingHingeFreeFall();
    RunHighAngleFallingHingePair();
    RunMiniFallingHingeGrounded();
    return 0;
}
