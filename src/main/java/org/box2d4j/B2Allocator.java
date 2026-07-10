package org.box2d4j;

import java.nio.ByteBuffer;

@FunctionalInterface
public interface B2Allocator {
    ByteBuffer allocate(int size, int alignment);
}
