// SPDX-License-Identifier: MIT

#include "box2d/collision.h"
#include "box2d/math_functions.h"

#include <stdint.h>
#include <stdio.h>
#include <string.h>

static void print_manifold(const char* label, b2Manifold m)
{
    b2ManifoldPoint p = m.points[0];
    b2ManifoldPoint q = m.points[1];
    printf("%s %d %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %u %.9g %.9g %.9g %.9g %.9g %.9g %.9g %u\n",
           label,
           m.pointCount,
           m.normal.x, m.normal.y,
           p.anchorA.x, p.anchorA.y,
           p.anchorB.x, p.anchorB.y,
           p.point.x, p.point.y,
           p.separation,
           p.normalImpulse,
           p.tangentImpulse,
           p.id,
           q.anchorA.x, q.anchorA.y,
           q.anchorB.x, q.anchorB.y,
           q.point.x, q.point.y,
           q.separation,
           q.id);
}

static uint32_t float_bits(float value)
{
    uint32_t bits;
    memcpy(&bits, &value, sizeof(bits));
    return bits;
}

static void print_fuzz(int type, const float* input, int input_count, b2Manifold manifold)
{
    printf("fuzz %d %d", type, input_count);
    for (int i = 0; i < input_count; ++i)
    {
        printf(" %08x", float_bits(input[i]));
    }

    printf(" %d %08x %08x %08x", manifold.pointCount, float_bits(manifold.normal.x),
           float_bits(manifold.normal.y), float_bits(manifold.rollingImpulse));
    for (int i = 0; i < 2; ++i)
    {
        b2ManifoldPoint point = manifold.points[i];
        printf(" %08x %08x %08x %08x %08x %08x %08x %08x %08x %08x %08x %u %d",
               float_bits(point.point.x), float_bits(point.point.y),
               float_bits(point.anchorA.x), float_bits(point.anchorA.y),
               float_bits(point.anchorB.x), float_bits(point.anchorB.y),
               float_bits(point.separation), float_bits(point.normalImpulse),
               float_bits(point.tangentImpulse), float_bits(point.totalNormalImpulse),
               float_bits(point.normalVelocity), point.id, point.persisted ? 1 : 0);
    }
    putchar('\n');
}

static uint32_t random_state = 0x6d2b79f5u;

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

static float random_radius(void)
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

static b2Transform random_transform(b2Vec2 base)
{
    b2Transform transform = {base, random_rotation()};
    return transform;
}

