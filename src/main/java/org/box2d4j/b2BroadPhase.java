package org.box2d4j;

import java.util.ArrayList;

public class b2BroadPhase {
    public b2DynamicTree[] trees = new b2DynamicTree[3];
    public b2HashSet moveSet = new b2HashSet();
    public ArrayList<Integer> moveArray = new ArrayList<>();
    public b2HashSet pairSet = new b2HashSet();
}
