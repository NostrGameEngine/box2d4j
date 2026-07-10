// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdint.h>
#include <stdio.h>

enum
{
    BODY_COUNT = 10,
    IGNORE_INDEX = 7,
    MAX_DOOMED = 16,
};

typedef struct BodyUserData
{
    int index;
    bool ignore;
} BodyUserData;

typedef struct QueryContext
{
    int indices[MAX_DOOMED];
    b2ShapeId shapeIds[MAX_DOOMED];
    int count;
} QueryContext;

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

static bool overlap_callback(b2ShapeId shapeId, void* context)
{
    QueryContext* query = context;
    BodyUserData* data = b2Shape_GetUserData(shapeId);
    if (data != NULL && data->ignore)
    {
        return true;
    }
    if (query->count < MAX_DOOMED)
    {
        int i = query->count++;
        query->indices[i] = data == NULL ? -1 : data->index;
        query->shapeIds[i] = shapeId;
    }
    return true;
}

static void run_variant(int shapeType, b2Vec2 queryPosition, float queryAngle)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    uint32_t seed = 12345;
    b2BodyId bodyIds[BODY_COUNT];
    BodyUserData userData[BODY_COUNT];
    b2Transform bodyTransforms[BODY_COUNT];

    b2Vec2 vertices[3] = {{-0.5f, 0.0f}, {0.5f, 0.0f}, {0.0f, 1.5f}};
    b2Hull hull = b2ComputeHull(vertices, 3);
    b2Polygon polygon = b2MakePolygon(&hull, 0.0f);
    for (int i = 0; i < BODY_COUNT; ++i)
    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.position = (b2Vec2){random_range(&seed, -20.0f, 20.0f), random_range(&seed, 0.0f, 20.0f)};
        bodyDef.rotation = b2MakeRot(random_range(&seed, -B2_PI, B2_PI));
        bodyIds[i] = b2CreateBody(worldId, &bodyDef);
        userData[i] = (BodyUserData){i, i == IGNORE_INDEX};
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.userData = userData + i;
        b2CreatePolygonShape(bodyIds[i], &shapeDef, &polygon);
    }

    b2World_Step(worldId, 1.0f / 60.0f, 4);
    for (int i = 0; i < BODY_COUNT; ++i)
    {
        bodyTransforms[i] = b2Body_GetTransform(bodyIds[i]);
    }
    b2Counters before = b2World_GetCounters(worldId);

    b2Transform queryTransform = {queryPosition, b2MakeRot(queryAngle)};
    b2ShapeProxy proxy;
    if (shapeType == 0)
    {
        proxy = b2MakeProxy(&queryTransform.p, 1, 1.0f);
    }
    else if (shapeType == 1)
    {
        b2Vec2 capsulePoints[2] = {
            b2TransformPoint(queryTransform, (b2Vec2){-1.0f, 0.0f}),
            b2TransformPoint(queryTransform, (b2Vec2){1.0f, 0.0f}),
        };
        proxy = b2MakeProxy(capsulePoints, 2, 0.5f);
    }
    else
    {
        b2Polygon box = b2MakeOffsetBox(2.0f, 0.5f, queryTransform.p, queryTransform.q);
        proxy = b2MakeProxy(box.vertices, box.count, box.radius);
    }

    QueryContext query = {0};
    b2TreeStats stats = b2World_OverlapShape(worldId, &proxy, b2DefaultQueryFilter(), overlap_callback, &query);
    for (int i = 0; i < query.count; ++i)
    {
        BodyUserData* data = b2Shape_GetUserData(query.shapeIds[i]);
        if (data != NULL)
        {
            b2DestroyBody(bodyIds[data->index]);
        }
    }
    b2Counters after = b2World_GetCounters(worldId);

    printf(" %d %.9g %.9g %.9g %d %d %d %d %d %d %d", shapeType, queryPosition.x,
           queryPosition.y, queryAngle, before.bodyCount, before.shapeCount,
           after.bodyCount, after.shapeCount, stats.nodeVisits, stats.leafVisits, query.count);
    for (int i = 0; i < query.count; ++i)
    {
        printf(" %d", query.indices[i]);
    }
    printf(" %d", BODY_COUNT);
    for (int i = 0; i < BODY_COUNT; ++i)
    {
        b2Transform transform = bodyTransforms[i];
        printf(" %.9g %.9g %.9g %.9g %d", transform.p.x, transform.p.y, transform.q.c,
               transform.q.s, b2Body_IsValid(bodyIds[i]) ? 1 : 0);
    }
    b2DestroyWorld(worldId);
}

int main(void)
{
    printf("overlapWorld 4");
    run_variant(0, (b2Vec2){0.0f, 10.0f}, 0.0f);
    run_variant(0, (b2Vec2){0.6f, 6.5f}, 0.0f);
    run_variant(1, (b2Vec2){10.5f, 9.9f}, 0.25f);
    run_variant(2, (b2Vec2){8.1f, 15.8f}, -0.3f);
    printf("\n");
    return 0;
}
