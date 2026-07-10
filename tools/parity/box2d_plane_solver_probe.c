// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <float.h>
#include <stdio.h>

static void print_vec(const char* label, b2Vec2 v)
{
    printf("%s %.9g %.9g\n", label, v.x, v.y);
}

int main(void)
{
    b2CollisionPlane planes[3] = {
        { .plane = { .normal = { 1.0f, 0.0f }, .offset = 0.25f }, .pushLimit = FLT_MAX, .clipVelocity = true },
        { .plane = { .normal = { 0.0f, 1.0f }, .offset = -0.1f }, .pushLimit = 0.2f, .clipVelocity = true },
        { .plane = { .normal = { -0.707106769f, 0.707106769f }, .offset = -0.15f }, .pushLimit = 0.5f, .clipVelocity = false },
    };

    b2PlaneSolverResult result = b2SolvePlanes((b2Vec2){-0.6f, -0.7f}, planes, 3);
    print_vec("translation", result.translation);
    printf("iterations %d\n", result.iterationCount);
    printf("push %.9g %.9g %.9g\n", planes[0].push, planes[1].push, planes[2].push);
    print_vec("clip", b2ClipVector((b2Vec2){-2.0f, -3.0f}, planes, 3));

    b2CollisionPlane inactive[2] = {
        { .plane = { .normal = { 1.0f, 0.0f }, .offset = 0.0f }, .push = 0.0f, .pushLimit = 1.0f, .clipVelocity = true },
        { .plane = { .normal = { 0.0f, 1.0f }, .offset = 0.0f }, .push = 1.0f, .pushLimit = 1.0f, .clipVelocity = false },
    };
    print_vec("clipInactive", b2ClipVector((b2Vec2){-2.0f, -3.0f}, inactive, 2));
    return 0;
}
