// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/collision.h"
#include "box2d/math_functions.h"

#include <stdio.h>

typedef enum ShapeType
{
    e_point,
    e_segment,
    e_triangle,
    e_box
} ShapeType;

static b2Vec2 point;
static b2Segment segment;
static b2Polygon triangle;
static b2Polygon box;

static b2ShapeProxy make_proxy(ShapeType type, float radius)
{
    b2ShapeProxy proxy = {0};
    proxy.radius = radius;

    switch (type)
    {
    case e_point:
        proxy.points[0] = point;
        proxy.count = 1;
        break;

    case e_segment:
        proxy.points[0] = segment.point1;
        proxy.points[1] = segment.point2;
        proxy.count = 2;
        break;

    case e_triangle:
        for (int i = 0; i < triangle.count; ++i)
        {
            proxy.points[i] = triangle.vertices[i];
        }
        proxy.count = triangle.count;
        break;

    case e_box:
        for (int i = 0; i < box.count; ++i)
        {
            proxy.points[i] = box.vertices[i];
        }
        proxy.count = box.count;
        break;
    }

    return proxy;
}

static void init_shapes(void)
{
    point = b2Vec2_zero;
    segment = (b2Segment){{-0.5f, 0.0f}, {0.5f, 0.0f}};

    b2Vec2 points[3] = {{-0.5f, 0.0f}, {0.5f, 0.0f}, {0.0f, 1.0f}};
    b2Hull hull = b2ComputeHull(points, 3);
    triangle = b2MakePolygon(&hull, 0.0f);

    box = b2MakeSquare(0.5f);
}

static void print_vec(b2Vec2 v)
{
    printf(" %.9g %.9g", v.x, v.y);
}

static void run_case(const char* label, ShapeType typeA, ShapeType typeB, float radiusA, float radiusB, b2Vec2 position, float angle)
{
    b2DistanceInput input = {0};
    input.proxyA = make_proxy(typeA, radiusA);
    input.proxyB = make_proxy(typeB, radiusB);
    input.transformA = b2Transform_identity;
    input.transformB = (b2Transform){position, b2MakeRot(angle)};
    input.useRadii = true;

    b2SimplexCache cache = b2_emptySimplexCache;
    b2Simplex simplexes[20];
    b2DistanceOutput output = b2ShapeDistance(&input, &cache, simplexes, 20);

    printf(" %s %d %d %.9g %.9g %.9g %.9g %.9g %d %d %d",
           label,
           (int)typeA,
           (int)typeB,
           radiusA,
           radiusB,
           position.x,
           position.y,
           angle,
           input.proxyA.count,
           input.proxyB.count,
           (int)cache.count);
    printf(" %u %u %u %u %u %u",
           cache.indexA[0],
           cache.indexA[1],
           cache.indexA[2],
           cache.indexB[0],
           cache.indexB[1],
           cache.indexB[2]);
    printf(" %.9g", output.distance);
    print_vec(output.normal);
    print_vec(output.pointA);
    print_vec(output.pointB);
    printf(" %d %d", output.iterations, output.simplexCount);
}

int main(void)
{
    init_shapes();
    printf("shapeDistance 2");
    run_case("default", e_box, e_box, 0.0f, 0.0f, b2Vec2_zero, 0.0f);
    run_case("mixed", e_triangle, e_segment, 0.05f, 0.1f, (b2Vec2){0.75f, -0.25f}, 0.35f);
    printf("\n");
    return 0;
}
