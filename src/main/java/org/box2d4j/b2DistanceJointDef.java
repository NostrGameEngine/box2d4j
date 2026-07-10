package org.box2d4j;

public class b2DistanceJointDef {
    public b2BodyId bodyIdA = new b2BodyId();
    public b2BodyId bodyIdB = new b2BodyId();
    public b2Vec2 localAnchorA = new b2Vec2();
    public b2Vec2 localAnchorB = new b2Vec2();
    public float length;
    public boolean enableSpring;
    public float hertz;
    public float dampingRatio;
    public boolean enableLimit;
    public float minLength;
    public float maxLength;
    public boolean enableMotor;
    public float maxMotorForce;
    public float motorSpeed;
    public boolean collideConnected;
    public Object userData;
    public int internalValue;
}
