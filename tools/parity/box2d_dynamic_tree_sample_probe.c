// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <inttypes.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

enum
{
    INCREMENTAL = 0,
    FULL_REBUILD = 1,
    PARTIAL_REBUILD = 2,
    ROW_COUNT = 100,
    COLUMN_COUNT = 100,
    STEP_COUNT = 3,
};

typedef struct Proxy
{
    b2AABB box;
    b2AABB fatBox;
    b2Vec2 position;
    b2Vec2 width;
    int proxyId;
    bool moved;
} Proxy;

typedef struct HitContext
{
    int* hits;
    int count;
} HitContext;

static int random_int(uint32_t* seed)
{
    uint32_t x = *seed;
    x ^= x << 13;
    x ^= x >> 17;
    x ^= x << 5;
    *seed = x;
    return (int)(x & 32767u);
}

static float random_range(uint32_t* seed, float lo, float hi)
{
    float r = (float)random_int(seed) / 32767.0f;
    return (hi - lo) * r + lo;
}

static float random_signed(uint32_t* seed)
{
    return 2.0f * (float)random_int(seed) / 32767.0f - 1.0f;
}

static bool query_callback(int proxyId, uint64_t userData, void* context)
{
    (void)proxyId;
    HitContext* hits = context;
    hits->hits[hits->count++] = (int)userData;
    return true;
}

static float ray_callback(const b2RayCastInput* input, int proxyId, uint64_t userData, void* context)
{
    (void)proxyId;
    HitContext* hits = context;
    hits->hits[hits->count++] = (int)userData;
    return input->maxFraction;
}

static int compare_int(const void* a, const void* b)
{
    int ia = *(const int*)a;
    int ib = *(const int*)b;
    return (ia > ib) - (ia < ib);
}

static uint32_t float_bits(float value)
{
    uint32_t bits;
    memcpy(&bits, &value, sizeof(bits));
    return bits;
}

static uint64_t hash_word(uint64_t hash, uint32_t value)
{
    return (hash ^ value) * UINT64_C(1099511628211);
}

static uint64_t state_hash(const Proxy* proxies, int count)
{
    uint64_t hash = UINT64_C(1469598103934665603);
    for (int i = 0; i < count; ++i)
    {
        const Proxy* proxy = proxies + i;
        hash = hash_word(hash, float_bits(proxy->position.x));
        hash = hash_word(hash, float_bits(proxy->position.y));
        hash = hash_word(hash, float_bits(proxy->width.x));
        hash = hash_word(hash, float_bits(proxy->width.y));
        hash = hash_word(hash, float_bits(proxy->box.lowerBound.x));
        hash = hash_word(hash, float_bits(proxy->box.lowerBound.y));
        hash = hash_word(hash, float_bits(proxy->box.upperBound.x));
        hash = hash_word(hash, float_bits(proxy->box.upperBound.y));
        hash = hash_word(hash, float_bits(proxy->fatBox.lowerBound.x));
        hash = hash_word(hash, float_bits(proxy->fatBox.lowerBound.y));
        hash = hash_word(hash, float_bits(proxy->fatBox.upperBound.x));
        hash = hash_word(hash, float_bits(proxy->fatBox.upperBound.y));
        hash = hash_word(hash, (uint32_t)proxy->proxyId);
        hash = hash_word(hash, proxy->moved ? 1u : 0u);
    }
    return hash;
}

static void print_proxy(const Proxy* proxy)
{
    printf(" %d %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %d",
           proxy->proxyId, proxy->position.x, proxy->position.y, proxy->width.x, proxy->width.y,
           proxy->box.lowerBound.x, proxy->box.lowerBound.y, proxy->box.upperBound.x,
           proxy->box.upperBound.y, proxy->fatBox.lowerBound.x, proxy->fatBox.lowerBound.y,
           proxy->fatBox.upperBound.x, proxy->fatBox.upperBound.y, proxy->moved ? 1 : 0);
}

