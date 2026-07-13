package org.box2d4j;

import java.nio.ByteBuffer;

@FunctionalInterface
public interface b2FreeFcn {
    void invoke(ByteBuffer memory);
}
