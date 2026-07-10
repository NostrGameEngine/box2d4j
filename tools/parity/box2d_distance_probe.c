// SPDX-License-Identifier: MIT

#include "box2d/collision.h"
#include "box2d/math_functions.h"

#include <stdio.h>

static void print_cast(const char* label, b2CastOutput cast)
{
    printf("%s %d %.9g %.9g %.9g %.9g %.9g %.9g %d\n",
           label,
           cast.hit ? 1 : 0,
           cast.fraction,
           cast.normal.x, cast.normal.y,
           cast.point.x, cast.point.y,
           0.0f,
           cast.iterations);
}

int main(void)
{
    b2Vec2 p1 = {-1.0f, -1.0f};
    b2Vec2 q1 = {-1.0f, 1.0f};
    b2Vec2 p2 = {2.0f, 0.0f};
    b2Vec2 q2 = {1.0f, 0.0f};
    b2SegmentDistanceResult segment = b2SegmentDistance(p1, q1, p2, q2);
    printf("segment %.9g %.9g %.9g %.9g %.9g %.9g %.9g\n",
           segment.fraction1, segment.fraction2,
           segment.closest1.x, segment.closest1.y,
           segment.closest2.x, segment.closest2.y,
           segment.distanceSquared);

    b2Vec2 vas[] = {{-1.0f, -1.0f}, {1.0f, -1.0f}, {1.0f, 1.0f}, {-1.0f, 1.0f}};
    b2Vec2 vbs[] = {{2.0f, -1.0f}, {2.0f, 1.0f}};

    b2DistanceInput input;
    input.proxyA = b2MakeProxy(vas, 4, 0.0f);
    input.proxyB = b2MakeProxy(vbs, 2, 0.0f);
    input.transformA = b2Transform_identity;
    input.transformB = b2Transform_identity;
    input.useRadii = false;

    b2SimplexCache cache = {0};
    b2DistanceOutput distance = b2ShapeDistance(&input, &cache, NULL, 0);
    printf("distance %.9g %.9g %.9g %.9g %.9g %.9g %.9g %d\n",
           distance.distance,
           distance.normal.x, distance.normal.y,
           distance.pointA.x, distance.pointA.y,
           distance.pointB.x, distance.pointB.y,
           distance.iterations);

    b2ShapeCastPairInput castInput = {0};
    castInput.proxyA = b2MakeProxy(vas, 4, 0.0f);
    castInput.proxyB = b2MakeProxy(vbs, 2, 0.0f);
    castInput.transformA = b2Transform_identity;
    castInput.transformB = b2Transform_identity;
    castInput.translationB = (b2Vec2){-2.0f, 0.0f};
    castInput.maxFraction = 1.0f;

    b2CastOutput cast = b2ShapeCast(&castInput);
    printf("cast %d %.9g %.9g %.9g %.9g %.9g %.9g %d\n",
           cast.hit ? 1 : 0,
           cast.fraction,
           cast.normal.x, cast.normal.y,
           cast.point.x, cast.point.y,
           0.0f,
           cast.iterations);

    b2TOIInput toiInput;
    toiInput.proxyA = b2MakeProxy(vas, 4, 0.0f);
    toiInput.proxyB = b2MakeProxy(vbs, 2, 0.0f);
    toiInput.sweepA = (b2Sweep){b2Vec2_zero, b2Vec2_zero, b2Vec2_zero, b2Rot_identity, b2Rot_identity};
    toiInput.sweepB = (b2Sweep){b2Vec2_zero, b2Vec2_zero, {-2.0f, 0.0f}, b2Rot_identity, b2Rot_identity};
    toiInput.maxFraction = 1.0f;

    b2TOIOutput toi = b2TimeOfImpact(&toiInput);
    printf("toi %d %.9g\n", toi.state, toi.fraction);

    b2Vec2 castProxyPoints[] = {{2.0f, -0.4f}, {2.0f, 0.4f}};
    b2ShapeCastInput shapeCastInput = {0};
    shapeCastInput.proxy = b2MakeProxy(castProxyPoints, 2, 0.1f);
    shapeCastInput.translation = (b2Vec2){-2.0f, 0.0f};
    shapeCastInput.maxFraction = 1.0f;
    shapeCastInput.canEncroach = false;

    b2Circle circle = {{0.0f, 0.0f}, 0.5f};
    print_cast("castCircle", b2ShapeCastCircle(&shapeCastInput, &circle));

    b2Capsule capsule = {{-0.5f, 0.0f}, {0.5f, 0.0f}, 0.25f};
    print_cast("castCapsule", b2ShapeCastCapsule(&shapeCastInput, &capsule));

    b2Segment segmentShape = {{-0.5f, -0.5f}, {-0.5f, 0.5f}};
    print_cast("castSegment", b2ShapeCastSegment(&shapeCastInput, &segmentShape));

    b2Polygon polygon = b2MakeBox(0.5f, 0.5f);
    print_cast("castPolygon", b2ShapeCastPolygon(&shapeCastInput, &polygon));

    return 0;
}
