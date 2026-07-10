package org.box2d4j.debugger;

import org.box2d4j.b2DebugDraw;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import static org.box2d4j.B2.b2World_Draw;

final class WorldDrawBatch {
    private static final int CIRCLE_SEGMENTS = 20;

    final FloatList fillVertices = new FloatList();
    final IntList fillColors = new IntList();
    final FloatList lineVertices = new FloatList();
    final IntList lineColors = new IntList();
    final Bounds bounds = new Bounds();

    static WorldDrawBatch capture(b2WorldId worldId, DrawOptions options, float pointSize) {
        WorldDrawBatch batch = new WorldDrawBatch();
        b2DebugDraw draw = new b2DebugDraw();
        draw.drawShapes = options.shapes;
        draw.drawJoints = options.joints;
        draw.drawJointExtras = options.jointExtras;
        draw.drawBounds = options.bounds;
        draw.drawContacts = options.contacts;
        draw.drawGraphColors = options.graphColors;
        draw.drawContactNormals = options.contactNormals;
        draw.drawContactImpulses = options.contactImpulses;
        draw.drawFrictionImpulses = options.frictionImpulses;
        draw.DrawPolygonFcn = (vertices, count, color) -> batch.outline(vertices, count, color);
        draw.DrawSolidPolygonFcn = (transform, vertices, count, radius, color) ->
            batch.solidPolygon(transform, vertices, count, color);
        draw.DrawCircleFcn = (center, radius, color) -> batch.circle(center, radius, color, false);
        draw.DrawSolidCircleFcn = (transform, radius, color) -> batch.circle(transform.p, radius, color, true);
        draw.DrawSolidCapsuleFcn = (p1, p2, radius, color) -> batch.capsule(p1, p2, radius, color);
        draw.DrawSegmentFcn = batch::line;
        draw.DrawTransformFcn = transform -> batch.transform(transform, pointSize);
        draw.DrawPointFcn = (point, size, color) -> batch.point(point, Math.max(pointSize, 0.02f), color);
        draw.DrawStringFcn = (point, text, color) -> {
        };
        b2World_Draw(worldId, draw);
        return batch;
    }

    void solidPolygon(b2Transform transform, b2Vec2[] vertices, int count, int color) {
        if (count < 2) {
            return;
        }
        float[] x = new float[count];
        float[] y = new float[count];
        for (int i = 0; i < count; ++i) {
            float localX = vertices[i].x;
            float localY = vertices[i].y;
            x[i] = transform.p.x + transform.q.c * localX - transform.q.s * localY;
            y[i] = transform.p.y + transform.q.s * localX + transform.q.c * localY;
            bounds.include(x[i], y[i]);
        }
        for (int i = 1; i < count - 1; ++i) {
            triangle(x[0], y[0], x[i], y[i], x[i + 1], y[i + 1], color);
        }
        for (int i = 0; i < count; ++i) {
            addLine(x[i], y[i], x[(i + 1) % count], y[(i + 1) % count], color);
        }
    }

    private void outline(b2Vec2[] vertices, int count, int color) {
        for (int i = 0; i < count; ++i) {
            b2Vec2 a = vertices[i];
            b2Vec2 b = vertices[(i + 1) % count];
            addLine(a.x, a.y, b.x, b.y, color);
        }
    }

    void circle(b2Vec2 center, float radius, int color, boolean solid) {
        float previousX = center.x + radius;
        float previousY = center.y;
        bounds.include(center.x - radius, center.y - radius);
        bounds.include(center.x + radius, center.y + radius);
        for (int i = 1; i <= CIRCLE_SEGMENTS; ++i) {
            float angle = (float) (2.0 * Math.PI * i / CIRCLE_SEGMENTS);
            float x = center.x + radius * (float) Math.cos(angle);
            float y = center.y + radius * (float) Math.sin(angle);
            if (solid) {
                triangle(center.x, center.y, previousX, previousY, x, y, color);
            }
            addLine(previousX, previousY, x, y, color);
            previousX = x;
            previousY = y;
        }
    }

