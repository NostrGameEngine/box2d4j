// SPDX-License-Identifier: MIT

#include "box2d/math_functions.h"

#include <stdio.h>

static void PrintRot(const char* label, b2Rot q)
{
    printf("%s %.9g %.9g %.9g\n", label, q.c, q.s, b2Rot_GetAngle(q));
}

static void PrintVec(const char* label, b2Vec2 v)
{
    printf("%s %.9g %.9g\n", label, v.x, v.y);
}

int main(void)
{
    float atanInputs[][2] = {
        {1.0f, 0.0f},
        {-1.0f, 0.0f},
        {0.0f, 1.0f},
        {0.0f, -1.0f},
        {0.0f, 0.0f},
        {0.75f, -0.25f},
        {-0.33f, 0.91f},
    };

    for (int i = 0; i < 7; ++i)
    {
        printf("atan %d %.9g\n", i, b2Atan2(atanInputs[i][0], atanInputs[i][1]));
    }

    float angles[] = {-10.0f * B2_PI, -3.25f, -1.0f, 0.0f, 0.5f * B2_PI, 2.75f, 10.0f * B2_PI};
    for (int i = 0; i < 7; ++i)
    {
        b2Rot q = b2MakeRot(angles[i]);
        printf("rot %d %.9g %.9g %.9g\n", i, q.c, q.s, b2UnwindAngle(angles[i]));
    }

    b2Vec2 zero = b2Vec2_zero;
    b2Vec2 one = {1.0f, 1.0f};
    b2Vec2 two = {2.0f, 2.0f};
    PrintVec("add", b2Add(one, two));
    PrintVec("sub", b2Sub(zero, two));

    b2Transform transform1 = {{-2.0f, 3.0f}, b2MakeRot(1.0f)};
    b2Transform transform2 = {{1.0f, 0.0f}, b2MakeRot(-2.0f)};
    b2Transform transform = b2MulTransforms(transform2, transform1);
    PrintVec("transformNested", b2TransformPoint(transform2, b2TransformPoint(transform1, two)));
    PrintVec("transformMul", b2TransformPoint(transform, two));
    PrintVec("invTransform", b2InvTransformPoint(transform1, b2TransformPoint(transform1, two)));

    b2Vec2 v = b2Normalize((b2Vec2){0.2f, -0.5f});
    b2Vec2 unitVectors[] = {
        b2Normalize((b2Vec2){1.0f, 0.25f}),
        b2Normalize((b2Vec2){-0.2f, 0.9f}),
        b2Normalize((b2Vec2){-0.7f, -0.3f}),
    };
    for (int i = 0; i < 3; ++i)
    {
        b2Rot r = b2ComputeRotationBetweenUnitVectors(v, unitVectors[i]);
        b2Vec2 w = b2RotateVector(r, v);
        printf("between %d %.9g %.9g %.9g %.9g\n", i, r.c, r.s, w.x, w.y);
    }

    b2Rot q1 = b2Rot_identity;
    b2Rot q2 = b2MakeRot(0.5f * B2_PI);
    float alphas[] = {0.0f, 0.1f, 0.5f, 0.9f, 1.0f};
    for (int i = 0; i < 5; ++i)
    {
        PrintRot("nlerp", b2NLerp(q1, q2, alphas[i]));
    }

    return 0;
}
