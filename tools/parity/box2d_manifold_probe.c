// SPDX-License-Identifier: MIT

#include "box2d/collision.h"
#include "box2d/math_functions.h"

#include <stdio.h>

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

    return 0;
}
