// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <float.h>
#include <math.h>
#include <stdint.h>
#include <stdio.h>

enum
{
    ANY = 0,
    CLOSEST = 1,
    MULTIPLE = 2,
    SORTED = 3,
    SIMPLE = 4,
    RAY_CAST = 0,
    CIRCLE_CAST = 1,
    CAPSULE_CAST = 2,
    POLYGON_CAST = 3,
    CREATED_BODY_COUNT = 8,
    IGNORE_INDEX = 7,
};

typedef struct BodyUserData
{
    int index;
    bool ignore;
} BodyUserData;

typedef struct Scene
{
    b2WorldId worldId;
    b2BodyId bodyIds[CREATED_BODY_COUNT];
    BodyUserData userData[CREATED_BODY_COUNT];
} Scene;

typedef struct CastContext
{
    b2Vec2 points[3];
    b2Vec2 normals[3];
    float fractions[3];
    int shapeIndices[3];
    int count;
    int mode;
} CastContext;

static float random_range(uint32_t* seed, float lo, float hi)
{
    uint32_t x = *seed;
    x ^= x << 13;
    x ^= x >> 17;
    x ^= x << 5;
    *seed = x;
    float r = (float)(x & 32767u) / 32767.0f;
    return (hi - lo) * r + lo;
}

static void context_set(CastContext* context, int index, b2Vec2 point, b2Vec2 normal,
                        float fraction, int shapeIndex)
{
    context->points[index] = point;
    context->normals[index] = normal;
    context->fractions[index] = fraction;
    context->shapeIndices[index] = shapeIndex;
}

static void context_copy(CastContext* context, int to, int from)
{
    context->points[to] = context->points[from];
    context->normals[to] = context->normals[from];
    context->fractions[to] = context->fractions[from];
    context->shapeIndices[to] = context->shapeIndices[from];
}

static float cast_callback(b2ShapeId shapeId, b2Vec2 point, b2Vec2 normal, float fraction, void* data)
{
    CastContext* context = data;
    BodyUserData* userData = b2Shape_GetUserData(shapeId);
    if ((userData != NULL && userData->ignore) || fraction == 0.0f)
    {
        return -1.0f;
    }
    int shapeIndex = userData == NULL ? -1 : userData->index;
    if (context->mode == ANY || context->mode == CLOSEST)
    {
        context_set(context, 0, point, normal, fraction, shapeIndex);
        context->count = 1;
        return context->mode == ANY ? 0.0f : fraction;
    }
    if (context->mode == MULTIPLE)
    {
        int index = context->count;
        context_set(context, index, point, normal, fraction, shapeIndex);
        context->count = index + 1;
        return context->count == 3 ? 0.0f : 1.0f;
    }

    int index = 3;
    while (fraction < context->fractions[index - 1])
    {
        index -= 1;
        if (index == 0)
        {
            break;
        }
    }
    if (index == 3)
    {
        return context->fractions[2];
    }
    for (int j = 2; j > index; --j)
    {
        context_copy(context, j, j - 1);
    }
    context_set(context, index, point, normal, fraction, shapeIndex);
    context->count = context->count < 3 ? context->count + 1 : 3;
    return context->count == 3 ? context->fractions[2] : 1.0f;
}

