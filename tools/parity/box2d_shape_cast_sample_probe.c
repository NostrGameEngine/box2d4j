// SPDX-License-Identifier: MIT

#include "box2d/collision.h"
#include "box2d/math_functions.h"

#include <stdio.h>

int main(void)
{
    b2Polygon box = b2MakeOffsetBox(0.5f, 0.5f, b2Vec2_zero, b2Rot_identity);
    b2Vec2 point = b2Vec2_zero;
    b2ShapeCastPairInput input = {0};
    input.proxyA = b2MakeProxy(box.vertices, box.count, 0.0f);
    input.proxyB = b2MakeProxy(&point, 1, 0.2f);
    input.transformA = b2Transform_identity;
    input.transformB = (b2Transform){{-0.6f, 0.0f}, b2Rot_identity};
    input.translationB = (b2Vec2){2.0f, 0.0f};
    input.maxFraction = 1.0f;
    input.canEncroach = false;
    b2CastOutput output = b2ShapeCast(&input);

    b2Transform hitTransform = {
        b2MulAdd(input.transformB.p, output.fraction, input.translationB),
        input.transformB.q,
    };
    b2DistanceInput distanceInput = {
        input.proxyA,
        input.proxyB,
        b2Transform_identity,
        hitTransform,
        false,
    };
    b2SimplexCache cache = {0};
    b2DistanceOutput distance = b2ShapeDistance(&distanceInput, &cache, NULL, 0);

    printf("shapeCast %d %.9g %.9g %.9g %.9g %.9g %d",
           output.hit ? 1 : 0, output.fraction, output.point.x, output.point.y,
           output.normal.x, output.normal.y, output.iterations);
    printf(" %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %d %d %u\n",
           hitTransform.p.x, hitTransform.p.y, hitTransform.q.c, hitTransform.q.s,
           distance.distance, distance.pointA.x, distance.pointA.y, distance.pointB.x,
           distance.pointB.y, distance.normal.x, distance.normal.y, distance.iterations,
           distance.simplexCount, cache.count);
    return 0;
}
