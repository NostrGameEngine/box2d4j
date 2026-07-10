package org.box2d4j;

@FunctionalInterface
public interface b2TreeShapeCastCallback {
    float invoke(b2ShapeCastInput input, int proxyId, long userData);
}
