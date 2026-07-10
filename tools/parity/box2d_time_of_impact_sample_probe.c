// SPDX-License-Identifier: MIT

#include "box2d/collision.h"
#include "box2d/math_functions.h"

#include <stdio.h>

static void print_segment(b2Transform transform, const b2Vec2* vertices)
{
    b2Vec2 p1 = b2TransformPoint(transform, vertices[0]);
    b2Vec2 p2 = b2TransformPoint(transform, vertices[1]);
    printf(" %.9g %.9g %.9g %.9g", p1.x, p1.y, p2.x, p2.y);
}

int main(void)
{
    b2Vec2 verticesA[4] = {
        {-16.25f, 44.75f}, {-15.75f, 44.75f}, {-15.75f, 45.25f}, {-16.25f, 45.25f},
    };
    b2Vec2 verticesB[2] = {{0.0f, -0.125000000f}, {0.0f, 0.125000000f}};
    b2Sweep sweepA = {
        b2Vec2_zero, {0.0f, 0.0f}, {0.0f, 0.0f}, b2Rot_identity, b2Rot_identity,
    };
    b2Sweep sweepB = {
        b2Vec2_zero,
        {-15.8332710f, 45.3520279f},
        {-15.8324337f, 45.3413048f},
        {-0.540891349f, 0.841092527f},
        {-0.457797021f, 0.889056742f},
    };

    b2TOIInput input = {
        b2MakeProxy(verticesA, 4, 0.0f),
        b2MakeProxy(verticesB, 2, 0.0299999993f),
        sweepA,
        sweepB,
        1.0f,
    };
    b2TOIOutput output = b2TimeOfImpact(&input);

    float distance = 0.0f;
    int distanceIterations = 0;
    int simplexCount = 0;
    b2Transform hit = b2GetSweepTransform(&sweepB, output.fraction);
    if (output.state == b2_toiStateHit)
    {
        b2DistanceInput distanceInput = {
            input.proxyA,
            input.proxyB,
            b2GetSweepTransform(&sweepA, output.fraction),
            hit,
            false,
        };
        b2SimplexCache cache = {0};
        b2DistanceOutput distanceOutput = b2ShapeDistance(&distanceInput, &cache, NULL, 0);
        distance = distanceOutput.distance;
        distanceIterations = distanceOutput.iterations;
        simplexCount = distanceOutput.simplexCount;
    }

    printf("timeOfImpact %d %.9g %.9g %d %d", output.state, output.fraction, distance,
           distanceIterations, simplexCount);
    print_segment(b2GetSweepTransform(&sweepB, 0.0f), verticesB);
    print_segment(hit, verticesB);
    print_segment(b2GetSweepTransform(&sweepB, 1.0f), verticesB);
    printf("\n");
    return 0;
}
