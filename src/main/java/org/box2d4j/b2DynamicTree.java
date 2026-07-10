package org.box2d4j;

public class b2DynamicTree {
    b2TreeNode[] nodes;
    public int root;
    public int nodeCount;
    public int nodeCapacity;
    public int freeList;
    public int proxyCount;
    int[] leafIndices;
    b2AABB[] leafBoxes;
    b2Vec2[] leafCenters;
    int[] binIndices;
    public int rebuildCapacity;
}
