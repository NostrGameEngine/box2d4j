// SPDX-License-Identifier: MIT

#include "box2d/collision.h"
#include "box2d/math_functions.h"

#include <stdio.h>

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

static void run_case(const b2ChainSegment* segments, int shapeType, b2Vec2 position,
                     float angle, float round)
{
    b2Transform transform2 = {position, b2MakeRot(angle)};
    printf(" %d %.9g %.9g %.9g %.9g 36", shapeType, position.x, position.y, angle, round);
    for (int i = 0; i < 36; ++i)
    {
        b2Manifold manifold;
        if (shapeType == 0)
        {
            b2Circle circle = {b2Vec2_zero, 0.5f};
            manifold = b2CollideChainSegmentAndCircle(segments + i, b2Transform_identity,
                                                       &circle, transform2);
        }
        else
        {
            float h = 0.5f - round;
            b2Polygon box = b2MakeRoundedBox(h, h, round);
            b2SimplexCache cache = {0};
            manifold = b2CollideChainSegmentAndPolygon(segments + i, b2Transform_identity,
                                                        &box, transform2, &cache);
        }
        print_manifold(manifold);
    }
}

int main(void)
{
    b2Vec2 points[36] = {
        {-20.58325f, 14.54175f}, {-21.90625f, 15.8645f}, {-24.552f, 17.1875f},
        {-27.198f, 11.89575f}, {-29.84375f, 15.8645f}, {-29.84375f, 21.15625f},
        {-25.875f, 23.802f}, {-20.58325f, 25.125f}, {-25.875f, 29.09375f},
        {-20.58325f, 31.7395f}, {-11.0089998f, 23.2290001f}, {-8.67700005f, 21.15625f},
        {-6.03125f, 21.15625f}, {-7.35424995f, 29.09375f}, {-3.38549995f, 29.09375f},
        {1.90625f, 30.41675f}, {5.875f, 17.1875f}, {11.16675f, 25.125f},
        {9.84375f, 29.09375f}, {13.8125f, 31.7395f}, {21.75f, 30.41675f},
        {28.3644981f, 26.448f}, {25.71875f, 18.5105f}, {24.3957481f, 13.21875f},
        {17.78125f, 11.89575f}, {15.1355f, 7.92700005f}, {5.875f, 9.25f},
        {1.90625f, 11.89575f}, {-3.25f, 11.89575f}, {-3.25f, 9.9375f},
        {-4.70825005f, 9.25f}, {-8.67700005f, 9.25f}, {-11.323f, 11.89575f},
        {-13.96875f, 11.89575f}, {-15.29175f, 14.54175f}, {-19.2605f, 14.54175f},
    };
    b2ChainSegment segments[36];
    for (int i = 0; i < 36; ++i)
    {
        int i0 = i > 0 ? i - 1 : 35;
        int i2 = i < 35 ? i + 1 : 0;
        int i3 = i2 < 35 ? i2 + 1 : 0;
        segments[i] = (b2ChainSegment){points[i0], {points[i], points[i2]}, points[i3], -1};
    }

    printf("smoothManifold 3");
    run_case(segments, 1, (b2Vec2){0.0f, 20.0f}, 0.0f, 0.0f);
    run_case(segments, 1, (b2Vec2){-7.35f, 20.8f}, 0.2f, 0.1f);
    run_case(segments, 0, (b2Vec2){-7.35f, 20.8f}, 0.0f, 0.0f);
    printf("\n");
    return 0;
}
