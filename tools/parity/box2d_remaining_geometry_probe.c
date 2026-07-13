// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdint.h>
#include <stdio.h>
#include <string.h>

static uint32_t bits(float value)
{
    uint32_t output;
    memcpy(&output, &value, sizeof(output));
    return output;
}

static void print_polygon(const b2Polygon* polygon)
{
    printf(" %d %08x", polygon->count, bits(polygon->radius));
    for (int i = 0; i < polygon->count; ++i)
    {
        printf(" %08x %08x %08x %08x", bits(polygon->vertices[i].x), bits(polygon->vertices[i].y),
               bits(polygon->normals[i].x), bits(polygon->normals[i].y));
    }
    printf(" %08x %08x", bits(polygon->centroid.x), bits(polygon->centroid.y));
}

static uint32_t random_state = 0x243f6a88u;

static uint32_t next_random(void)
{
    random_state = random_state * 1664525u + 1013904223u;
    return random_state;
}

static float random_coordinate(void)
{
    int value = (int)((next_random() >> 16) & 2047u) - 1024;
    return (float)value / 128.0f;
}

static void print_random_hulls(void)
{
    for (int case_index = 0; case_index < 512; ++case_index)
    {
        int count = 3 + (int)(next_random() % 6u);
        b2Vec2 points[B2_MAX_POLYGON_VERTICES];
        for (int i = 0; i < count; ++i)
        {
            points[i] = (b2Vec2){random_coordinate(), random_coordinate()};
        }
        if (case_index % 5 == 0 && count > 3)
        {
            points[count - 1] = points[0];
        }
        if (case_index % 7 == 0)
        {
            for (int i = 0; i < count; ++i)
            {
                points[i].y = 0.5f * points[i].x + 0.25f;
            }
        }

        b2Hull hull = b2ComputeHull(points, count);
        bool valid = hull.count > 0 && b2ValidateHull(&hull);
        printf("fuzzHull %d", count);
        for (int i = 0; i < count; ++i)
        {
            printf(" %08x %08x", bits(points[i].x), bits(points[i].y));
        }
        printf(" %d %d", hull.count, valid ? 1 : 0);
        for (int i = 0; i < hull.count; ++i)
        {
            printf(" %08x %08x", bits(hull.points[i].x), bits(hull.points[i].y));
        }
        putchar('\n');
    }
}

int main(void)
{
    b2Vec2 points[] = {{-1.25f, -0.75f}, {2.0f, -0.4f}, {1.4f, 1.8f}, {-0.8f, 1.2f}};
    b2Hull hull = b2ComputeHull(points, 4);
    b2Vec2 position = {3.25f, -2.75f};
    b2Rot rotation = b2MakeRot(0.37f);
    b2Polygon plain = b2MakeOffsetPolygon(&hull, position, rotation);
    b2Polygon rounded = b2MakeOffsetRoundedPolygon(&hull, position, rotation, 0.23f);

    b2Capsule capsule = {{-1.1f, 0.35f}, {2.2f, -0.45f}, 0.6f};
    b2Transform transform = {{-0.7f, 1.9f}, b2MakeRot(-0.28f)};
    b2AABB aabb = b2ComputeCapsuleAABB(&capsule, transform);

    b2Hull validHull = {{{-1.0f, -1.0f}, {1.0f, -1.0f}, {1.0f, 1.0f}, {-1.0f, 1.0f}}, 4};
    b2Hull nearlyCollinearHull = {
        {{-1.0f, -1.0f}, {0.0f, -1.001f}, {1.0f, -1.0f}, {1.0f, 1.0f}, {-1.0f, 1.0f}}, 5};

    printf("remainingGeometry");
    print_polygon(&plain);
    print_polygon(&rounded);
    printf(" %08x %08x %08x %08x %d %d %d %d %d\n", bits(aabb.lowerBound.x), bits(aabb.lowerBound.y),
           bits(aabb.upperBound.x), bits(aabb.upperBound.y),
           b2PointInCapsule((b2Vec2){0.3f, 0.1f}, &capsule),
           b2PointInCapsule((b2Vec2){2.6f, -0.45f}, &capsule),
           b2PointInCapsule((b2Vec2){3.0f, 0.5f}, &capsule),
           b2ValidateHull(&validHull), b2ValidateHull(&nearlyCollinearHull));
    print_random_hulls();
    return 0;
}