static Scene create_scene(void)
{
    Scene scene = {0};
    b2WorldDef worldDef = b2DefaultWorldDef();
    scene.worldId = b2CreateWorld(&worldDef);
    uint32_t seed = 12345;

    b2BodyDef groundDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(scene.worldId, &groundDef);
    b2ShapeDef groundShapeDef = b2DefaultShapeDef();
    b2Segment ground = {{-40.0f, 0.0f}, {40.0f, 0.0f}};
    b2CreateSegmentShape(groundId, &groundShapeDef, &ground);

    b2Polygon polygons[4];
    b2Vec2 vertices1[3] = {{-0.5f, 0.0f}, {0.5f, 0.0f}, {0.0f, 1.5f}};
    b2Hull hull = b2ComputeHull(vertices1, 3);
    polygons[0] = b2MakePolygon(&hull, 0.0f);
    b2Vec2 vertices2[3] = {{-0.1f, 0.0f}, {0.1f, 0.0f}, {0.0f, 1.5f}};
    hull = b2ComputeHull(vertices2, 3);
    polygons[1] = b2MakePolygon(&hull, 0.0f);
    polygons[1].radius = 0.5f;
    float w = 1.0f;
    float b = w / (2.0f + sqrtf(2.0f));
    float s = sqrtf(2.0f) * b;
    b2Vec2 vertices3[8] = {
        {0.5f * s, 0.0f}, {0.5f * w, b}, {0.5f * w, b + s}, {0.5f * s, w},
        {-0.5f * s, w}, {-0.5f * w, b + s}, {-0.5f * w, b}, {-0.5f * s, 0.0f},
    };
    hull = b2ComputeHull(vertices3, 8);
    polygons[2] = b2MakePolygon(&hull, 0.0f);
    polygons[3] = b2MakeBox(0.5f, 0.5f);
    b2Circle circle = {b2Vec2_zero, 0.5f};
    b2Capsule capsule = {{-0.5f, 0.0f}, {0.5f, 0.0f}, 0.25f};
    b2Segment segment = {{-1.0f, 0.0f}, {1.0f, 0.0f}};

    for (int i = 0; i < CREATED_BODY_COUNT; ++i)
    {
        int shapeIndex = i % 7;
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.position = (b2Vec2){random_range(&seed, -20.0f, 20.0f), random_range(&seed, 0.0f, 20.0f)};
        bodyDef.rotation = b2MakeRot(random_range(&seed, -B2_PI, B2_PI));
        int mod = i % 3;
        bodyDef.type = mod == 0 ? b2_staticBody : mod == 1 ? b2_kinematicBody : b2_dynamicBody;
        if (bodyDef.type == b2_dynamicBody)
        {
            bodyDef.gravityScale = 0.0f;
        }
        scene.bodyIds[i] = b2CreateBody(scene.worldId, &bodyDef);
        scene.userData[i] = (BodyUserData){i, i == IGNORE_INDEX};
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.userData = scene.userData + i;
        if (shapeIndex < 4)
        {
            b2CreatePolygonShape(scene.bodyIds[i], &shapeDef, polygons + shapeIndex);
        }
        else if (shapeIndex == 4)
        {
            b2CreateCircleShape(scene.bodyIds[i], &shapeDef, &circle);
        }
        else if (shapeIndex == 5)
        {
            b2CreateCapsuleShape(scene.bodyIds[i], &shapeDef, &capsule);
        }
        else
        {
            b2CreateSegmentShape(scene.bodyIds[i], &shapeDef, &segment);
        }
    }
    b2World_Step(scene.worldId, 1.0f / 60.0f, 4);
    return scene;
}

static void print_query_header(int castType, int mode, b2Vec2 start, b2Vec2 end,
                               float angle, float radius, b2TreeStats stats, int count)
{
    printf(" %d %d %.9g %.9g %.9g %.9g %.9g %.9g %d %d %d", castType, mode,
           start.x, start.y, end.x, end.y, angle, radius, stats.nodeVisits, stats.leafVisits, count);
}

static void print_hits(CastContext* context)
{
    for (int i = 0; i < context->count; ++i)
    {
        printf(" %.9g %.9g %.9g %.9g %.9g %d", context->points[i].x, context->points[i].y,
               context->normals[i].x, context->normals[i].y, context->fractions[i],
               context->shapeIndices[i]);
    }
}

