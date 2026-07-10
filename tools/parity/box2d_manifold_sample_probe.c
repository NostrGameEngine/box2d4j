// SPDX-License-Identifier: MIT

#include "box2d/collision.h"
#include "box2d/math_functions.h"

#include <stdio.h>

typedef struct Caches
{
    b2SimplexCache rounded1;
    b2SimplexCache rounded2;
    b2SimplexCache capsule1;
    b2SimplexCache capsule2;
} Caches;

static b2Transform transform(b2Vec2 offset)
{
    return (b2Transform){offset, b2Rot_identity};
}

static b2Transform relative_transform(b2Transform relative, b2Vec2 offset)
{
    return (b2Transform){b2Add(relative.p, offset), relative.q};
}

static b2ChainSegment chain_segment(b2Vec2 ghost1, b2Vec2 point1, b2Vec2 point2, b2Vec2 ghost2)
{
    return (b2ChainSegment){ghost1, {point1, point2}, ghost2, -1};
}

static void print_manifold(b2Manifold manifold)
{
    printf(" %d %.9g %.9g %.9g", manifold.pointCount, manifold.normal.x, manifold.normal.y,
           manifold.rollingImpulse);
    for (int i = 0; i < manifold.pointCount; ++i)
    {
        b2ManifoldPoint* point = manifold.points + i;
        printf(" %.9g %.9g %.9g %.9g %.9g %.9g %.9g %u", point->point.x, point->point.y,
               point->anchorA.x, point->anchorA.y, point->anchorB.x, point->anchorB.y,
               point->separation, point->id);
    }
}

static void print_cache(b2SimplexCache cache)
{
    printf(" %u %u %u %u %u %u %u", cache.count, cache.indexA[0], cache.indexA[1], cache.indexA[2],
           cache.indexB[0], cache.indexB[1], cache.indexB[2]);
}

