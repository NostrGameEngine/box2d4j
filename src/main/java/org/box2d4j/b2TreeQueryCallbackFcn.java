package org.box2d4j;

@FunctionalInterface
public interface b2TreeQueryCallbackFcn {
    boolean invoke(int proxyId, long userData);
}
