package org.box2d4j;

import org.junit.jupiter.api.Test;

import static org.box2d4j.B2.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DistanceTest {
    private static final float FLT_EPSILON = Math.ulp(1.0f);

    private static void ensureSmall(float value, float tolerance) {
        assertTrue(Math.abs(value) <= tolerance, "value=" + value + ", tolerance=" + tolerance);
    }

    @Test
    void segmentShapeCastAndToi() {
        b2SegmentDistanceResult result = b2SegmentDistance(
            new b2Vec2(-1.0f, -1.0f), new b2Vec2(-1.0f, 1.0f),
            new b2Vec2(2.0f, 0.0f), new b2Vec2(1.0f, 0.0f));

        ensureSmall(result.fraction1 - 0.5f, FLT_EPSILON);
        ensureSmall(result.fraction2 - 1.0f, FLT_EPSILON);
        ensureSmall(result.closest1.x + 1.0f, FLT_EPSILON);
        ensureSmall(result.closest1.y, FLT_EPSILON);
        ensureSmall(result.closest2.x - 1.0f, FLT_EPSILON);
        ensureSmall(result.closest2.y, FLT_EPSILON);
        ensureSmall(result.distanceSquared - 4.0f, FLT_EPSILON);

        b2Vec2[] vas = {new b2Vec2(-1.0f, -1.0f), new b2Vec2(1.0f, -1.0f), new b2Vec2(1.0f, 1.0f), new b2Vec2(-1.0f, 1.0f)};
        b2Vec2[] vbs = {new b2Vec2(2.0f, -1.0f), new b2Vec2(2.0f, 1.0f)};

        b2DistanceInput distanceInput = new b2DistanceInput();
        distanceInput.proxyA = b2MakeProxy(vas, vas.length, 0.0f);
        distanceInput.proxyB = b2MakeProxy(vbs, vbs.length, 0.0f);
        distanceInput.transformA = b2Transform_identity;
        distanceInput.transformB = b2Transform_identity;
        b2DistanceOutput distance = b2ShapeDistance(distanceInput, new b2SimplexCache(), null, 0);
        ensureSmall(distance.distance - 1.0f, FLT_EPSILON);

        b2ShapeCastPairInput castInput = new b2ShapeCastPairInput();
        castInput.proxyA = b2MakeProxy(vas, vas.length, 0.0f);
        castInput.proxyB = b2MakeProxy(vbs, vbs.length, 0.0f);
        castInput.transformA = b2Transform_identity;
        castInput.transformB = b2Transform_identity;
        castInput.translationB = new b2Vec2(-2.0f, 0.0f);
        castInput.maxFraction = 1.0f;
        b2CastOutput cast = b2ShapeCast(castInput);
        assertTrue(cast.hit);
        ensureSmall(cast.fraction - 0.5f, 0.005f);

        b2TOIInput toiInput = new b2TOIInput();
        toiInput.proxyA = b2MakeProxy(vas, vas.length, 0.0f);
        toiInput.proxyB = b2MakeProxy(vbs, vbs.length, 0.0f);
        toiInput.sweepA.localCenter = b2Vec2_zero.copy();
        toiInput.sweepA.c1 = b2Vec2_zero.copy();
        toiInput.sweepA.c2 = b2Vec2_zero.copy();
        toiInput.sweepA.q1 = b2Rot_identity.copy();
        toiInput.sweepA.q2 = b2Rot_identity.copy();
        toiInput.sweepB.localCenter = b2Vec2_zero.copy();
        toiInput.sweepB.c1 = b2Vec2_zero.copy();
        toiInput.sweepB.c2 = new b2Vec2(-2.0f, 0.0f);
        toiInput.sweepB.q1 = b2Rot_identity.copy();
        toiInput.sweepB.q2 = b2Rot_identity.copy();
        toiInput.maxFraction = 1.0f;
        b2TOIOutput toi = b2TimeOfImpact(toiInput);
        assertEquals(b2_toiStateHit, toi.state);
        ensureSmall(toi.fraction - 0.5f, 0.005f);
    }
}
