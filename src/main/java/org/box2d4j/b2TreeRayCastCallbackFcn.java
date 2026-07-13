package org.box2d4j;

@FunctionalInterface
public interface b2TreeRayCastCallbackFcn {
    float invoke(b2RayCastInput input, int proxyId, long userData);
}
