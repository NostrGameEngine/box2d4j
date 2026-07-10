package org.box2d4j;

public class b2ChainDef {
    public Object userData;
    public b2Vec2[] points;
    public int count;
    public b2SurfaceMaterial[] materials;
    public int materialCount;
    public b2Filter filter = new b2Filter();
    public boolean isLoop;
    public boolean enableSensorEvents;
    public int internalValue;
}