static void perform_cast(b2WorldId worldId, int castType, int mode, b2Vec2 start,
                         b2Vec2 end, float angle, float radius)
{
    CastContext context = {0};
    context.mode = mode;
    context.fractions[0] = FLT_MAX;
    context.fractions[1] = FLT_MAX;
    context.fractions[2] = FLT_MAX;
    context.shapeIndices[0] = -1;
    context.shapeIndices[1] = -1;
    context.shapeIndices[2] = -1;
    b2Vec2 translation = b2Sub(end, start);
    b2TreeStats stats;
    if (castType == RAY_CAST)
    {
        stats = b2World_CastRay(worldId, start, translation, b2DefaultQueryFilter(), cast_callback, &context);
    }
    else
    {
        b2Transform transform = {start, b2MakeRot(angle)};
        b2ShapeProxy proxy;
        if (castType == CIRCLE_CAST)
        {
            proxy = b2MakeProxy(&start, 1, radius);
        }
        else if (castType == CAPSULE_CAST)
        {
            b2Vec2 points[2] = {
                b2TransformPoint(transform, (b2Vec2){-0.25f, 0.0f}),
                b2TransformPoint(transform, (b2Vec2){0.25f, 0.0f}),
            };
            proxy = b2MakeProxy(points, 2, radius);
        }
        else
        {
            b2Polygon box = b2MakeOffsetRoundedBox(0.25f, 0.5f, transform.p, transform.q, radius);
            proxy = b2MakeProxy(box.vertices, box.count, box.radius);
        }
        stats = b2World_CastShape(worldId, &proxy, translation, b2DefaultQueryFilter(), cast_callback, &context);
    }
    print_query_header(castType, mode, start, end, angle, radius, stats, context.count);
    print_hits(&context);
}

static void perform_simple_cast(b2WorldId worldId, b2Vec2 start, b2Vec2 end)
{
    b2RayResult result = b2World_CastRayClosest(worldId, start, b2Sub(end, start), b2DefaultQueryFilter());
    int count = result.hit ? 1 : 0;
    b2TreeStats stats = {result.nodeVisits, result.leafVisits};
    print_query_header(RAY_CAST, SIMPLE, start, end, 0.0f, 0.5f, stats, count);
    if (result.hit)
    {
        BodyUserData* data = b2Shape_GetUserData(result.shapeId);
        printf(" %.9g %.9g %.9g %.9g %.9g %d", result.point.x, result.point.y,
               result.normal.x, result.normal.y, result.fraction, data == NULL ? -1 : data->index);
    }
}

int main(void)
{
    Scene scene = create_scene();
    b2Counters counters = b2World_GetCounters(scene.worldId);
    b2Vec2 start = {-20.0f, 10.0f};
    b2Vec2 end = {20.0f, 10.0f};
    printf("castWorld %d %d %d %d %d 18", counters.bodyCount, counters.shapeCount,
           counters.contactCount, b2World_GetAwakeBodyCount(scene.worldId), IGNORE_INDEX);
    for (int castType = RAY_CAST; castType <= POLYGON_CAST; ++castType)
    {
        float angle = 0.2f * castType;
        for (int mode = ANY; mode <= SORTED; ++mode)
        {
            perform_cast(scene.worldId, castType, mode, start, end, angle, 0.5f);
        }
    }
    perform_simple_cast(scene.worldId, start, end);
    perform_cast(scene.worldId, RAY_CAST, SORTED,
                 (b2Vec2){-20.0f, 16.7f}, (b2Vec2){20.0f, 16.7f}, 0.0f, 0.5f);

    printf(" %d", CREATED_BODY_COUNT);
    for (int i = 0; i < CREATED_BODY_COUNT; ++i)
    {
        b2Transform transform = b2Body_GetTransform(scene.bodyIds[i]);
        printf(" %d %.9g %.9g %.9g %.9g", b2Body_GetType(scene.bodyIds[i]), transform.p.x,
               transform.p.y, transform.q.c, transform.q.s);
    }
    printf("\n");
    b2DestroyWorld(scene.worldId);
    return 0;
}
