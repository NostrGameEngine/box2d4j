package org.box2d4j;

@FunctionalInterface
public interface B2AssertHandler {
    int assertFailed(String condition, String fileName, int lineNumber);
}
