// SPDX-License-Identifier: MIT

#include "box2d/collision.h"
#include "box2d/types.h"

#include <inttypes.h>
#include <stdio.h>
#include <stdlib.h>

typedef struct HitContext
{
    uint64_t values[16];
    int count;
} HitContext;

static int compare_u64(const void* a, const void* b)
{
    uint64_t va = *(const uint64_t*)a;
    uint64_t vb = *(const uint64_t*)b;
    return (va > vb) - (va < vb);
}

static void add_hit(HitContext* context, uint64_t userData)
{
    context->values[context->count++] = userData;
}

static bool query_callback(int proxyId, uint64_t userData, void* context)
{
    (void)proxyId;
    add_hit((HitContext*)context, userData);
    return true;
}

static float ray_callback(const b2RayCastInput* input, int proxyId, uint64_t userData, void* context)
{
    (void)proxyId;
    add_hit((HitContext*)context, userData);
    return input->maxFraction;
}

static float shape_callback(const b2ShapeCastInput* input, int proxyId, uint64_t userData, void* context)
{
    (void)proxyId;
    add_hit((HitContext*)context, userData);
    return input->maxFraction;
}

static void print_hits(const char* label, HitContext* context, b2TreeStats stats)
{
    qsort(context->values, context->count, sizeof(uint64_t), compare_u64);
    printf("%s %d %d %d", label, context->count, stats.nodeVisits, stats.leafVisits);
    for (int i = 0; i < context->count; ++i)
    {
        printf(" %" PRIu64, context->values[i]);
    }
    printf("\n");
}

typedef struct ReuseContext
{
    uint64_t hash;
    int count;
} ReuseContext;

static bool reuse_callback(int proxyId, uint64_t userData, void* context)
{
    ReuseContext* result = context;
    result->hash = (result->hash ^ (uint32_t)proxyId) * UINT64_C(0x100000001b3);
    result->hash = (result->hash ^ (uint32_t)userData) * UINT64_C(0x100000001b3);
    result->count += 1;
    return true;
}

