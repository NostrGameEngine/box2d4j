// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <setjmp.h>
#include <stdio.h>

static jmp_buf assertionJump;

static int catch_assertion(const char* condition, const char* fileName, int lineNumber)
{
    (void)condition;
    (void)fileName;
    (void)lineNumber;
    longjmp(assertionJump, 1);
    return 0;
}

static void run_case(int index)
{
    b2DynamicTree tree = b2DynamicTree_Create();
    b2AABB unit = {{0.0f, 0.0f}, {1.0f, 1.0f}};
    int proxyId = b2DynamicTree_CreateProxy(&tree, unit, 1u, 7u);
    switch (index)
    {
        case 0: (void)b2DynamicTree_CreateProxy(&tree, (b2AABB){{-100000.0f, 0.0f}, {0.0f, 1.0f}}, 1u, 0u); break;
        case 1: (void)b2DynamicTree_CreateProxy(&tree, (b2AABB){{0.0f, 0.0f}, {1.0f, 100000.0f}}, 1u, 0u); break;
        case 2: b2DynamicTree_DestroyProxy(&tree, -1); break;
        case 3: b2DynamicTree_DestroyProxy(&tree, tree.nodeCapacity); break;
        case 4: b2DynamicTree_DestroyProxy(&tree, proxyId); b2DynamicTree_DestroyProxy(&tree, proxyId); break;
        case 5: b2DynamicTree_MoveProxy(&tree, proxyId, (b2AABB){{1.0f, 1.0f}, {-1.0f, -1.0f}}); break;
        case 6: b2DynamicTree_MoveProxy(&tree, proxyId, (b2AABB){{0.0f, 0.0f}, {100000.0f, 1.0f}}); break;
        case 7: b2DynamicTree_MoveProxy(&tree, -1, unit); break;
        case 8: b2DynamicTree_EnlargeProxy(&tree, proxyId, (b2AABB){{1.0f, 1.0f}, {-1.0f, -1.0f}}); break;
        case 9: b2DynamicTree_EnlargeProxy(&tree, proxyId, (b2AABB){{0.0f, 0.0f}, {1.0f, 100000.0f}}); break;
        case 10: b2DynamicTree_EnlargeProxy(&tree, proxyId, (b2AABB){{0.2f, 0.2f}, {0.8f, 0.8f}}); break;
        case 11: (void)b2DynamicTree_GetCategoryBits(&tree, -1); break;
        case 12: (void)b2DynamicTree_GetUserData(&tree, tree.nodeCapacity); break;
        default: (void)b2DynamicTree_GetAABB(&tree, -1); break;
    }
    b2DynamicTree_Destroy(&tree);
}

static int asserts(int index)
{
    if (setjmp(assertionJump) == 0)
    {
        run_case(index);
        return 0;
    }
    return 1;
}

int main(void)
{
    b2SetAssertFcn(catch_assertion);
    printf("dynamicTreeValidation");
    for (int i = 0; i < 14; ++i)
    {
        printf(" %d", asserts(i));
    }
    printf("\n");
    return 0;
}
