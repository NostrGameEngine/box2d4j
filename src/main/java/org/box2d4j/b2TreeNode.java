package org.box2d4j;

final class b2TreeNode {
    b2AABB aabb = new b2AABB();
    long categoryBits = B2.B2_DEFAULT_CATEGORY_BITS;
    int child1 = -1;
    int child2 = -1;
    long userData = -1L;
    int parent = -1;
    int next = -1;
    int height;
    int flags;
}
