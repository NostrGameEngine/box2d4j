// SPDX-License-Identifier: MIT

#include "box2d/collision.h"
#include "box2d/math_functions.h"

#include <stdint.h>
#include <stdio.h>
#include <string.h>

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

static uint32_t float_bits(float value)
{
    uint32_t bits;
    memcpy(&bits, &value, sizeof(bits));
    return bits;
}

static void print_float(float value)
{
    printf(" %08x", float_bits(value));
}

static uint32_t random_state = 0xa341316cu;

static uint32_t next_random(void)
{
    random_state = random_state * 1664525u + 1013904223u;
    return random_state;
}

static float random_signed(int denominator)
{
    int value = (int)((next_random() >> 16) & 2047u) - 1024;
    return (float)value / (float)denominator;
}

static float random_positive(void)
{
    return (float)(1u + ((next_random() >> 24) & 31u)) / 32.0f;
}

static b2Rot random_rotation(void)
{
    static const b2Rot rotations[] = {
        {1.0f, 0.0f}, {0.8f, 0.6f}, {0.6f, -0.8f},
        {-0.8f, 0.6f}, {-0.6f, -0.8f}, {0.0f, 1.0f}
    };
    return rotations[next_random() % (sizeof(rotations) / sizeof(rotations[0]))];
}

static b2ShapeProxy make_random_proxy(int type, float x, float y, float radius)
{
    if (type == 0)
    {
        b2Vec2 point = {0.0f, 0.0f};
        return b2MakeProxy(&point, 1, radius);
    }
    if (type == 1)
    {
        b2Vec2 points[2] = {{-x, 0.0f}, {x, 0.0f}};
        return b2MakeProxy(points, 2, radius);
    }
    b2Polygon box = b2MakeBox(x, y);
    return b2MakeProxy(box.vertices, box.count, radius);
}

static void print_proxy_parameters(int type, float x, float y, float radius)
{
    printf(" %d", type);
    print_float(x);
    print_float(y);
    print_float(radius);
}

static void print_transform(b2Transform transform)
{
    print_float(transform.p.x);
    print_float(transform.p.y);
    print_float(transform.q.c);
    print_float(transform.q.s);
}

static void print_sweep(b2Sweep sweep)
{
    print_float(sweep.c1.x);
    print_float(sweep.c1.y);
    print_float(sweep.c2.x);
    print_float(sweep.c2.y);
    print_float(sweep.q1.c);
    print_float(sweep.q1.s);
    print_float(sweep.q2.c);
    print_float(sweep.q2.s);
}

