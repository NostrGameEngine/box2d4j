package org.box2d4j;

public class b2MotorJointDef {
    public b2BodyId bodyIdA = new b2BodyId();
    public b2BodyId bodyIdB = new b2BodyId();
    public b2Vec2 linearOffset = new b2Vec2();
    public float angularOffset;
    public float maxForce;
    public float maxTorque;
    public float correctionFactor;
    public boolean collideConnected;
    public Object userData;
    public int internalValue;
}
