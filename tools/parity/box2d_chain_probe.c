// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>

static void print_chain_segment(const char* label, b2ShapeId shapeId)
{
    b2ChainSegment segment = b2Shape_GetChainSegment(shapeId);
    b2ChainId parent = b2Shape_GetParentChain(shapeId);
    printf("%s %d %d %d %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g\n", label, shapeId.index1, b2Shape_GetType(shapeId),
           parent.index1, segment.ghost1.x, segment.ghost1.y, segment.segment.point1.x, segment.segment.point1.y,
           segment.segment.point2.x, segment.segment.point2.y, segment.ghost2.x, segment.ghost2.y);
}

static bool overlap_callback(b2ShapeId shapeId, void* context)
{
    int* count = context;
    *count += 1;
    printf("overlapHit %d\n", shapeId.index1);
    return true;
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    b2Vec2 points[5] = { { -2.0f, 0.0f }, { -1.0f, 0.0f }, { 0.0f, 0.5f }, { 1.0f, 0.0f }, { 2.0f, 0.0f } };
    b2SurfaceMaterial materials[5] = { b2DefaultSurfaceMaterial(), b2DefaultSurfaceMaterial(), b2DefaultSurfaceMaterial(),
                                       b2DefaultSurfaceMaterial(), b2DefaultSurfaceMaterial() };
    materials[1].friction = 0.11f;
    materials[1].restitution = 0.21f;
    materials[1].userMaterialId = 31;
    materials[2].friction = 0.12f;
    materials[2].restitution = 0.22f;
    materials[2].userMaterialId = 32;

    b2ChainDef chainDef = b2DefaultChainDef();
    chainDef.points = points;
    chainDef.count = 5;
    chainDef.materials = materials;
    chainDef.materialCount = 5;
    chainDef.filter.categoryBits = 0x0002u;
    chainDef.filter.maskBits = 0xFFFFu;
    b2ChainId chainId = b2CreateChain(bodyId, &chainDef);

    printf("chain %d %d %d %d %d\n", chainId.index1, chainId.world0, chainId.generation, b2Chain_IsValid(chainId),
           b2Chain_GetSegmentCount(chainId));
    b2ShapeId segments[4] = { 0 };
    int segmentCount = b2Chain_GetSegments(chainId, segments, 4);
    printf("segments %d %d %d\n", segmentCount, segments[0].index1, segments[1].index1);
    print_chain_segment("seg0", segments[0]);
    print_chain_segment("seg1", segments[1]);
    printf("material0 %.9g %.9g %d\n", b2Shape_GetFriction(segments[0]), b2Shape_GetRestitution(segments[0]),
           b2Shape_GetMaterial(segments[0]));
    printf("material1 %.9g %.9g %d\n", b2Shape_GetFriction(segments[1]), b2Shape_GetRestitution(segments[1]),
           b2Shape_GetMaterial(segments[1]));

    b2Chain_SetFriction(chainId, 0.7f);
    b2Chain_SetRestitution(chainId, 0.3f);
    b2Chain_SetMaterial(chainId, 77);
    printf("chainMaterial %.9g %.9g %d %.9g %.9g %d\n", b2Chain_GetFriction(chainId), b2Chain_GetRestitution(chainId),
           b2Chain_GetMaterial(chainId), b2Shape_GetFriction(segments[0]), b2Shape_GetRestitution(segments[1]),
           b2Shape_GetMaterial(segments[1]));

    int overlapCount = 0;
    b2QueryFilter filter = b2DefaultQueryFilter();
    filter.categoryBits = 0x0002u;
    filter.maskBits = 0x0002u;
    b2TreeStats stats = b2World_OverlapAABB(worldId, (b2AABB){ { -1.5f, -0.2f }, { 0.5f, 0.7f } }, filter, overlap_callback,
                                            &overlapCount);
    printf("overlap %d stats %d %d\n", overlapCount, stats.nodeVisits, stats.leafVisits);
    b2RayResult ray = b2World_CastRayClosest(worldId, (b2Vec2){ -1.5f, 1.0f }, (b2Vec2){ 2.0f, -2.0f }, filter);
    printf("ray %d %d %.9g %.9g %.9g %.9g %.9g\n", ray.hit, ray.shapeId.index1, ray.point.x, ray.point.y, ray.normal.x,
           ray.normal.y, ray.fraction);

    b2Vec2 loopPoints[4] = { { -1.0f, -1.0f }, { 1.0f, -1.0f }, { 1.0f, 1.0f }, { -1.0f, 1.0f } };
    chainDef = b2DefaultChainDef();
    chainDef.points = loopPoints;
    chainDef.count = 4;
    chainDef.isLoop = true;
    b2ChainId loopId = b2CreateChain(bodyId, &chainDef);
    b2ShapeId loopSegments[4] = { 0 };
    printf("loop %d %d\n", loopId.index1, b2Chain_GetSegments(loopId, loopSegments, 4));
    print_chain_segment("loopLast", loopSegments[3]);

    b2DestroyChain(chainId);
    printf("destroy %d %d\n", b2Chain_IsValid(chainId), b2Shape_IsValid(segments[0]));

    b2DestroyWorld(worldId);
    return 0;
}