static void print_randomized_distance_cases(void)
{
    for (int case_index = 0; case_index < 512; ++case_index)
    {
        b2Vec2 p1 = {random_signed(128), random_signed(128)};
        b2Vec2 q1 = {p1.x + random_signed(256), p1.y + random_signed(256)};
        b2Vec2 p2 = {random_signed(128), random_signed(128)};
        b2Vec2 q2 = {p2.x + random_signed(256), p2.y + random_signed(256)};
        b2SegmentDistanceResult segment = b2SegmentDistance(p1, q1, p2, q2);
        printf("fuzzSegment");
        print_float(p1.x); print_float(p1.y); print_float(q1.x); print_float(q1.y);
        print_float(p2.x); print_float(p2.y); print_float(q2.x); print_float(q2.y);
        print_float(segment.fraction1); print_float(segment.fraction2);
        print_float(segment.closest1.x); print_float(segment.closest1.y);
        print_float(segment.closest2.x); print_float(segment.closest2.y);
        print_float(segment.distanceSquared);
        putchar('\n');
    }

    for (int case_index = 0; case_index < 512; ++case_index)
    {
        int type_a = (int)(next_random() % 3u);
        int type_b = (int)(next_random() % 3u);
        float ax = random_positive() + 0.125f;
        float ay = random_positive() + 0.125f;
        float ar = 0.25f * random_positive();
        float bx = random_positive() + 0.125f;
        float by = random_positive() + 0.125f;
        float br = 0.25f * random_positive();
        b2ShapeProxy proxy_a = make_random_proxy(type_a, ax, ay, ar);
        b2ShapeProxy proxy_b = make_random_proxy(type_b, bx, by, br);
        b2Transform transform_a = {{random_signed(128), random_signed(128)}, random_rotation()};
        b2Transform transform_b = {{random_signed(128), random_signed(128)}, random_rotation()};
        bool use_radii = (case_index & 1) != 0;
        b2DistanceInput input = {proxy_a, proxy_b, transform_a, transform_b, use_radii};
        b2SimplexCache cache = {0};
        b2DistanceOutput output = b2ShapeDistance(&input, &cache, NULL, 0);
        printf("fuzzDistance");
        print_proxy_parameters(type_a, ax, ay, ar); print_transform(transform_a);
        print_proxy_parameters(type_b, bx, by, br); print_transform(transform_b);
        printf(" %d", use_radii ? 1 : 0);
        print_float(output.pointA.x); print_float(output.pointA.y);
        print_float(output.pointB.x); print_float(output.pointB.y);
        print_float(output.normal.x); print_float(output.normal.y); print_float(output.distance);
        printf(" %d %d\n", output.iterations, output.simplexCount);
    }

    for (int case_index = 0; case_index < 256; ++case_index)
    {
        int type_a = (int)(next_random() % 3u);
        int type_b = (int)(next_random() % 3u);
        float ax = random_positive() + 0.125f;
        float ay = random_positive() + 0.125f;
        float ar = 0.25f * random_positive();
        float bx = random_positive() + 0.125f;
        float by = random_positive() + 0.125f;
        float br = 0.25f * random_positive();
        b2ShapeProxy proxy_a = make_random_proxy(type_a, ax, ay, ar);
        b2ShapeProxy proxy_b = make_random_proxy(type_b, bx, by, br);
        b2Transform transform_a = {{random_signed(128), random_signed(128)}, random_rotation()};
        b2Transform transform_b = {{random_signed(128), random_signed(128)}, random_rotation()};
        bool use_radii = (case_index & 1) != 0;
        b2DistanceInput input = {proxy_a, proxy_b, transform_a, transform_b, use_radii};
        b2SimplexCache cache = {0};
        (void)b2ShapeDistance(&input, &cache, NULL, 0);
        b2DistanceOutput output = b2ShapeDistance(&input, &cache, NULL, 0);
        printf("fuzzWarmDistance");
        print_proxy_parameters(type_a, ax, ay, ar); print_transform(transform_a);
        print_proxy_parameters(type_b, bx, by, br); print_transform(transform_b);
        printf(" %d", use_radii ? 1 : 0);
        print_float(output.pointA.x); print_float(output.pointA.y);
        print_float(output.pointB.x); print_float(output.pointB.y);
        print_float(output.normal.x); print_float(output.normal.y); print_float(output.distance);
        printf(" %d %d %u", output.iterations, output.simplexCount, cache.count);
        for (int i = 0; i < 3; ++i)
        {
            printf(" %u %u", cache.indexA[i], cache.indexB[i]);
        }
        putchar('\n');
    }

    for (int case_index = 0; case_index < 512; ++case_index)
    {
        int type_a = (int)(next_random() % 3u);
        int type_b = (int)(next_random() % 3u);
        float ax = random_positive() + 0.125f;
        float ay = random_positive() + 0.125f;
        float ar = 0.25f * random_positive();
        float bx = random_positive() + 0.125f;
        float by = random_positive() + 0.125f;
        float br = 0.25f * random_positive();
        b2ShapeCastPairInput input = {0};
        input.proxyA = make_random_proxy(type_a, ax, ay, ar);
        input.proxyB = make_random_proxy(type_b, bx, by, br);
        input.transformA = (b2Transform){{random_signed(256), random_signed(256)}, random_rotation()};
        input.transformB = (b2Transform){{random_signed(256), random_signed(256)}, random_rotation()};
        input.translationB = (b2Vec2){random_signed(128), random_signed(128)};
        input.maxFraction = (float)(1 + (case_index % 4)) / 4.0f;
        input.canEncroach = (case_index & 1) != 0;
        b2CastOutput output = b2ShapeCast(&input);
        printf("fuzzCast");
        print_proxy_parameters(type_a, ax, ay, ar); print_transform(input.transformA);
        print_proxy_parameters(type_b, bx, by, br); print_transform(input.transformB);
        print_float(input.translationB.x); print_float(input.translationB.y); print_float(input.maxFraction);
        printf(" %d %d", input.canEncroach ? 1 : 0, output.hit ? 1 : 0);
        print_float(output.point.x); print_float(output.point.y);
        print_float(output.normal.x); print_float(output.normal.y); print_float(output.fraction);
        printf(" %d\n", output.iterations);
    }

    for (int case_index = 0; case_index < 256; ++case_index)
    {
        int type_a = (int)(next_random() % 3u);
        int type_b = (int)(next_random() % 3u);
        float ax = random_positive() + 0.125f;
        float ay = random_positive() + 0.125f;
        float ar = 0.25f * random_positive();
        float bx = random_positive() + 0.125f;
        float by = random_positive() + 0.125f;
        float br = 0.25f * random_positive();
        b2TOIInput input = {0};
        input.proxyA = make_random_proxy(type_a, ax, ay, ar);
        input.proxyB = make_random_proxy(type_b, bx, by, br);
        input.sweepA = (b2Sweep){b2Vec2_zero,
            {random_signed(256), random_signed(256)}, {random_signed(256), random_signed(256)},
            random_rotation(), random_rotation()};
        input.sweepB = (b2Sweep){b2Vec2_zero,
            {random_signed(256), random_signed(256)}, {random_signed(256), random_signed(256)},
            random_rotation(), random_rotation()};
        input.maxFraction = (float)(1 + (case_index % 4)) / 4.0f;
        b2TOIOutput output = b2TimeOfImpact(&input);
        printf("fuzzToi");
        print_proxy_parameters(type_a, ax, ay, ar); print_sweep(input.sweepA);
        print_proxy_parameters(type_b, bx, by, br); print_sweep(input.sweepB);
        print_float(input.maxFraction);
        printf(" %d", output.state); print_float(output.fraction); putchar('\n');
    }
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

    print_randomized_distance_cases();

    return 0;
}