static void print_randomized_manifolds(void)
{
    for (int case_index = 0; case_index < 1024; ++case_index)
    {
        int type = case_index & 7;
        b2Vec2 base = {random_signed(128), random_signed(128)};
        b2Transform transform_a = random_transform(base);
        b2Vec2 offset = {random_signed(512), random_signed(512)};
        b2Transform transform_b = random_transform(b2Add(base, offset));

        if (type == 0)
        {
            b2Circle a = {{random_signed(512), random_signed(512)}, random_radius()};
            b2Circle b = {{random_signed(512), random_signed(512)}, random_radius()};
            float input[] = {a.center.x, a.center.y, a.radius,
                             transform_a.p.x, transform_a.p.y, transform_a.q.c, transform_a.q.s,
                             b.center.x, b.center.y, b.radius,
                             transform_b.p.x, transform_b.p.y, transform_b.q.c, transform_b.q.s};
            print_fuzz(type, input, 14, b2CollideCircles(&a, transform_a, &b, transform_b));
        }
        else if (type == 1)
        {
            float half_length = random_radius() + 0.125f;
            float axis_offset = random_signed(1024);
            b2Capsule a = {{-half_length, axis_offset}, {half_length, axis_offset}, random_radius()};
            b2Circle b = {{random_signed(512), random_signed(512)}, random_radius()};
            float input[] = {a.center1.x, a.center1.y, a.center2.x, a.center2.y, a.radius,
                             transform_a.p.x, transform_a.p.y, transform_a.q.c, transform_a.q.s,
                             b.center.x, b.center.y, b.radius,
                             transform_b.p.x, transform_b.p.y, transform_b.q.c, transform_b.q.s};
            print_fuzz(type, input, 16, b2CollideCapsuleAndCircle(&a, transform_a, &b, transform_b));
        }
        else if (type == 2)
        {
            float half_a = random_radius() + 0.125f;
            float half_b = random_radius() + 0.125f;
            b2Capsule a = {{-half_a, random_signed(1024)}, {half_a, random_signed(1024)}, random_radius()};
            b2Capsule b = {{-half_b, random_signed(1024)}, {half_b, random_signed(1024)}, random_radius()};
            float input[] = {a.center1.x, a.center1.y, a.center2.x, a.center2.y, a.radius,
                             transform_a.p.x, transform_a.p.y, transform_a.q.c, transform_a.q.s,
                             b.center1.x, b.center1.y, b.center2.x, b.center2.y, b.radius,
                             transform_b.p.x, transform_b.p.y, transform_b.q.c, transform_b.q.s};
            print_fuzz(type, input, 18, b2CollideCapsules(&a, transform_a, &b, transform_b));
        }
        else if (type == 3)
        {
            float half_width_a = random_radius() + 0.125f;
            float half_height_a = random_radius() + 0.125f;
            float radius_a = 0.25f * b2MinFloat(half_width_a, half_height_a) * random_radius();
            float half_width_b = random_radius() + 0.125f;
            float half_height_b = random_radius() + 0.125f;
            float radius_b = 0.25f * b2MinFloat(half_width_b, half_height_b) * random_radius();
            b2Polygon a = b2MakeRoundedBox(half_width_a, half_height_a, radius_a);
            b2Polygon b = b2MakeRoundedBox(half_width_b, half_height_b, radius_b);
            float input[] = {half_width_a, half_height_a, radius_a,
                             transform_a.p.x, transform_a.p.y, transform_a.q.c, transform_a.q.s,
                             half_width_b, half_height_b, radius_b,
                             transform_b.p.x, transform_b.p.y, transform_b.q.c, transform_b.q.s};
            print_fuzz(type, input, 14, b2CollidePolygons(&a, transform_a, &b, transform_b));
        }
        else if (type == 4)
        {
            float half_length = random_radius() + 0.125f;
            b2Segment a = {{-half_length, random_signed(1024)}, {half_length, random_signed(1024)}};
            b2Circle b = {{random_signed(512), random_signed(512)}, random_radius()};
            float input[] = {a.point1.x, a.point1.y, a.point2.x, a.point2.y,
                             transform_a.p.x, transform_a.p.y, transform_a.q.c, transform_a.q.s,
                             b.center.x, b.center.y, b.radius,
                             transform_b.p.x, transform_b.p.y, transform_b.q.c, transform_b.q.s};
            print_fuzz(type, input, 15, b2CollideSegmentAndCircle(&a, transform_a, &b, transform_b));
        }
        else if (type == 5)
        {
            float half_a = random_radius() + 0.125f;
            float half_b = random_radius() + 0.125f;
            b2Segment a = {{-half_a, random_signed(1024)}, {half_a, random_signed(1024)}};
            b2Capsule b = {{-half_b, random_signed(1024)}, {half_b, random_signed(1024)}, random_radius()};
            float input[] = {a.point1.x, a.point1.y, a.point2.x, a.point2.y,
                             transform_a.p.x, transform_a.p.y, transform_a.q.c, transform_a.q.s,
                             b.center1.x, b.center1.y, b.center2.x, b.center2.y, b.radius,
                             transform_b.p.x, transform_b.p.y, transform_b.q.c, transform_b.q.s};
            print_fuzz(type, input, 17, b2CollideSegmentAndCapsule(&a, transform_a, &b, transform_b));
        }
        else if (type == 6)
        {
            float half_length = random_radius() + 0.125f;
            b2Segment a = {{-half_length, random_signed(1024)}, {half_length, random_signed(1024)}};
            float half_width = random_radius() + 0.125f;
            float half_height = random_radius() + 0.125f;
            float radius = 0.25f * b2MinFloat(half_width, half_height) * random_radius();
            b2Polygon b = b2MakeRoundedBox(half_width, half_height, radius);
            float input[] = {a.point1.x, a.point1.y, a.point2.x, a.point2.y,
                             transform_a.p.x, transform_a.p.y, transform_a.q.c, transform_a.q.s,
                             half_width, half_height, radius,
                             transform_b.p.x, transform_b.p.y, transform_b.q.c, transform_b.q.s};
            print_fuzz(type, input, 15, b2CollideSegmentAndPolygon(&a, transform_a, &b, transform_b));
        }
        else
        {
            float half_width = random_radius() + 0.125f;
            float half_height = random_radius() + 0.125f;
            float radius_a = 0.25f * b2MinFloat(half_width, half_height) * random_radius();
            b2Polygon a = b2MakeRoundedBox(half_width, half_height, radius_a);
            float half_b = random_radius() + 0.125f;
            b2Capsule b = {{-half_b, random_signed(1024)}, {half_b, random_signed(1024)}, random_radius()};
            float input[] = {half_width, half_height, radius_a,
                             transform_a.p.x, transform_a.p.y, transform_a.q.c, transform_a.q.s,
                             b.center1.x, b.center1.y, b.center2.x, b.center2.y, b.radius,
                             transform_b.p.x, transform_b.p.y, transform_b.q.c, transform_b.q.s};
            print_fuzz(type, input, 16, b2CollidePolygonAndCapsule(&a, transform_a, &b, transform_b));
        }
    }
}