int main(void)
{
    b2DynamicTree tree = b2DynamicTree_Create();

    int isolated = b2DynamicTree_CreateProxy(&tree, (b2AABB){{0.0f, 0.0f}, {1.0f, 1.0f}}, 0x1u, UINT64_MAX);
    b2AABB isolatedBounds = b2DynamicTree_GetAABB(&tree, isolated);
    if (isolatedBounds.lowerBound.x != 0.0f || isolatedBounds.upperBound.y != 1.0f)
    {
        return 2;
    }
    b2DynamicTree_SetCategoryBits(&tree, isolated, 0x8u);
    if (b2DynamicTree_GetCategoryBits(&tree, isolated) != 0x8u)
    {
        return 3;
    }
    b2DynamicTree_ValidateNoEnlarged(&tree);
    b2DynamicTree_Destroy(&tree);

    tree = b2DynamicTree_Create();

    int a = b2DynamicTree_CreateProxy(&tree, (b2AABB){{0.0f, 0.0f}, {1.0f, 1.0f}}, 0x1u, 101u);
    int b = b2DynamicTree_CreateProxy(&tree, (b2AABB){{2.0f, 0.0f}, {3.0f, 1.0f}}, 0x2u, 202u);
    int c = b2DynamicTree_CreateProxy(&tree, (b2AABB){{0.5f, 0.5f}, {1.5f, 1.5f}}, 0x1u, 303u);
    int d = b2DynamicTree_CreateProxy(&tree, (b2AABB){{-2.0f, -1.0f}, {-1.0f, 1.0f}}, 0x4u, 404u);

    b2DynamicTree_MoveProxy(&tree, d, (b2AABB){{1.2f, -0.5f}, {1.8f, 0.5f}});
    b2DynamicTree_EnlargeProxy(&tree, a, (b2AABB){{-0.2f, -0.2f}, {1.2f, 1.2f}});

    b2AABB root = b2DynamicTree_GetRootBounds(&tree);
    printf("summary %d %" PRIu64 " %" PRIu64 " %.9g %.9g %.9g %.9g\n",
           b2DynamicTree_GetProxyCount(&tree),
           b2DynamicTree_GetCategoryBits(&tree, b),
           b2DynamicTree_GetUserData(&tree, c),
           root.lowerBound.x, root.lowerBound.y, root.upperBound.x, root.upperBound.y);

    HitContext query = {0};
    b2TreeStats queryStats =
        b2DynamicTree_Query(&tree, (b2AABB){{0.75f, -0.25f}, {2.5f, 1.25f}}, 0x7u, query_callback, &query);
    print_hits("query", &query, queryStats);

    HitContext ray = {0};
    b2RayCastInput rayInput = {{-3.0f, 0.25f}, {6.0f, 0.0f}, 1.0f};
    b2TreeStats rayStats = b2DynamicTree_RayCast(&tree, &rayInput, B2_DEFAULT_MASK_BITS, ray_callback, &ray);
    print_hits("ray", &ray, rayStats);

    HitContext shape = {0};
    b2Vec2 points[] = {{-2.5f, -0.25f}, {-2.5f, 0.25f}};
    b2ShapeCastInput shapeInput = {0};
    shapeInput.proxy = b2MakeProxy(points, 2, 0.1f);
    shapeInput.translation = (b2Vec2){5.0f, 0.0f};
    shapeInput.maxFraction = 1.0f;
    b2TreeStats shapeStats = b2DynamicTree_ShapeCast(&tree, &shapeInput, B2_DEFAULT_MASK_BITS, shape_callback, &shape);
    print_hits("shape", &shape, shapeStats);

    int partial = b2DynamicTree_Rebuild(&tree, false);
    b2AABB partialRoot = b2DynamicTree_GetRootBounds(&tree);
    printf("partial %d %d %.9g %.9g %.9g %.9g %.9g\n",
           partial,
           b2DynamicTree_GetHeight(&tree),
           b2DynamicTree_GetAreaRatio(&tree),
           partialRoot.lowerBound.x, partialRoot.lowerBound.y, partialRoot.upperBound.x, partialRoot.upperBound.y);

    int full = b2DynamicTree_Rebuild(&tree, true);
    b2AABB fullRoot = b2DynamicTree_GetRootBounds(&tree);
    printf("full %d %d %.9g %.9g %.9g %.9g %.9g\n",
           full,
           b2DynamicTree_GetHeight(&tree),
           b2DynamicTree_GetAreaRatio(&tree),
           fullRoot.lowerBound.x, fullRoot.lowerBound.y, fullRoot.upperBound.x, fullRoot.upperBound.y);

    HitContext post = {0};
    b2TreeStats postStats =
        b2DynamicTree_Query(&tree, (b2AABB){{0.75f, -0.25f}, {2.5f, 1.25f}}, 0x7u, query_callback, &post);
    print_hits("post", &post, postStats);

    b2DynamicTree_Destroy(&tree);

    tree = b2DynamicTree_Create();
    int proxyIds[820];
    int index = 0;
    for (int i = 0; i < 40; ++i)
    {
        float y = i + 1.5f;
        for (int j = i; j < 40; ++j)
        {
            float x = 0.5f * i + (j - i) - 20.0f;
            b2AABB box = {{x - 0.6f, y - 0.6f}, {x + 0.6f, y + 0.6f}};
            proxyIds[index] = b2DynamicTree_CreateProxy(&tree, box, B2_DEFAULT_CATEGORY_BITS, (uint64_t)(index + 1));
            index += 1;
        }
    }
    for (int i = 0; i < 820; ++i)
    {
        b2DynamicTree_DestroyProxy(&tree, proxyIds[i]);
    }
    index = 0;
    int firstProxy = -1;
    int lastProxy = -1;
    for (int i = 0; i < 40; ++i)
    {
        float y = i + 1.5f;
        for (int j = i; j < 40; ++j)
        {
            float x = 0.5f * i + (j - i) - 20.0f;
            b2AABB box = {{x - 0.6f, y - 0.6f}, {x + 0.6f, y + 0.6f}};
            int proxyId = b2DynamicTree_CreateProxy(&tree, box, B2_DEFAULT_CATEGORY_BITS, (uint64_t)(820 - index));
            if (index == 0)
            {
                firstProxy = proxyId;
            }
            lastProxy = proxyId;
            index += 1;
        }
    }
    ReuseContext reuse = {UINT64_C(0xcbf29ce484222325), 0};
    b2TreeStats reuseStats = b2DynamicTree_Query(&tree, (b2AABB){{-100.0f, -100.0f}, {100.0f, 100.0f}},
                                                 B2_DEFAULT_MASK_BITS, reuse_callback, &reuse);
    printf("reuse %d %d %d %d %d %d %d %" PRIu64 "\n", firstProxy, lastProxy, tree.root, tree.freeList,
           tree.nodeCount, reuseStats.nodeVisits, reuse.count, reuse.hash);
    b2DynamicTree_Destroy(&tree);
    return 0;
}
