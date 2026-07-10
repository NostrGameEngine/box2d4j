package org.box2d4j;

public class b2ShapeDef {
    public Object userData;
    public float density;
    public b2Filter filter = new b2Filter();
    public b2SurfaceMaterial material = new b2SurfaceMaterial();
    public boolean isSensor;
    public boolean enableSensorEvents;
    public boolean enableContactEvents;
    public boolean enableHitEvents;
    public boolean enablePreSolveEvents;
    public boolean invokeContactCreation;
    public boolean updateBodyMass;
    public int internalValue;
}
