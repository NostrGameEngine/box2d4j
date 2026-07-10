package org.box2d4j;

import org.junit.jupiter.api.Test;

import static org.box2d4j.B2.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MathTest {
    private static final float FLT_EPSILON = Math.ulp(1.0f);
    private static final float ATAN_TOL = 0.00004f;

    private static void ensureSmall(float value, float tolerance) {
        assertTrue(Math.abs(value) <= tolerance, "value=" + value + ", tolerance=" + tolerance);
    }

    @Test
    void mathParity() {
        for (float t = -10.0f; t < 10.0f; t += 0.01f) {
            float angle = B2_PI * t;
            b2Rot r = b2MakeRot(angle);
            float c = (float) Math.cos(angle);
            float s = (float) Math.sin(angle);

            ensureSmall(r.c - c, 0.002f);
            ensureSmall(r.s - s, 0.002f);

            float xn = b2UnwindAngle(angle);
            assertTrue(-B2_PI <= xn && xn <= B2_PI);

            float a = b2Atan2(s, c);
            assertTrue(b2IsValidFloat(a));

            float diff = b2AbsFloat(a - xn);
            if (diff > B2_PI) {
                diff -= 2.0f * B2_PI;
            }
            ensureSmall(diff, ATAN_TOL);
        }

        for (float y = -1.0f; y <= 1.0f; y += 0.01f) {
            for (float x = -1.0f; x <= 1.0f; x += 0.01f) {
                float a1 = b2Atan2(y, x);
                float a2 = (float) Math.atan2(y, x);
                float diff = b2AbsFloat(a1 - a2);
                assertTrue(b2IsValidFloat(a1));
                ensureSmall(diff, ATAN_TOL);
            }
        }

        specialAtan2(1.0f, 0.0f);
        specialAtan2(-1.0f, 0.0f);
        specialAtan2(0.0f, 1.0f);
        specialAtan2(0.0f, -1.0f);
        specialAtan2(0.0f, 0.0f);

        b2Vec2 zero = b2Vec2_zero.copy();
        b2Vec2 one = new b2Vec2(1.0f, 1.0f);
        b2Vec2 two = new b2Vec2(2.0f, 2.0f);

        b2Vec2 v = b2Add(one, two);
        assertTrue(v.x == 3.0f && v.y == 3.0f);

        v = b2Sub(zero, two);
        assertTrue(v.x == -2.0f && v.y == -2.0f);

        v = b2Add(two, two);
        assertTrue(v.x != 5.0f && v.y != 5.0f);

        b2Transform transform1 = new b2Transform(new b2Vec2(-2.0f, 3.0f), b2MakeRot(1.0f));
        b2Transform transform2 = new b2Transform(new b2Vec2(1.0f, 0.0f), b2MakeRot(-2.0f));
        b2Transform transform = b2MulTransforms(transform2, transform1);

        v = b2TransformPoint(transform2, b2TransformPoint(transform1, two));
        b2Vec2 u = b2TransformPoint(transform, two);

        ensureSmall(u.x - v.x, 10.0f * FLT_EPSILON);
        ensureSmall(u.y - v.y, 10.0f * FLT_EPSILON);

        v = b2TransformPoint(transform1, two);
        v = b2InvTransformPoint(transform1, v);

        ensureSmall(v.x - two.x, 8.0f * FLT_EPSILON);
        ensureSmall(v.y - two.y, 8.0f * FLT_EPSILON);

        v = b2Normalize(new b2Vec2(0.2f, -0.5f));
        for (float y = -1.0f; y <= 1.0f; y += 0.01f) {
            for (float x = -1.0f; x <= 1.0f; x += 0.01f) {
                if (x == 0.0f && y == 0.0f) {
                    continue;
                }

                u = b2Normalize(new b2Vec2(x, y));
                b2Rot r = b2ComputeRotationBetweenUnitVectors(v, u);
                b2Vec2 w = b2RotateVector(r, v);
                ensureSmall(w.x - u.x, 4.0f * FLT_EPSILON);
                ensureSmall(w.y - u.y, 4.0f * FLT_EPSILON);
            }
        }

        b2Rot q1 = b2Rot_identity.copy();
        b2Rot q2 = b2MakeRot(0.5f * B2_PI);
        int n = 100;
        for (int i = 0; i <= n; ++i) {
            float alpha = (float) i / (float) n;
            b2Rot q = b2NLerp(q1, q2, alpha);
            float angle = b2Rot_GetAngle(q);
            ensureSmall(alpha * 0.5f * B2_PI - angle, 5.0f * B2_PI / 180.0f);
        }
    }

    private static void specialAtan2(float y, float x) {
        float a1 = b2Atan2(y, x);
        float a2 = (float) Math.atan2(y, x);
        float diff = b2AbsFloat(a1 - a2);
        assertTrue(b2IsValidFloat(a1));
        ensureSmall(diff, ATAN_TOL);
    }
}
