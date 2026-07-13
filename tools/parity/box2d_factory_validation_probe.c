// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <setjmp.h>
#include <stdio.h>

static jmp_buf assertionJump;
static b2WorldId worldId;
static b2BodyId bodyA;
static b2BodyId bodyB;

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
    if (index == 0)
    {
        b2WorldDef def = {0};
        (void)b2CreateWorld(&def);
    }
    else if (index == 1)
    {
        b2BodyDef def = {0};
        (void)b2CreateBody(worldId, &def);
    }
    else if (index == 2)
    {
        b2ShapeDef def = {0};
        b2Circle circle = {{0.0f, 0.0f}, 0.5f};
        (void)b2CreateCircleShape(bodyA, &def, &circle);
    }
    else if (index == 3)
    {
        b2ChainDef def = {0};
        (void)b2CreateChain(bodyA, &def);
    }
    else if (index == 4)
    {
        b2DistanceJointDef def = {0};
        (void)b2CreateDistanceJoint(worldId, &def);
    }
    else if (index == 5)
    {
        b2MotorJointDef def = {0};
        (void)b2CreateMotorJoint(worldId, &def);
    }
    else if (index == 6)
    {
        b2MouseJointDef def = {0};
        (void)b2CreateMouseJoint(worldId, &def);
    }
    else if (index == 7)
    {
        b2FilterJointDef def = {0};
        (void)b2CreateFilterJoint(worldId, &def);
    }
    else if (index == 8)
    {
        b2RevoluteJointDef def = {0};
        (void)b2CreateRevoluteJoint(worldId, &def);
    }
    else if (index == 9)
    {
        b2PrismaticJointDef def = {0};
        (void)b2CreatePrismaticJoint(worldId, &def);
    }
    else if (index == 10)
    {
        b2WeldJointDef def = {0};
        (void)b2CreateWeldJoint(worldId, &def);
    }
    else if (index == 11)
    {
        b2WheelJointDef def = {0};
        (void)b2CreateWheelJoint(worldId, &def);
    }
    else if (index == 12)
    {
        b2DistanceJointDef def = b2DefaultDistanceJointDef();
        def.bodyIdA = bodyA;
        def.bodyIdB = bodyB;
        def.length = 0.0f;
        (void)b2CreateDistanceJoint(worldId, &def);
    }
    else if (index == 13)
    {
        b2RevoluteJointDef def = b2DefaultRevoluteJointDef();
        def.bodyIdA = bodyA;
        def.bodyIdB = bodyB;
        def.lowerAngle = 1.0f;
        def.upperAngle = -1.0f;
        (void)b2CreateRevoluteJoint(worldId, &def);
    }
    else if (index == 14)
    {
        b2RevoluteJointDef def = b2DefaultRevoluteJointDef();
        def.bodyIdA = bodyA;
        def.bodyIdB = bodyB;
        def.lowerAngle = -B2_PI;
        (void)b2CreateRevoluteJoint(worldId, &def);
    }
    else if (index == 15)
    {
        b2RevoluteJointDef def = b2DefaultRevoluteJointDef();
        def.bodyIdA = bodyA;
        def.bodyIdB = bodyB;
        def.upperAngle = B2_PI;
        (void)b2CreateRevoluteJoint(worldId, &def);
    }
    else if (index == 16)
    {
        b2PrismaticJointDef def = b2DefaultPrismaticJointDef();
        def.bodyIdA = bodyA;
        def.bodyIdB = bodyB;
        def.lowerTranslation = 1.0f;
        def.upperTranslation = -1.0f;
        (void)b2CreatePrismaticJoint(worldId, &def);
    }
    else
    {
        b2WheelJointDef def = b2DefaultWheelJointDef();
        def.bodyIdA = bodyA;
        def.bodyIdB = bodyB;
        def.lowerTranslation = 1.0f;
        def.upperTranslation = -1.0f;
        (void)b2CreateWheelJoint(worldId, &def);
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
    worldId = b2CreateWorld(&worldDef);
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyA = b2CreateBody(worldId, &bodyDef);
    bodyB = b2CreateBody(worldId, &bodyDef);

    b2SetAssertFcn(catch_assertion);
    printf("factoryValidation");
    for (int i = 0; i < 18; ++i)
    {
        printf(" %d", asserts(i));
    }
    printf("\n");

    b2SetAssertFcn(ignore_assertion);
    b2DestroyWorld(worldId);
    return 0;
}
