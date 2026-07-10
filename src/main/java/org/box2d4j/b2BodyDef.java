package org.box2d4j;

public class b2BodyDef {
    public int type;
    public b2Vec2 position = new b2Vec2();
    public b2Rot rotation = new b2Rot();
    public b2Vec2 linearVelocity = new b2Vec2();
    public float angularVelocity;
    public float linearDamping;
    public float angularDamping;
    public float gravityScale;
    public float sleepThreshold;
    public String name;
    public Object userData;
    public boolean enableSleep;
    public boolean isAwake;
    public boolean fixedRotation;
    public boolean isBullet;
    public boolean isEnabled;
    public boolean allowFastRotation;
    public int internalValue;
}
