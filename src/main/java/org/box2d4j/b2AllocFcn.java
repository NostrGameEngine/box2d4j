package org.box2d4j;

import java.nio.ByteBuffer;

@FunctionalInterface
public interface b2AllocFcn {
    ByteBuffer invoke(int size, int alignment);
}
