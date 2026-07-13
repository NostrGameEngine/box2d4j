package org.box2d4j;

@FunctionalInterface
public interface B2AssertHandler extends b2AssertFcn {
    int assertFailed(String condition, String fileName, int lineNumber);

    @Override
    default int invoke(String condition, String fileName, int lineNumber) {
        return assertFailed(condition, fileName, lineNumber);
    }
}
