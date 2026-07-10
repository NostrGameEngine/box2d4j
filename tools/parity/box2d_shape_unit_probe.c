// SPDX-License-Identifier: MIT

#include "box2d/collision.h"
#include "box2d/math_functions.h"

#include <math.h>
#include <stdio.h>

#define N 4

static void PrintMass(const char* label, b2MassData md)
{
    printf("%s %.9g %.9g %.9g %.9g\n", label, md.mass, md.center.x, md.center.y, md.rotationalInertia);
}

static void PrintAABB(const char* label, b2AABB aabb)
{
    printf("%s %.9g %.9g %.9g %.9g\n", label, aabb.lowerBound.x, aabb.lowerBound.y, aabb.upperBound.x,
           aabb.upperBound.y);
}

static void PrintCast(const char* label, b2CastOutput output)
{
    printf("%s %d %.9g %.9g %.9g\n", label, output.hit, output.fraction, output.normal.x, output.normal.y);
}

int main(void)
{
    b2Capsule capsule = {{-1.0f, 0.0f}, {1.0f, 0.0f}, 1.0f};
    b2Circle circle = {{1.0f, 0.0f}, 1.0f};
    b2Polygon box = b2MakeBox(1.0f, 1.0f);
    b2Segment segment = {{0.0f, 1.0f}, {0.0f, -1.0f}};

    PrintMass("massCircle", b2ComputeCircleMass(&circle, 1.0f));

    float radius = capsule.radius;
    float length = b2Distance(capsule.center1, capsule.center2);
    b2MassData capsuleMass = b2ComputeCapsuleMass(&capsule, 1.0f);
    b2Polygon containingBox = b2MakeBox(radius, radius + 0.5f * length);
    b2MassData containingBoxMass = b2ComputePolygonMass(&containingBox, 1.0f);

    b2Vec2 points[2 * N];
    float d = B2_PI / (N - 1.0f);
    float angle = -0.5f * B2_PI;
    for (int i = 0; i < N; ++i)
    {
        points[i].x = 1.0f + radius * cosf(angle);
        points[i].y = radius * sinf(angle);
        angle += d;
    }

    angle = 0.5f * B2_PI;
    for (int i = N; i < 2 * N; ++i)
    {
        points[i].x = -1.0f + radius * cosf(angle);
        points[i].y = radius * sinf(angle);
        angle += d;
    }

    b2Hull hull = b2ComputeHull(points, 2 * N);
    b2Polygon approximateCapsule = b2MakePolygon(&hull, 0.0f);
    b2MassData approximateMass = b2ComputePolygonMass(&approximateCapsule, 1.0f);

    PrintMass("massCapsule", capsuleMass);
    PrintMass("massCapsuleContainingBox", containingBoxMass);
    printf("massCapsuleBounds %d %d\n", approximateMass.mass < capsuleMass.mass && capsuleMass.mass < containingBoxMass.mass,
           approximateMass.rotationalInertia < capsuleMass.rotationalInertia &&
               capsuleMass.rotationalInertia < containingBoxMass.rotationalInertia);
    PrintMass("massBox", b2ComputePolygonMass(&box, 1.0f));

    PrintAABB("aabbCircle", b2ComputeCircleAABB(&circle, b2Transform_identity));
    PrintAABB("aabbBox", b2ComputePolygonAABB(&box, b2Transform_identity));
    PrintAABB("aabbSegment", b2ComputeSegmentAABB(&segment, b2Transform_identity));

    b2Vec2 p1 = {0.5f, 0.5f};
    b2Vec2 p2 = {4.0f, -4.0f};
    printf("pointCircle %d %d\n", b2PointInCircle(p1, &circle), b2PointInCircle(p2, &circle));
    printf("pointPolygon %d %d\n", b2PointInPolygon(p1, &box), b2PointInPolygon(p2, &box));

    b2RayCastInput input = {{-4.0f, 0.0f}, {8.0f, 0.0f}, 1.0f};
    PrintCast("rayCircle", b2RayCastCircle(&input, &circle));
    PrintCast("rayPolygon", b2RayCastPolygon(&input, &box));
    PrintCast("raySegment", b2RayCastSegment(&input, &segment, true));

    return 0;
}
