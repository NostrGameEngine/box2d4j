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
