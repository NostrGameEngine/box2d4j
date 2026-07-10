package org.box2d4j;

public class b2WeldJointDef {
    public b2BodyId bodyIdA = new b2BodyId();
    public b2BodyId bodyIdB = new b2BodyId();
    public b2Vec2 localAnchorA = new b2Vec2();
    public b2Vec2 localAnchorB = new b2Vec2();
    public float referenceAngle;
    public float linearHertz;
    public float angularHertz;
    public float linearDampingRatio;
    public float angularDampingRatio;
    public boolean collideConnected;
    public Object userData;
    public int internalValue;
}
