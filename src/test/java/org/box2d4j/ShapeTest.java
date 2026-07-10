package org.box2d4j;

import org.junit.jupiter.api.Test;

import static org.box2d4j.B2.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ShapeTest {
    private static final float FLT_EPSILON = Math.ulp(1.0f);

    private static void ensureSmall(float value, float tolerance) {
        assertTrue(Math.abs(value) <= tolerance, "value=" + value + ", tolerance=" + tolerance);
    }

    @Test
    void shapeMassAabbPointAndRayCast() {
        b2Capsule capsule = new b2Capsule(new b2Vec2(-1.0f, 0.0f), new b2Vec2(1.0f, 0.0f), 1.0f);
        b2Circle circle = new b2Circle(new b2Vec2(1.0f, 0.0f), 1.0f);
        b2Polygon box = b2MakeBox(1.0f, 1.0f);
        b2Segment segment = new b2Segment(new b2Vec2(0.0f, 1.0f), new b2Vec2(0.0f, -1.0f));

        b2MassData circleMass = b2ComputeCircleMass(circle, 1.0f);
        ensureSmall(circleMass.mass - B2_PI, FLT_EPSILON);
        assertTrue(circleMass.center.x == 1.0f && circleMass.center.y == 0.0f);
        ensureSmall(circleMass.rotationalInertia - 1.5f * B2_PI, FLT_EPSILON);

        float radius = capsule.radius;
        float length = b2Distance(capsule.center1, capsule.center2);
        b2MassData capsuleMass = b2ComputeCapsuleMass(capsule, 1.0f);
        b2Polygon r = b2MakeBox(radius, radius + 0.5f * length);
        b2MassData mdr = b2ComputePolygonMass(r, 1.0f);
        b2Vec2[] points = b2Vec2.array(8);
        float d = B2_PI / 3.0f;
        float angle = -0.5f * B2_PI;
        for (int i = 0; i < 4; ++i) {
            points[i].x = 1.0f + radius * (float) Math.cos(angle);
            points[i].y = radius * (float) Math.sin(angle);
            angle += d;
        }
        angle = 0.5f * B2_PI;
        for (int i = 4; i < 8; ++i) {
            points[i].x = -1.0f + radius * (float) Math.cos(angle);
            points[i].y = radius * (float) Math.sin(angle);
            angle += d;
        }
        b2Hull hull = b2ComputeHull(points, 8);
        b2MassData ma = b2ComputePolygonMass(b2MakePolygon(hull, 0.0f), 1.0f);
        assertTrue(ma.mass < capsuleMass.mass && capsuleMass.mass < mdr.mass);
        assertTrue(ma.rotationalInertia < capsuleMass.rotationalInertia && capsuleMass.rotationalInertia < mdr.rotationalInertia);

        b2MassData boxMass = b2ComputePolygonMass(box, 1.0f);
        ensureSmall(boxMass.mass - 4.0f, FLT_EPSILON);
        ensureSmall(boxMass.center.x, FLT_EPSILON);
        ensureSmall(boxMass.center.y, FLT_EPSILON);
        ensureSmall(boxMass.rotationalInertia - 8.0f / 3.0f, 2.0f * FLT_EPSILON);

        b2AABB circleAabb = b2ComputeCircleAABB(circle, b2Transform_identity);
        ensureSmall(circleAabb.lowerBound.x, FLT_EPSILON);
        ensureSmall(circleAabb.lowerBound.y + 1.0f, FLT_EPSILON);
        ensureSmall(circleAabb.upperBound.x - 2.0f, FLT_EPSILON);
        ensureSmall(circleAabb.upperBound.y - 1.0f, FLT_EPSILON);

        b2AABB boxAabb = b2ComputePolygonAABB(box, b2Transform_identity);
        ensureSmall(boxAabb.lowerBound.x + 1.0f, FLT_EPSILON);
        ensureSmall(boxAabb.lowerBound.y + 1.0f, FLT_EPSILON);
        ensureSmall(boxAabb.upperBound.x - 1.0f, FLT_EPSILON);
        ensureSmall(boxAabb.upperBound.y - 1.0f, FLT_EPSILON);

        b2AABB segmentAabb = b2ComputeSegmentAABB(segment, b2Transform_identity);
        ensureSmall(segmentAabb.lowerBound.x, FLT_EPSILON);
        ensureSmall(segmentAabb.lowerBound.y + 1.0f, FLT_EPSILON);
        ensureSmall(segmentAabb.upperBound.x, FLT_EPSILON);
        ensureSmall(segmentAabb.upperBound.y - 1.0f, FLT_EPSILON);

        assertTrue(b2PointInCircle(new b2Vec2(0.5f, 0.5f), circle));
        assertFalse(b2PointInCircle(new b2Vec2(4.0f, -4.0f), circle));
        assertTrue(b2PointInPolygon(new b2Vec2(0.5f, 0.5f), box));
        assertFalse(b2PointInPolygon(new b2Vec2(4.0f, -4.0f), box));

        b2RayCastInput input = new b2RayCastInput();
        input.origin = new b2Vec2(-4.0f, 0.0f);
        input.translation = new b2Vec2(8.0f, 0.0f);
        input.maxFraction = 1.0f;

        b2CastOutput output = b2RayCastCircle(input, circle);
        assertTrue(output.hit);
        ensureSmall(output.normal.x + 1.0f, FLT_EPSILON);
        ensureSmall(output.normal.y, FLT_EPSILON);
        ensureSmall(output.fraction - 0.5f, FLT_EPSILON);

        output = b2RayCastPolygon(input, box);
        assertTrue(output.hit);
        ensureSmall(output.normal.x + 1.0f, FLT_EPSILON);
        ensureSmall(output.normal.y, FLT_EPSILON);
        ensureSmall(output.fraction - 3.0f / 8.0f, FLT_EPSILON);

        output = b2RayCastSegment(input, segment, true);
        assertTrue(output.hit);
        ensureSmall(output.normal.x + 1.0f, FLT_EPSILON);
        ensureSmall(output.normal.y, FLT_EPSILON);
        ensureSmall(output.fraction - 0.5f, FLT_EPSILON);
    }
}