    void capsule(b2Vec2 p1, b2Vec2 p2, float radius, int color) {
        float angle = (float) Math.atan2(p2.y - p1.y, p2.x - p1.x);
        int capSegments = CIRCLE_SEGMENTS / 2;
        int count = 2 * (capSegments + 1);
        float[] x = new float[count];
        float[] y = new float[count];
        int index = 0;
        for (int i = 0; i <= capSegments; ++i) {
            float a = angle - 0.5f * (float) Math.PI + (float) Math.PI * i / capSegments;
            x[index] = p2.x + radius * (float) Math.cos(a);
            y[index] = p2.y + radius * (float) Math.sin(a);
            index += 1;
        }
        for (int i = 0; i <= capSegments; ++i) {
            float a = angle + 0.5f * (float) Math.PI + (float) Math.PI * i / capSegments;
            x[index] = p1.x + radius * (float) Math.cos(a);
            y[index] = p1.y + radius * (float) Math.sin(a);
            index += 1;
        }
        float centerX = 0.5f * (p1.x + p2.x);
        float centerY = 0.5f * (p1.y + p2.y);
        for (int i = 0; i < count; ++i) {
            bounds.include(x[i], y[i]);
            triangle(centerX, centerY, x[i], y[i], x[(i + 1) % count], y[(i + 1) % count], color);
            addLine(x[i], y[i], x[(i + 1) % count], y[(i + 1) % count], color);
        }
    }

    private void transform(b2Transform transform, float size) {
        float axis = Math.max(0.25f, 8.0f * size);
        line(transform.p, new b2Vec2(transform.p.x + axis * transform.q.c,
            transform.p.y + axis * transform.q.s), 0xF05252);
        line(transform.p, new b2Vec2(transform.p.x - axis * transform.q.s,
            transform.p.y + axis * transform.q.c), 0x41C47A);
    }

    void point(b2Vec2 point, float size, int color) {
        addLine(point.x - size, point.y, point.x + size, point.y, color);
        addLine(point.x, point.y - size, point.x, point.y + size, color);
    }

    void line(b2Vec2 p1, b2Vec2 p2, int color) {
        addLine(p1.x, p1.y, p2.x, p2.y, color);
    }

    private void triangle(float x1, float y1, float x2, float y2, float x3, float y3, int color) {
        addFillVertex(x1, y1, color);
        addFillVertex(x2, y2, color);
        addFillVertex(x3, y3, color);
    }

    private void addFillVertex(float x, float y, int color) {
        fillVertices.add(x);
        fillVertices.add(y);
        fillColors.add(color);
        bounds.include(x, y);
    }

    private void addLine(float x1, float y1, float x2, float y2, int color) {
        lineVertices.add(x1);
        lineVertices.add(y1);
        lineVertices.add(x2);
        lineVertices.add(y2);
        lineColors.add(color);
        lineColors.add(color);
        bounds.include(x1, y1);
        bounds.include(x2, y2);
    }

    static final class DrawOptions {
        boolean shapes = true;
        boolean joints = true;
        boolean jointExtras;
        boolean bounds;
        boolean contacts;
        boolean graphColors;
        boolean contactNormals;
        boolean contactImpulses;
        boolean frictionImpulses;
    }

    static final class Bounds {
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;

        void include(float x, float y) {
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }

        boolean isValid() {
            return minX <= maxX && minY <= maxY;
        }

        float width() {
            return maxX - minX;
        }

        float height() {
            return maxY - minY;
        }
    }

    static final class FloatList {
        private float[] values = new float[1024];
        private int size;

        void add(float value) {
            ensure(size + 1);
            values[size++] = value;
        }

        float get(int index) {
            return values[index];
        }

        int size() {
            return size;
        }

        private void ensure(int capacity) {
            if (capacity > values.length) {
                float[] next = new float[Math.max(capacity, values.length + values.length / 2)];
                System.arraycopy(values, 0, next, 0, size);
                values = next;
            }
        }
    }

    static final class IntList {
        private int[] values = new int[512];
        private int size;

        void add(int value) {
            ensure(size + 1);
            values[size++] = value;
        }

        int get(int index) {
            return values[index];
        }

        private void ensure(int capacity) {
            if (capacity > values.length) {
                int[] next = new int[Math.max(capacity, values.length + values.length / 2)];
                System.arraycopy(values, 0, next, 0, size);
                values = next;
            }
        }
    }
}
