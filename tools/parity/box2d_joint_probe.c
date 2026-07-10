// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdint.h>
#include <stdio.h>

static void print_vec(const char* label, b2Vec2 v)
{
    printf("%s %.9g %.9g\n", label, v.x, v.y);
}

int main(void)
{
    b2DistanceJointDef distanceDef0 = b2DefaultDistanceJointDef();
    b2MotorJointDef motorDef0 = b2DefaultMotorJointDef();
    b2MouseJointDef mouseDef0 = b2DefaultMouseJointDef();
    b2PrismaticJointDef prismaticDef0 = b2DefaultPrismaticJointDef();
    b2RevoluteJointDef revoluteDef0 = b2DefaultRevoluteJointDef();
    b2WheelJointDef wheelDef0 = b2DefaultWheelJointDef();
    printf("defaults %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %d %.9g %.9g\n",
           distanceDef0.length, distanceDef0.maxLength, motorDef0.maxForce, motorDef0.maxTorque,
           motorDef0.correctionFactor, mouseDef0.hertz, mouseDef0.dampingRatio, mouseDef0.maxForce,
           wheelDef0.enableSpring ? 1 : 0, wheelDef0.hertz, wheelDef0.dampingRatio);
    print_vec("prismaticDefaultAxis", prismaticDef0.localAxisA);
    printf("revoluteDefault %.9g\n", revoluteDef0.drawSize);
    print_vec("wheelDefaultAxis", wheelDef0.localAxisA);

    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    b2BodyId bodyA = b2CreateBody(worldId, &bodyDef);
    bodyDef.position = (b2Vec2){0.5f, 0.0f};
    b2BodyId bodyB = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Polygon box = b2MakeBox(1.0f, 1.0f);
    b2CreatePolygonShape(bodyA, &shapeDef, &box);
    b2CreatePolygonShape(bodyB, &shapeDef, &box);
    b2World_Step(worldId, 1.0f / 60.0f, 4);
    printf("contacts0 %d %d\n", b2World_GetCounters(worldId).contactCount, b2Body_GetContactCapacity(bodyA));

    uintptr_t userData = 0x1234u;
    b2DistanceJointDef distanceDef = b2DefaultDistanceJointDef();
    distanceDef.bodyIdA = bodyA;
    distanceDef.bodyIdB = bodyB;
    distanceDef.localAnchorA = (b2Vec2){0.1f, 0.2f};
    distanceDef.localAnchorB = (b2Vec2){0.3f, 0.4f};
    distanceDef.length = 2.0f;
    distanceDef.collideConnected = false;
    distanceDef.userData = (void*)userData;
    b2JointId distanceId = b2CreateDistanceJoint(worldId, &distanceDef);
    b2JointId bodyAJoints[8] = {0};
    int bodyAJointCount = b2Body_GetJoints(bodyA, bodyAJoints, 8);
    printf("distance %d %d %d %d %d %d %d %d %d %d\n", distanceId.index1, distanceId.world0, distanceId.generation,
           b2Joint_IsValid(distanceId) ? 1 : 0, b2Joint_GetType(distanceId), b2Joint_GetBodyA(distanceId).index1,
           b2Joint_GetBodyB(distanceId).index1, b2Joint_GetWorld(distanceId).index1,
           b2Body_GetJointCount(bodyA), bodyAJointCount);
    printf("distanceBodyJoint %d %d\n", bodyAJoints[0].index1, bodyAJoints[0].generation);
    print_vec("anchorA0", b2Joint_GetLocalAnchorA(distanceId));
    print_vec("anchorB0", b2Joint_GetLocalAnchorB(distanceId));
    printf("distanceProps %d %d %.9g %.9g %.9g %d\n", b2Joint_GetCollideConnected(distanceId) ? 1 : 0,
           b2Joint_GetUserData(distanceId) == (void*)userData ? 1 : 0, b2Joint_GetReferenceAngle(distanceId),
           b2Joint_GetConstraintForce(distanceId).x, b2Joint_GetConstraintTorque(distanceId),
           b2World_GetCounters(worldId).contactCount);

    b2Joint_SetLocalAnchorA(distanceId, (b2Vec2){-0.5f, 0.75f});
    b2Joint_SetLocalAnchorB(distanceId, (b2Vec2){1.25f, -1.5f});
    b2Joint_SetUserData(distanceId, (void*)0x5678u);
    print_vec("anchorA1", b2Joint_GetLocalAnchorA(distanceId));
    print_vec("anchorB1", b2Joint_GetLocalAnchorB(distanceId));
    printf("userdata1 %d\n", b2Joint_GetUserData(distanceId) == (void*)0x5678u ? 1 : 0);

    b2Joint_SetCollideConnected(distanceId, true);
    b2World_Step(worldId, 1.0f / 60.0f, 4);
    printf("contacts1 %d %d\n", b2Joint_GetCollideConnected(distanceId) ? 1 : 0, b2World_GetCounters(worldId).contactCount);

    b2PrismaticJointDef prismaticDef = b2DefaultPrismaticJointDef();
    prismaticDef.bodyIdA = bodyA;
    prismaticDef.bodyIdB = bodyB;
    prismaticDef.localAnchorA = (b2Vec2){0.2f, -0.1f};
    prismaticDef.localAnchorB = (b2Vec2){-0.3f, 0.6f};
    prismaticDef.localAxisA = (b2Vec2){2.0f, 0.0f};
    prismaticDef.referenceAngle = 0.4f;
    prismaticDef.collideConnected = true;
    b2JointId prismaticId = b2CreatePrismaticJoint(worldId, &prismaticDef);
    print_vec("prismaticAxis0", b2Joint_GetLocalAxisA(prismaticId));
    printf("prismaticRef0 %.9g\n", b2Joint_GetReferenceAngle(prismaticId));
    b2Joint_SetLocalAxisA(prismaticId, (b2Vec2){0.0f, 1.0f});
    b2Joint_SetReferenceAngle(prismaticId, -0.7f);
    print_vec("prismaticAxis1", b2Joint_GetLocalAxisA(prismaticId));
    printf("prismaticRef1 %.9g\n", b2Joint_GetReferenceAngle(prismaticId));

    b2WheelJointDef wheelDef = b2DefaultWheelJointDef();
    wheelDef.bodyIdA = bodyA;
    wheelDef.bodyIdB = bodyB;
    wheelDef.localAxisA = (b2Vec2){0.0f, 2.0f};
    wheelDef.collideConnected = true;
    b2JointId wheelId = b2CreateWheelJoint(worldId, &wheelDef);
    print_vec("wheelAxis0", b2Joint_GetLocalAxisA(wheelId));

    b2RevoluteJointDef revoluteDef = b2DefaultRevoluteJointDef();
    revoluteDef.bodyIdA = bodyA;
    revoluteDef.bodyIdB = bodyB;
    revoluteDef.referenceAngle = 4.0f;
    revoluteDef.collideConnected = true;
    b2JointId revoluteId = b2CreateRevoluteJoint(worldId, &revoluteDef);
    printf("revolute %.9g\n", b2Joint_GetReferenceAngle(revoluteId));

    b2WeldJointDef weldDef = b2DefaultWeldJointDef();
    weldDef.bodyIdA = bodyA;
    weldDef.bodyIdB = bodyB;
    weldDef.referenceAngle = 1.25f;
    weldDef.collideConnected = true;
    b2JointId weldId = b2CreateWeldJoint(worldId, &weldDef);
    printf("weld %.9g\n", b2Joint_GetReferenceAngle(weldId));

    b2MotorJointDef motorDef = b2DefaultMotorJointDef();
    motorDef.bodyIdA = bodyA;
    motorDef.bodyIdB = bodyB;
    motorDef.collideConnected = true;
    b2JointId motorId = b2CreateMotorJoint(worldId, &motorDef);

    b2Body_SetTransform(bodyA, (b2Vec2){0.0f, 0.0f}, b2MakeRot(0.0f));
    b2Body_SetTransform(bodyB, (b2Vec2){0.5f, 0.0f}, b2MakeRot(0.0f));

    b2MouseJointDef mouseDef = b2DefaultMouseJointDef();
    mouseDef.bodyIdA = bodyA;
    mouseDef.bodyIdB = bodyB;
    mouseDef.target = (b2Vec2){0.25f, 0.75f};
    mouseDef.collideConnected = true;
    b2JointId mouseId = b2CreateMouseJoint(worldId, &mouseDef);
    print_vec("mouseAnchorA", b2Joint_GetLocalAnchorA(mouseId));
    print_vec("mouseAnchorB", b2Joint_GetLocalAnchorB(mouseId));

    b2FilterJointDef filterDef = b2DefaultFilterJointDef();
    filterDef.bodyIdA = bodyA;
    filterDef.bodyIdB = bodyB;
    b2JointId filterId = b2CreateFilterJoint(worldId, &filterDef);
    printf("types %d %d %d %d %d %d %d\n", b2Joint_GetType(filterId), b2Joint_GetType(motorId),
           b2Joint_GetType(mouseId), b2Joint_GetType(prismaticId), b2Joint_GetType(revoluteId),
           b2Joint_GetType(weldId), b2Joint_GetType(wheelId));
    b2World_Step(worldId, 1.0f / 60.0f, 4);
    printf("contacts2 %d %d\n", b2Joint_GetCollideConnected(filterId) ? 1 : 0, b2World_GetCounters(worldId).contactCount);

    b2DestroyJoint(distanceId);
    printf("destroyDistance %d %d %d\n", b2Joint_IsValid(distanceId) ? 1 : 0, b2Body_GetJointCount(bodyA),
           b2World_GetCounters(worldId).jointCount);
    b2DestroyBody(bodyB);
    printf("destroyBody %d %d %d\n", b2Joint_IsValid(filterId) ? 1 : 0, b2Body_GetJointCount(bodyA),
           b2World_GetCounters(worldId).jointCount);

    b2DestroyWorld(worldId);
    return 0;
}
