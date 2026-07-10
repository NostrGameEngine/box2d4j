// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"
#include "random.h"

#include <stdio.h>

int main(void)
{
    g_randomSeed = RAND_SEED;

    b2Vec2 points[B2_MAX_POLYGON_VERTICES];
    float angle = B2_PI * RandomFloat();
    b2Rot r = b2MakeRot(angle);

    b2Vec2 lowerBound = {-4.0f, -4.0f};
    b2Vec2 upperBound = {4.0f, 4.0f};

    for (int i = 0; i < B2_MAX_POLYGON_VERTICES; ++i)
    {
        float x = 10.0f * RandomFloat();
        float y = 10.0f * RandomFloat();
        b2Vec2 v = b2Clamp((b2Vec2){x, y}, lowerBound, upperBound);
        points[i] = b2RotateVector(r, v);
    }

    b2Hull hull = b2ComputeHull(points, B2_MAX_POLYGON_VERTICES);
    bool valid = hull.count > 0 && b2ValidateHull(&hull);

    printf("convexHull 1 %.9g %d %d", angle, valid ? 1 : 0, hull.count);
    for (int i = 0; i < B2_MAX_POLYGON_VERTICES; ++i)
    {
        printf(" %.9g %.9g", points[i].x, points[i].y);
    }
    for (int i = 0; i < hull.count; ++i)
    {
        printf(" %.9g %.9g", hull.points[i].x, hull.points[i].y);
    }
    printf("\n");

    return 0;
}