static void compute_pass(Caches* caches)
{
    b2Manifold manifolds[20];
    int count = 0;
    b2Transform relative = {{0.17f, 1.12f}, b2Rot_identity};
    float round = 0.1f;
    b2Vec2 offset = {-10.0f, -5.0f};
    b2Vec2 increment = {4.0f, 0.0f};

    b2Circle circle1 = {b2Vec2_zero, 0.5f};
    b2Circle circle2 = {b2Vec2_zero, 1.0f};
    manifolds[count++] = b2CollideCircles(&circle1, transform(offset), &circle2,
                                           relative_transform(relative, offset));
    offset = b2Add(offset, increment);

    b2Capsule capsule1 = {{-0.5f, 0.0f}, {0.5f, 0.0f}, 0.25f};
    b2Circle circle = {b2Vec2_zero, 0.5f};
    manifolds[count++] = b2CollideCapsuleAndCircle(&capsule1, transform(offset), &circle,
                                                   relative_transform(relative, offset));
    offset = b2Add(offset, increment);

    b2Segment segment = {{-1.0f, 0.0f}, {1.0f, 0.0f}};
    manifolds[count++] = b2CollideSegmentAndCircle(&segment, transform(offset), &circle,
                                                   relative_transform(relative, offset));
    offset = b2Add(offset, increment);

    b2Polygon box = b2MakeSquare(0.5f);
    box.radius = round;
    manifolds[count++] = b2CollidePolygonAndCircle(&box, transform(offset), &circle,
                                                   relative_transform(relative, offset));
    offset = b2Add(offset, increment);

    b2Capsule capsule2 = {{0.25f, 0.0f}, {1.0f, 0.0f}, 0.1f};
    manifolds[count++] = b2CollideCapsules(&capsule1, transform(offset), &capsule2,
                                           relative_transform(relative, offset));
    offset = b2Add(offset, increment);

    b2Capsule shortCapsule = {{-0.4f, 0.0f}, {-0.1f, 0.0f}, 0.1f};
    box = b2MakeOffsetBox(0.25f, 1.0f, (b2Vec2){1.0f, -1.0f}, b2MakeRot(0.25f * B2_PI));
    manifolds[count++] = b2CollidePolygonAndCapsule(&box, transform(offset), &shortCapsule,
                                                    relative_transform(relative, offset));
    offset = b2Add(offset, increment);

    manifolds[count++] = b2CollideSegmentAndCapsule(&segment, transform(offset), &capsule1,
                                                    relative_transform(relative, offset));

    offset = (b2Vec2){-10.0f, 0.0f};
    b2Polygon square1 = b2MakeSquare(0.5f);
    b2Polygon square2 = b2MakeSquare(0.5f);
    manifolds[count++] = b2CollidePolygons(&square1, transform(offset), &square2,
                                           relative_transform(relative, offset));
    offset = b2Add(offset, increment);

    b2Polygon longBox = b2MakeBox(2.0f, 0.1f);
    b2Polygon smallBox = b2MakeSquare(0.25f);
    manifolds[count++] = b2CollidePolygons(&longBox, transform(offset), &smallBox,
                                           relative_transform(relative, offset));
    offset = b2Add(offset, increment);

    square1 = b2MakeSquare(0.5f);
    float h = 0.5f - round;
    b2Polygon roundedBox = b2MakeRoundedBox(h, h, round);
    manifolds[count++] = b2CollidePolygons(&square1, transform(offset), &roundedBox,
                                           relative_transform(relative, offset));
    offset = b2Add(offset, increment);

    manifolds[count++] = b2CollidePolygons(&roundedBox, transform(offset), &roundedBox,
                                           relative_transform(relative, offset));
    offset = b2Add(offset, increment);

    manifolds[count++] = b2CollideSegmentAndPolygon(&segment, transform(offset), &roundedBox,
                                                    relative_transform(relative, offset));
    offset = b2Add(offset, increment);

    b2Vec2 wedgePoints[3] = {{-0.1f, -0.5f}, {0.1f, -0.5f}, {0.0f, 0.5f}};
    b2Hull wedge = b2ComputeHull(wedgePoints, 3);
    b2Polygon wox = b2MakePolygon(&wedge, round);
    manifolds[count++] = b2CollidePolygons(&wox, transform(offset), &wox,
                                           relative_transform(relative, offset));
    offset = b2Add(offset, increment);

    b2Vec2 p1s[3] = {
        {0.175740838f, 0.224936664f}, {-0.301293969f, 0.194021404f}, {-0.105151534f, -0.432157338f},
    };
    b2Vec2 p2s[3] = {
        {-0.427884758f, -0.225028217f}, {0.0566576123f, -0.128772855f}, {0.176625848f, 0.338923335f},
    };
    b2Hull h1 = b2ComputeHull(p1s, 3);
    b2Hull h2 = b2ComputeHull(p2s, 3);
    b2Polygon w1 = b2MakePolygon(&h1, 0.158798501f);
    b2Polygon w2 = b2MakePolygon(&h2, 0.205900759f);
    manifolds[count++] = b2CollidePolygons(&w1, transform(offset), &w2,
                                           relative_transform(relative, offset));

    offset = (b2Vec2){-10.0f, 5.0f};
    b2Polygon bigBox = b2MakeBox(1.0f, 1.0f);
    b2Vec2 trianglePoints[3] = {{-0.05f, 0.0f}, {0.05f, 0.0f}, {0.0f, 0.1f}};
    b2Hull triangleHull = b2ComputeHull(trianglePoints, 3);
    b2Polygon triangle = b2MakePolygon(&triangleHull, 0.0f);
    manifolds[count++] = b2CollidePolygons(&bigBox, transform(offset), &triangle,
                                           relative_transform(relative, offset));
    offset = b2Add(offset, increment);

    b2ChainSegment chain1 = chain_segment((b2Vec2){2.0f, 1.0f}, (b2Vec2){1.0f, 1.0f},
                                           (b2Vec2){-1.0f, 0.0f}, (b2Vec2){-2.0f, 0.0f});
    manifolds[count++] = b2CollideChainSegmentAndCircle(&chain1, transform(offset), &circle,
                                                        relative_transform(relative, offset));
    offset.x += 2.0f * increment.x;

    b2ChainSegment chain2 = chain_segment((b2Vec2){3.0f, 1.0f}, (b2Vec2){2.0f, 1.0f},
                                           (b2Vec2){1.0f, 1.0f}, (b2Vec2){-1.0f, 0.0f});
    manifolds[count++] = b2CollideChainSegmentAndPolygon(&chain1, transform(offset), &roundedBox,
                                                         relative_transform(relative, offset), &caches->rounded1);
    manifolds[count++] = b2CollideChainSegmentAndPolygon(&chain2, transform(offset), &roundedBox,
                                                         relative_transform(relative, offset), &caches->rounded2);
    offset.x += 2.0f * increment.x;

    manifolds[count++] = b2CollideChainSegmentAndCapsule(&chain1, transform(offset), &capsule1,
                                                         relative_transform(relative, offset), &caches->capsule1);
    manifolds[count++] = b2CollideChainSegmentAndCapsule(&chain2, transform(offset), &capsule1,
                                                         relative_transform(relative, offset), &caches->capsule2);

    printf(" %d", count);
    for (int i = 0; i < count; ++i)
    {
        print_manifold(manifolds[i]);
    }
    printf(" 4");
    print_cache(caches->rounded1);
    print_cache(caches->rounded2);
    print_cache(caches->capsule1);
    print_cache(caches->capsule2);
}

int main(void)
{
    Caches caches = {0};
    printf("manifold 2");
    compute_pass(&caches);
    compute_pass(&caches);
    printf("\n");
    return 0;
}
