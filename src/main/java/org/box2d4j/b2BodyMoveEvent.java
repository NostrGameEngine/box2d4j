package org.box2d4j;

public class b2BodyMoveEvent {
    public b2Transform transform = new b2Transform();
    public b2BodyId bodyId = new b2BodyId();
    public Object userData;
    public boolean fellAsleep;
}
