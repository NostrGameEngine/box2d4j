// SPDX-License-Identifier: MIT

#include "aabb.h"

#include "box2d/id.h"
#include "box2d/math_functions.h"

#include <stdio.h>

int main(void)
{
    uint32_t a = 0x01234567;
    b2WorldId worldId = b2LoadWorldId(a);
    printf("world %u %u %u\n", worldId.index1, worldId.generation, b2StoreWorldId(worldId));

    uint64_t x = 0x0123456789ABCDEFull;
    b2BodyId bodyId = b2LoadBodyId(x);
    b2ShapeId shapeId = b2LoadShapeId(x);
    b2ChainId chainId = b2LoadChainId(x);
    b2JointId jointId = b2LoadJointId(x);

    printf("body %d %u %u %llu\n", bodyId.index1, bodyId.world0, bodyId.generation,
           (unsigned long long)b2StoreBodyId(bodyId));
    printf("shape %d %u %u %llu\n", shapeId.index1, shapeId.world0, shapeId.generation,
           (unsigned long long)b2StoreShapeId(shapeId));
    printf("chain %d %u %u %llu\n", chainId.index1, chainId.world0, chainId.generation,
           (unsigned long long)b2StoreChainId(chainId));
    printf("joint %d %u %u %llu\n", jointId.index1, jointId.world0, jointId.generation,
           (unsigned long long)b2StoreJointId(jointId));
    printf("idMacros %d %d %d %d %d %d %d %d %d %d\n",
           B2_IS_NULL(b2_nullWorldId), B2_IS_NULL(b2_nullBodyId), B2_IS_NULL(b2_nullShapeId),
           B2_IS_NULL(b2_nullChainId), B2_IS_NULL(b2_nullJointId), B2_IS_NON_NULL(worldId),
           B2_IS_NON_NULL(bodyId), B2_IS_NON_NULL(shapeId), B2_IS_NON_NULL(chainId), B2_IS_NON_NULL(jointId));
    printf("emptyCache %u %u %u %u %u %u %u\n", b2_emptySimplexCache.count,
           b2_emptySimplexCache.indexA[0], b2_emptySimplexCache.indexA[1], b2_emptySimplexCache.indexA[2],
           b2_emptySimplexCache.indexB[0], b2_emptySimplexCache.indexB[1], b2_emptySimplexCache.indexB[2]);

    b2AABB box;
    box.lowerBound = (b2Vec2){-1.0f, -1.0f};
    box.upperBound = (b2Vec2){-2.0f, -2.0f};
    printf("aabbInvalid %d\n", b2IsValidAABB(box));

    box.upperBound = (b2Vec2){1.0f, 1.0f};
    b2AABB other = {{2.0f, 2.0f}, {4.0f, 4.0f}};
    b2CastOutput output = b2AABB_RayCast(box, (b2Vec2){-2.0f, 0.0f}, (b2Vec2){2.0f, 0.0f});
    printf("aabb %d %d %d %d %.9g %.9g %.9g\n",
           b2IsValidAABB(box),
           b2AABB_Overlaps(box, other),
           b2AABB_Contains(box, other),
           output.hit,
           output.fraction,
           output.normal.x,
           output.normal.y);

    return 0;
}
