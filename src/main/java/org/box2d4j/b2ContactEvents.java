package org.box2d4j;

public class b2ContactEvents {
    public b2ContactBeginTouchEvent[] beginEvents = new b2ContactBeginTouchEvent[0];
    public b2ContactEndTouchEvent[] endEvents = new b2ContactEndTouchEvent[0];
    public b2ContactHitEvent[] hitEvents = new b2ContactHitEvent[0];
    public int beginCount;
    public int endCount;
    public int hitCount;
}
