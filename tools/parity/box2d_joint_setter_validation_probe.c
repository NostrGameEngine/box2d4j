// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <setjmp.h>
#include <stdio.h>

static jmp_buf assertionJump;
static b2JointId prismaticId;
static b2JointId mouseId;
static b2JointId weldId;
static b2JointId revoluteId;
static b2JointId wheelId;

static int catch_assertion(const char* condition, const char* fileName, int lineNumber)
{
    (void)condition;
    (void)fileName;
    (void)lineNumber;
    longjmp(assertionJump, 1);
    return 0;
}

static int ignore_assertion(const char* condition, const char* fileName, int lineNumber)
{
    (void)condition;
    (void)fileName;
    (void)lineNumber;
    return 0;
}

static void run_case(int index)
{
    switch (index)
    {
        case 0: b2Joint_SetLocalAnchorA(prismaticId, (b2Vec2){NAN, 0.0f}); break;
        case 1: b2Joint_SetLocalAnchorB(prismaticId, (b2Vec2){0.0f, INFINITY}); break;
        case 2: b2Joint_SetReferenceAngle(prismaticId, NAN); break;
        case 3: b2Joint_SetLocalAxisA(prismaticId, (b2Vec2){NAN, 0.0f}); break;
        case 4: b2Joint_SetLocalAxisA(prismaticId, (b2Vec2){2.0f, 0.0f}); break;
        case 5: b2MouseJoint_SetTarget(mouseId, (b2Vec2){NAN, 0.0f}); break;
        case 6: b2MouseJoint_SetSpringHertz(mouseId, -1.0f); break;
        case 7: b2MouseJoint_SetSpringDampingRatio(mouseId, NAN); break;
        case 8: b2MouseJoint_SetMaxForce(mouseId, -1.0f); break;
        case 9: b2WeldJoint_SetLinearHertz(weldId, -1.0f); break;
        case 10: b2WeldJoint_SetLinearDampingRatio(weldId, -1.0f); break;
        case 11: b2WeldJoint_SetAngularHertz(weldId, NAN); break;
        case 12: b2WeldJoint_SetAngularDampingRatio(weldId, -1.0f); break;
        case 13: b2PrismaticJoint_SetLimits(prismaticId, 1.0f, -1.0f); break;
        case 14: b2RevoluteJoint_SetLimits(revoluteId, 1.0f, -1.0f); break;
        case 15: b2RevoluteJoint_SetLimits(revoluteId, -B2_PI, 0.0f); break;
        case 16: b2RevoluteJoint_SetLimits(revoluteId, 0.0f, B2_PI); break;
        case 17: b2WheelJoint_SetLimits(wheelId, 1.0f, -1.0f); break;
        case 18: b2Joint_SetConstraintTuning(prismaticId, -1.0f, 1.0f); break;
        default: b2Joint_SetConstraintTuning(prismaticId, 1.0f, NAN); break;
    }
}

static int asserts(int index)
{
    if (setjmp(assertionJump) == 0)
    {
        run_case(index);
        return 0;
    }
    return 1;
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId bodyA = b2CreateBody(worldId, &bodyDef);
    bodyDef.type = b2_dynamicBody;
    b2BodyId bodyB = b2CreateBody(worldId, &bodyDef);

    b2PrismaticJointDef prismaticDef = b2DefaultPrismaticJointDef();
    prismaticDef.bodyIdA = bodyA;
    prismaticDef.bodyIdB = bodyB;
    prismaticId = b2CreatePrismaticJoint(worldId, &prismaticDef);
    b2MouseJointDef mouseDef = b2DefaultMouseJointDef();
    mouseDef.bodyIdA = bodyA;
    mouseDef.bodyIdB = bodyB;
    mouseId = b2CreateMouseJoint(worldId, &mouseDef);
    b2WeldJointDef weldDef = b2DefaultWeldJointDef();
    weldDef.bodyIdA = bodyA;
    weldDef.bodyIdB = bodyB;
    weldId = b2CreateWeldJoint(worldId, &weldDef);
    b2RevoluteJointDef revoluteDef = b2DefaultRevoluteJointDef();
    revoluteDef.bodyIdA = bodyA;
    revoluteDef.bodyIdB = bodyB;
    revoluteId = b2CreateRevoluteJoint(worldId, &revoluteDef);
    b2WheelJointDef wheelDef = b2DefaultWheelJointDef();
    wheelDef.bodyIdA = bodyA;
    wheelDef.bodyIdB = bodyB;
    wheelId = b2CreateWheelJoint(worldId, &wheelDef);

    b2SetAssertFcn(catch_assertion);
    printf("jointSetterValidation");
    for (int i = 0; i < 20; ++i)
    {
        printf(" %d", asserts(i));
    }
    printf("\n");
    b2SetAssertFcn(ignore_assertion);
    b2DestroyWorld(worldId);
    return 0;
}
