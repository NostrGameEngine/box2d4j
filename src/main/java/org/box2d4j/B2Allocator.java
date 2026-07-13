package org.box2d4j;

import java.nio.ByteBuffer;

@FunctionalInterface
public interface B2Allocator extends b2AllocFcn {
    ByteBuffer allocate(int size, int alignment);

    @Override
    default ByteBuffer invoke(int size, int alignment) {
        return allocate(size, alignment);
    }
}
