package org.box2d4j;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.box2d4j.B2.*;
import static org.junit.jupiter.api.Assertions.*;

final class PlatformUtilitiesTest {
    @Test
    void versionTimingAndYieldExposeJavaElevenFriendlyContracts() throws Exception {
        b2Version version = b2GetVersion();
        assertAll(
            () -> assertEquals(3, version.major),
            () -> assertEquals(1, version.minor),
            () -> assertEquals(1, version.revision));

        long ticks = b2GetTicks();
        Thread.sleep(2L);
        assertTrue(b2GetMilliseconds(ticks) >= 0.0f);
        long[] resetTicks = {ticks};
        assertTrue(b2GetMillisecondsAndReset(resetTicks) >= 0.0f);
        assertTrue(resetTicks[0] >= ticks);
        b2Yield();
    }

    @Test
    void assertHookReceivesTheOriginalDiagnostic() {
        AtomicReference<String> diagnostic = new AtomicReference<>();
        try {
            b2SetAssertFcn((condition, fileName, lineNumber) -> {
                diagnostic.set(condition + ":" + fileName + ":" + lineNumber);
                return 0;
            });
            assertEquals(0, b2InternalAssertFcn("condition", "file.c", 42));
            assertEquals("condition:file.c:42", diagnostic.get());
        } finally {
            b2SetAssertFcn((condition, fileName, lineNumber) -> 1);
        }
    }

    @Test
    void lengthUnitsAndValidationHelpersPreserveUpstreamContracts() {
        float previous = b2GetLengthUnitsPerMeter();
        try {
            b2SetLengthUnitsPerMeter(2.5f);
            assertEquals(2.5f, b2GetLengthUnitsPerMeter(), 0.0f);
            assertEquals(0.005f * 2.5f, B2_LINEAR_SLOP(), 0.0f);
            assertThrows(AssertionError.class, () -> b2SetLengthUnitsPerMeter(0.0f));

            assertTrue(b2IsValidVec2(new b2Vec2(1.0f, -2.0f)));
            assertFalse(b2IsValidVec2(new b2Vec2(Float.NaN, 0.0f)));
            assertTrue(b2IsValidRotation(b2MakeRot(0.4f)));
            assertFalse(b2IsValidRotation(new b2Rot(1.0f, 1.0f)));
            assertTrue(b2IsValidPlane(new b2Plane(new b2Vec2(0.0f, 1.0f), 3.0f)));
            assertFalse(b2IsValidPlane(new b2Plane(new b2Vec2(0.0f, 2.0f), 3.0f)));
            assertTrue(b2IsValidRay(new b2RayCastInput(new b2Vec2(), new b2Vec2(1.0f, 0.0f), 1.0f)));
            assertFalse(b2IsValidRay(new b2RayCastInput(new b2Vec2(), new b2Vec2(1.0f, 0.0f), -1.0f)));
        } finally {
            b2SetLengthUnitsPerMeter(previous);
        }
    }
}
