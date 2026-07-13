package org.box2d4j;

@FunctionalInterface
public interface b2AssertFcn {
    int invoke(String condition, String fileName, int lineNumber);
}
