package org.box2d4j;

@FunctionalInterface
public interface b2TreeQueryCallback {
    boolean invoke(int proxyId, long userData);
}
