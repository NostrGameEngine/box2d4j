package org.box2d4j;

public class b2MouseJointDef {
    public b2BodyId bodyIdA = new b2BodyId();
    public b2BodyId bodyIdB = new b2BodyId();
    public b2Vec2 target = new b2Vec2();
    public float hertz;
    public float dampingRatio;
    public float maxForce;
    public boolean collideConnected;
    public Object userData;
    public int internalValue;
}
