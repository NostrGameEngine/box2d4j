// SPDX-License-Identifier: MIT

#include "box2d/collision.h"
#include "box2d/math_functions.h"

#include <stdio.h>

static void print_output(b2CastOutput output)
{
    printf(" %d %.9g %.9g %.9g %.9g %.9g %d", output.hit ? 1 : 0, output.fraction,
           output.point.x, output.point.y, output.normal.x, output.normal.y, output.iterations);
}

static b2CastOutput world_output(b2Transform transform, b2CastOutput localOutput)
{
    localOutput.point = b2TransformPoint(transform, localOutput.point);
    localOutput.normal = b2RotateVector(transform.q, localOutput.normal);
    return localOutput;
}

int main(void)
{
    b2Circle circle = {{0.0f, 0.0f}, 2.0f};
    b2Capsule capsule = {{-1.0f, 1.0f}, {1.0f, -1.0f}, 1.5f};
    b2Polygon box = b2MakeBox(2.0f, 2.0f);
    b2Vec2 vertices[3] = {{-2.0f, 0.0f}, {2.0f, 0.0f}, {2.0f, 3.0f}};
    b2Hull hull = b2ComputeHull(vertices, 3);
    b2Polygon triangle = b2MakePolygon(&hull, 0.0f);
    b2Segment segment = {{-3.0f, 0.0f}, {3.0f, 0.0f}};

    b2Transform baseTransform = b2Transform_identity;
    b2Vec2 rayStart = {0.0f, 30.0f};
    b2Vec2 rayEnd = {0.0f, 0.0f};
    b2Vec2 offset = {-20.0f, 20.0f};
    b2Vec2 increment = {10.0f, 0.0f};
    b2CastOutput outputs[5] = {0};
    b2CastOutput closest = {0};
    float maxFraction = 1.0f;

    for (int i = 0; i < 5; ++i)
    {
        b2Transform transform = {b2Add(baseTransform.p, offset), baseTransform.q};
        b2Vec2 start = b2InvTransformPoint(transform, rayStart);
        b2Vec2 translation = b2InvRotateVector(transform.q, b2Sub(rayEnd, rayStart));
        b2RayCastInput input = {start, translation, maxFraction};
        b2CastOutput localOutput;
        if (i == 0)
        {
            localOutput = b2RayCastCircle(&input, &circle);
        }
        else if (i == 1)
        {
            localOutput = b2RayCastCapsule(&input, &capsule);
        }
        else if (i == 2)
        {
            localOutput = b2RayCastPolygon(&input, &box);
        }
        else if (i == 3)
        {
            localOutput = b2RayCastPolygon(&input, &triangle);
        }
        else
        {
            localOutput = b2RayCastSegment(&input, &segment, false);
        }

        if (localOutput.hit)
        {
            closest = world_output(transform, localOutput);
            maxFraction = localOutput.fraction;
        }
        outputs[i] = localOutput;
        offset = b2Add(offset, increment);
    }

    printf("rayCast 5");
    for (int i = 0; i < 5; ++i)
    {
        print_output(outputs[i]);
    }
    print_output(closest);
    printf(" %.9g\n", maxFraction);
    return 0;
}
