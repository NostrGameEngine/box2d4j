// SPDX-License-Identifier: MIT

#include "broad_phase.h"

#include <stdio.h>

static void print_move_array(const b2BroadPhase* bp)
{
    printf("moves %d %d", bp->moveArray.count, (int)bp->moveSet.count);
    for (int i = 0; i < bp->moveArray.count; ++i)
    {
        printf(" %d", bp->moveArray.data[i]);
    }
    printf("\n");
}

int main(void)
{
    b2BroadPhase bp;
    b2CreateBroadPhase(&bp);

    int staticKey = b2BroadPhase_CreateProxy(&bp, b2_staticBody, (b2AABB){{0.0f, 0.0f}, {1.0f, 1.0f}}, 0x1u, 11, false);
    int dynamicKey = b2BroadPhase_CreateProxy(&bp, b2_dynamicBody, (b2AABB){{0.5f, 0.5f}, {1.5f, 1.5f}}, 0x2u, 22, false);
    int kinematicKey = b2BroadPhase_CreateProxy(&bp, b2_kinematicBody, (b2AABB){{2.0f, 0.0f}, {3.0f, 1.0f}}, 0x4u, 33, false);
    int forcedStaticKey = b2BroadPhase_CreateProxy(&bp, b2_staticBody, (b2AABB){{4.0f, 0.0f}, {5.0f, 1.0f}}, 0x8u, 44, true);

    printf("keys %d %d %d %d\n", staticKey, dynamicKey, kinematicKey, forcedStaticKey);
    print_move_array(&bp);

    b2BroadPhase_MoveProxy(&bp, dynamicKey, (b2AABB){{0.25f, 0.25f}, {1.25f, 1.25f}});
    b2BroadPhase_EnlargeProxy(&bp, kinematicKey, (b2AABB){{1.75f, -0.25f}, {3.25f, 1.25f}});
    print_move_array(&bp);

    printf("state %d %d %d %d %d %d\n",
           b2BroadPhase_GetShapeIndex(&bp, dynamicKey),
           b2BroadPhase_TestOverlap(&bp, staticKey, dynamicKey) ? 1 : 0,
           b2BroadPhase_TestOverlap(&bp, dynamicKey, kinematicKey) ? 1 : 0,
           b2DynamicTree_GetProxyCount(bp.trees + b2_staticBody),
           b2DynamicTree_GetProxyCount(bp.trees + b2_dynamicBody),
           b2DynamicTree_GetProxyCount(bp.trees + b2_kinematicBody));

    b2BroadPhase_RebuildTrees(&bp);
    printf("rebuilt %d %d %d %.9g %.9g\n",
           b2DynamicTree_GetHeight(bp.trees + b2_staticBody),
           b2DynamicTree_GetHeight(bp.trees + b2_dynamicBody),
           b2DynamicTree_GetHeight(bp.trees + b2_kinematicBody),
           b2DynamicTree_GetAreaRatio(bp.trees + b2_dynamicBody),
           b2DynamicTree_GetAreaRatio(bp.trees + b2_kinematicBody));

    b2BroadPhase_DestroyProxy(&bp, forcedStaticKey);
    print_move_array(&bp);
    printf("afterDestroy %d %d\n",
           b2DynamicTree_GetProxyCount(bp.trees + b2_staticBody),
           b2BroadPhase_TestOverlap(&bp, staticKey, dynamicKey) ? 1 : 0);

    b2DestroyBroadPhase(&bp);
    return 0;
}
