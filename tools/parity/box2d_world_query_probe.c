// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>

typedef struct QueryContext
{
    int count;
    int ids[8];
    float fractions[8];
} QueryContext;

static bool overlap_callback(b2ShapeId shapeId, void* context)
{
    QueryContext* ctx = context;
    ctx->ids[ctx->count++] = shapeId.index1;
    return true;
}

static float cast_callback(b2ShapeId shapeId, b2Vec2 point, b2Vec2 normal, float fraction, void* context)
{
    QueryContext* ctx = context;
    int index = ctx->count++;
    ctx->ids[index] = shapeId.index1;
    ctx->fractions[index] = fraction;
    printf("castHit %d %.9g %.9g %.9g %.9g %.9g\n", shapeId.index1, point.x, point.y, normal.x, normal.y, fraction);
    return fraction;
}

static void print_query(const char* label, QueryContext* ctx, b2TreeStats stats)
{
    printf("%s %d", label, ctx->count);
    for (int i = 0; i < ctx->count; ++i)
    {
        printf(" %d", ctx->ids[i]);
    }
    printf(" stats %d %d\n", stats.nodeVisits, stats.leafVisits);
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.filter.categoryBits = 0x0002u;
    shapeDef.filter.maskBits = 0xFFFFu;

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.position = (b2Vec2){ -2.0f, 0.0f };
    b2BodyId circleBody = b2CreateBody(worldId, &bodyDef);
    b2Circle circle = { { 0.0f, 0.0f }, 0.5f };
    b2ShapeId circleId = b2CreateCircleShape(circleBody, &shapeDef, &circle);

    bodyDef.position = (b2Vec2){ 1.0f, 0.0f };
    b2BodyId boxBody = b2CreateBody(worldId, &bodyDef);
    b2Polygon box = b2MakeBox(0.5f, 0.5f);
    b2ShapeId boxId = b2CreatePolygonShape(boxBody, &shapeDef, &box);

    shapeDef.filter.categoryBits = 0x0004u;
    bodyDef.position = (b2Vec2){ 3.0f, 0.0f };
    b2BodyId capsuleBody = b2CreateBody(worldId, &bodyDef);
    b2Capsule capsule = { { -0.25f, 0.0f }, { 0.25f, 0.0f }, 0.25f };
    b2ShapeId capsuleId = b2CreateCapsuleShape(capsuleBody, &shapeDef, &capsule);
    printf("ids %d %d %d\n", circleId.index1, boxId.index1, capsuleId.index1);

    b2QueryFilter filter = b2DefaultQueryFilter();
    filter.categoryBits = 0x0002u;
    filter.maskBits = 0x0002u;

    QueryContext ctx = { 0 };
    b2TreeStats stats = b2World_OverlapAABB(worldId, (b2AABB){ { -3.0f, -1.0f }, { 4.0f, 1.0f } }, filter, overlap_callback, &ctx);
    print_query("overlapAABB", &ctx, stats);

    b2ShapeProxy proxy = b2MakeProxy((b2Vec2[]){ { -0.25f, -0.25f }, { 1.25f, -0.25f }, { 1.25f, 0.25f }, { -0.25f, 0.25f } }, 4, 0.0f);
    ctx = (QueryContext){ 0 };
    stats = b2World_OverlapShape(worldId, &proxy, filter, overlap_callback, &ctx);
    print_query("overlapShape", &ctx, stats);

    b2RayResult ray = b2World_CastRayClosest(worldId, (b2Vec2){ -4.0f, 0.0f }, (b2Vec2){ 8.0f, 0.0f }, filter);
    printf("closest %d %d %.9g %.9g %.9g %.9g %.9g stats %d %d\n", ray.hit, ray.shapeId.index1, ray.point.x, ray.point.y,
           ray.normal.x, ray.normal.y, ray.fraction, ray.nodeVisits, ray.leafVisits);

    ctx = (QueryContext){ 0 };
    stats = b2World_CastRay(worldId, (b2Vec2){ -4.0f, 0.0f }, (b2Vec2){ 8.0f, 0.0f }, filter, cast_callback, &ctx);
    print_query("castRay", &ctx, stats);

    b2ShapeProxy castProxy = b2MakeProxy((b2Vec2[]){ { -4.0f, 0.0f } }, 1, 0.1f);
    ctx = (QueryContext){ 0 };
    stats = b2World_CastShape(worldId, &castProxy, (b2Vec2){ 8.0f, 0.0f }, filter, cast_callback, &ctx);
    print_query("castShape", &ctx, stats);

    b2DestroyWorld(worldId);
    return 0;
}