int main(void)
{
    b2Circle circleA = {{0.0f, 0.0f}, 1.0f};
    b2Circle circleB = {{1.5f, 0.0f}, 1.0f};
    print_manifold("circles", b2CollideCircles(&circleA, b2Transform_identity, &circleB, b2Transform_identity));

    b2Capsule capsule = {{-1.0f, 0.0f}, {1.0f, 0.0f}, 0.5f};
    b2Circle capsuleCircle = {{1.2f, 0.4f}, 0.25f};
    print_manifold("capsuleCircle", b2CollideCapsuleAndCircle(&capsule, b2Transform_identity, &capsuleCircle, b2Transform_identity));

    b2Polygon box = b2MakeBox(1.0f, 1.0f);
    b2Circle polygonCircle = {{1.2f, 0.3f}, 0.5f};
    print_manifold("polygonCircle", b2CollidePolygonAndCircle(&box, b2Transform_identity, &polygonCircle, b2Transform_identity));

    b2Segment segment = {{0.0f, -1.0f}, {0.0f, 1.0f}};
    b2Circle segmentCircle = {{0.2f, 0.0f}, 0.5f};
    print_manifold("segmentCircle", b2CollideSegmentAndCircle(&segment, b2Transform_identity, &segmentCircle, b2Transform_identity));

    b2Capsule capsuleA = {{-1.0f, 0.0f}, {1.0f, 0.0f}, 0.25f};
    b2Capsule capsuleB = {{-0.5f, 0.3f}, {1.5f, 0.3f}, 0.25f};
    print_manifold("capsules", b2CollideCapsules(&capsuleA, b2Transform_identity, &capsuleB, b2Transform_identity));

    b2Polygon boxA = b2MakeBox(1.0f, 1.0f);
    b2Polygon boxB = b2MakeOffsetBox(1.0f, 1.0f, (b2Vec2){0.5f, 0.25f}, b2MakeRot(0.15f));
    print_manifold("polygons", b2CollidePolygons(&boxA, b2Transform_identity, &boxB, b2Transform_identity));

    b2Segment segmentPoly = {{0.0f, 0.0f}, {2.0f, 0.0f}};
    b2Polygon segmentBox = b2MakeOffsetBox(0.35f, 0.2f, (b2Vec2){1.0f, 0.2f}, b2MakeRot(0.1f));
    print_manifold("segmentPolygon",
                   b2CollideSegmentAndPolygon(&segmentPoly, b2Transform_identity, &segmentBox, b2Transform_identity));

    b2ChainSegment chain = {{-1.0f, -1.0f}, {{0.0f, 0.0f}, {2.0f, 0.0f}}, {3.0f, -1.0f}, 7};
    b2Circle chainCircle = {{1.0f, 0.2f}, 0.35f};
    print_manifold("chainCircle", b2CollideChainSegmentAndCircle(&chain, b2Transform_identity, &chainCircle, b2Transform_identity));

    b2Capsule chainCapsule = {{0.5f, 0.25f}, {1.6f, 0.25f}, 0.2f};
    b2SimplexCache chainCapsuleCache = {0};
    print_manifold("chainCapsule",
                   b2CollideChainSegmentAndCapsule(&chain, b2Transform_identity, &chainCapsule, b2Transform_identity,
                                                   &chainCapsuleCache));

    b2Polygon chainPolygon = b2MakeOffsetBox(0.45f, 0.2f, (b2Vec2){1.0f, 0.25f}, b2MakeRot(0.05f));
    b2SimplexCache chainPolygonCache = {0};
    print_manifold("chainPolygon",
                   b2CollideChainSegmentAndPolygon(&chain, b2Transform_identity, &chainPolygon, b2Transform_identity,
                                                   &chainPolygonCache));

    print_randomized_manifolds();

    return 0;
}
