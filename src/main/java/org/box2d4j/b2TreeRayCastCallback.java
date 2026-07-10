package org.box2d4j;

@FunctionalInterface
public interface b2TreeRayCastCallback {
    float invoke(b2RayCastInput input, int proxyId, long userData);
}
