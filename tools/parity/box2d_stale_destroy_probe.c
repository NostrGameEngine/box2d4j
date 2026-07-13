// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <setjmp.h>
#include <stdio.h>

static jmp_buf assertionJump;
static b2WorldId staleWorld;
static b2BodyId staleBody;
static b2ShapeId staleShape;
static b2ChainId staleChain;
static b2JointId staleJoint;

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
        case 0: b2DestroyWorld(staleWorld); break;
        case 1: b2DestroyShape(staleShape, true); break;
        case 2: b2DestroyChain(staleChain); break;
        case 3: b2DestroyJoint(staleJoint); break;
        default: b2DestroyBody(staleBody); break;
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
    staleWorld = b2CreateWorld(&worldDef);
    b2DestroyWorld(staleWorld);

    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId bodyA = b2CreateBody(worldId, &bodyDef);
    bodyDef.type = b2_dynamicBody;
    b2BodyId bodyB = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Circle circle = {{0.0f, 0.0f}, 0.5f};
    staleShape = b2CreateCircleShape(bodyB, &shapeDef, &circle);
    b2DestroyShape(staleShape, true);

    b2ChainDef chainDef = b2DefaultChainDef();
    b2Vec2 points[] = {{-2.0f, 0.0f}, {-1.0f, 0.0f}, {0.0f, 0.0f}, {1.0f, 0.0f}};
    chainDef.points = points;
    chainDef.count = 4;
    staleChain = b2CreateChain(bodyA, &chainDef);
    b2DestroyChain(staleChain);

    b2DistanceJointDef jointDef = b2DefaultDistanceJointDef();
    jointDef.bodyIdA = bodyA;
    jointDef.bodyIdB = bodyB;
    jointDef.length = 1.0f;
    staleJoint = b2CreateDistanceJoint(worldId, &jointDef);
    b2DestroyJoint(staleJoint);

    staleBody = b2CreateBody(worldId, &bodyDef);
    b2DestroyBody(staleBody);

    b2SetAssertFcn(catch_assertion);
    printf("staleDestroy");
    for (int i = 0; i < 5; ++i)
    {
        printf(" %d", asserts(i));
    }
    printf("\n");
    b2SetAssertFcn(ignore_assertion);
    b2DestroyWorld(worldId);
    return 0;
}
