// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <setjmp.h>
#include <stdio.h>

static jmp_buf assertionJump;

static int catch_assertion(const char* condition, const char* fileName, int lineNumber)
{
    (void)condition;
    (void)fileName;
    (void)lineNumber;
    longjmp(assertionJump, 1);
    return 0;
}

static b2DistanceInput distance_input(void)
{
    static b2Vec2 pointA = {0.0f, 0.0f};
    static b2Vec2 pointB = {2.0f, 0.0f};
    b2DistanceInput input = {0};
    input.proxyA = b2MakeProxy(&pointA, 1, 0.1f);
    input.proxyB = b2MakeProxy(&pointB, 1, 0.2f);
    input.transformA = b2Transform_identity;
    input.transformB = b2Transform_identity;
    return input;
}

static b2TOIInput toi_input(void)
{
    b2DistanceInput distance = distance_input();
    b2TOIInput input = {0};
    input.proxyA = distance.proxyA;
    input.proxyB = distance.proxyB;
    input.sweepA.q1 = b2Rot_identity;
    input.sweepA.q2 = b2Rot_identity;
    input.sweepB.q1 = b2Rot_identity;
    input.sweepB.q2 = b2Rot_identity;
    input.maxFraction = 1.0f;
    return input;
}

static void run_case(int index)
{
    b2DistanceInput input = distance_input();
    b2SimplexCache cache = {0};
    switch (index)
    {
        case 0: cache.count = 4; (void)b2ShapeDistance(&input, &cache, NULL, 0); break;
        case 1: input.proxyA.count = 0; (void)b2ShapeDistance(&input, &cache, NULL, 0); break;
        case 2: input.proxyB.count = 0; (void)b2ShapeDistance(&input, &cache, NULL, 0); break;
        case 3: input.proxyA.radius = -1.0f; (void)b2ShapeDistance(&input, &cache, NULL, 0); break;
        case 4: input.proxyB.radius = -1.0f; (void)b2ShapeDistance(&input, &cache, NULL, 0); break;
        case 5:
        {
            b2ShapeCastPairInput cast = {0};
            cast.proxyA = input.proxyA;
            cast.proxyB = input.proxyB;
            cast.proxyA.radius = -1.0f;
            cast.transformA = b2Transform_identity;
            cast.transformB = b2Transform_identity;
            cast.maxFraction = 1.0f;
            (void)b2ShapeCast(&cast);
            break;
        }
        default:
        {
            b2TOIInput toi = toi_input();
            if (index == 6) toi.sweepA.q1 = (b2Rot){0.0f, 0.0f};
            if (index == 7) toi.sweepA.q2 = (b2Rot){0.0f, 0.0f};
            if (index == 8) toi.sweepB.q1 = (b2Rot){0.0f, 0.0f};
            if (index == 9) toi.sweepB.q2 = (b2Rot){0.0f, 0.0f};
            (void)b2TimeOfImpact(&toi);
            break;
        }
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
    b2SetAssertFcn(catch_assertion);
    printf("distanceValidation");
    for (int i = 0; i < 11; ++i)
    {
        printf(" %d", asserts(i));
    }
    printf("\n");
    return 0;
}