static void run_variant(int updateType)
{
    uint32_t seed = 12345;
    b2DynamicTree tree = b2DynamicTree_Create();
    Proxy* proxies = malloc(ROW_COUNT * COLUMN_COUNT * sizeof(Proxy));
    int proxyCount = 0;
    b2Vec2 margin = {0.1f, 0.1f};
    float y = -4.0f;
    for (int row = 0; row < ROW_COUNT; ++row)
    {
        float x = -40.0f;
        for (int column = 0; column < COLUMN_COUNT; ++column)
        {
            if (random_range(&seed, 0.0f, 1.0f) <= 0.25f)
            {
                Proxy* proxy = proxies + proxyCount;
                proxy->position = (b2Vec2){x, y};
                float ratio = random_range(&seed, 1.0f, 5.0f);
                float width = random_range(&seed, 0.1f, 0.5f);
                if (random_signed(&seed) > 0.0f)
                {
                    proxy->width = (b2Vec2){ratio * width, width};
                }
                else
                {
                    proxy->width = (b2Vec2){width, ratio * width};
                }
                proxy->box = (b2AABB){{x, y}, {x + proxy->width.x, y + proxy->width.y}};
                proxy->fatBox = (b2AABB){b2Sub(proxy->box.lowerBound, margin),
                                          b2Add(proxy->box.upperBound, margin)};
                proxy->proxyId = b2DynamicTree_CreateProxy(&tree, proxy->fatBox,
                                                            B2_DEFAULT_CATEGORY_BITS, (uint64_t)proxyCount);
                proxy->moved = false;
                proxyCount += 1;
            }
            x += 1.0f;
        }
        y += 1.0f;
    }

    int movedCounts[STEP_COUNT] = {0};
    int rebuildCounts[STEP_COUNT] = {0};
    for (int step = 0; step < STEP_COUNT; ++step)
    {
        for (int i = 0; i < proxyCount; ++i)
        {
            Proxy* proxy = proxies + i;
            if (0.05f > random_range(&seed, 0.0f, 1.0f))
            {
                float dx = 0.1f * random_signed(&seed);
                float dy = 0.1f * random_signed(&seed);
                proxy->position.x += dx;
                proxy->position.y += dy;
                proxy->box.lowerBound.x = proxy->position.x + dx;
                proxy->box.lowerBound.y = proxy->position.y + dy;
                proxy->box.upperBound.x = proxy->position.x + dx + proxy->width.x;
                proxy->box.upperBound.y = proxy->position.y + dy + proxy->width.y;
                if (b2AABB_Contains(proxy->fatBox, proxy->box) == false)
                {
                    proxy->fatBox.lowerBound = b2Sub(proxy->box.lowerBound, margin);
                    proxy->fatBox.upperBound = b2Add(proxy->box.upperBound, margin);
                    proxy->moved = true;
                    movedCounts[step] += 1;
                }
                else
                {
                    proxy->moved = false;
                }
            }
            else
            {
                proxy->moved = false;
            }
        }

        if (updateType == INCREMENTAL)
        {
            for (int i = 0; i < proxyCount; ++i)
            {
                if (proxies[i].moved)
                {
                    b2DynamicTree_MoveProxy(&tree, proxies[i].proxyId, proxies[i].fatBox);
                }
            }
        }
        else
        {
            for (int i = 0; i < proxyCount; ++i)
            {
                if (proxies[i].moved)
                {
                    b2DynamicTree_EnlargeProxy(&tree, proxies[i].proxyId, proxies[i].fatBox);
                }
            }
            rebuildCounts[step] = b2DynamicTree_Rebuild(&tree, updateType == FULL_REBUILD);
        }
        b2DynamicTree_Validate(&tree);
    }

    int* queryValues = malloc(proxyCount * sizeof(int));
    HitContext queryHits = {queryValues, 0};
    b2AABB queryBox = {{0.0f, 0.0f}, {25.0f, 25.0f}};
    b2TreeStats queryStats = b2DynamicTree_Query(&tree, queryBox, B2_DEFAULT_MASK_BITS,
                                                 query_callback, &queryHits);
    qsort(queryHits.hits, queryHits.count, sizeof(int), compare_int);

    int* rayValues = malloc(proxyCount * sizeof(int));
    HitContext rayHits = {rayValues, 0};
    b2RayCastInput rayInput = {{-40.0f, 10.0f}, {100.0f, 0.0f}, 1.0f};
    b2TreeStats rayStats = b2DynamicTree_RayCast(&tree, &rayInput, B2_DEFAULT_MASK_BITS,
                                                 ray_callback, &rayHits);
    qsort(rayHits.hits, rayHits.count, sizeof(int), compare_int);

    b2AABB root = b2DynamicTree_GetRootBounds(&tree);
    printf(" %d %d %d", updateType, proxyCount, STEP_COUNT);
    for (int i = 0; i < STEP_COUNT; ++i)
    {
        printf(" %d %d", movedCounts[i], rebuildCounts[i]);
    }
    printf(" %d %.9g %d %.9g %.9g %.9g %.9g %" PRIu64 " %d %d %d",
           b2DynamicTree_GetHeight(&tree), b2DynamicTree_GetAreaRatio(&tree),
           b2DynamicTree_GetByteCount(&tree), root.lowerBound.x, root.lowerBound.y,
           root.upperBound.x, root.upperBound.y, state_hash(proxies, proxyCount),
           queryStats.nodeVisits, queryStats.leafVisits, queryHits.count);
    for (int i = 0; i < queryHits.count; ++i)
    {
        printf(" %d", queryHits.hits[i]);
    }
    printf(" %d %d %d", rayStats.nodeVisits, rayStats.leafVisits, rayHits.count);
    for (int i = 0; i < rayHits.count; ++i)
    {
        printf(" %d", rayHits.hits[i]);
    }
    printf(" 3");
    print_proxy(proxies);
    print_proxy(proxies + proxyCount / 2);
    print_proxy(proxies + proxyCount - 1);

    free(queryValues);
    free(rayValues);
    free(proxies);
    b2DynamicTree_Destroy(&tree);
}

int main(void)
{
    printf("dynamicTree 3");
    run_variant(INCREMENTAL);
    run_variant(FULL_REBUILD);
    run_variant(PARTIAL_REBUILD);
    printf("\n");
    return 0;
}
