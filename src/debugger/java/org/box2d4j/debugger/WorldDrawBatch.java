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
    final java.util.ArrayList<Label> labels = new java.util.ArrayList<>();
    final Bounds bounds = new Bounds();
    private final float[] capsuleX = new float[CIRCLE_SEGMENTS + 2];
    private final float[] capsuleY = new float[CIRCLE_SEGMENTS + 2];
    private final b2DebugDraw draw = new b2DebugDraw();
    private int labelCount;
    private float pointSize;

    WorldDrawBatch() {
        draw.DrawPolygonFcn = (vertices, count, color) -> outline(vertices, count, color);
        draw.DrawSolidPolygonFcn = (transform, vertices, count, radius, color) ->
            solidPolygon(transform, vertices, count, color);
        draw.DrawCircleFcn = (center, radius, color) -> circle(center, radius, color, false);
        draw.DrawSolidCircleFcn = (transform, radius, color) -> circle(transform.p, radius, color, true);
        draw.DrawSolidCapsuleFcn = this::capsule;
        draw.DrawSegmentFcn = this::line;
        draw.DrawTransformFcn = transform -> transform(transform, pointSize);
        draw.DrawPointFcn = (point, size, color) -> point(point, Math.max(pointSize, 0.02f), color);
        draw.DrawStringFcn = this::label;
    }

    static WorldDrawBatch capture(b2WorldId worldId, DrawOptions options, float pointSize) {
        WorldDrawBatch batch = new WorldDrawBatch();
        batch.captureInto(worldId, options, pointSize);
        return batch;
    }

    void captureInto(b2WorldId worldId, DrawOptions options, float pointSize) {
        clear();
        this.pointSize = pointSize;
        draw.drawShapes = options.shapes;
        draw.drawJoints = options.joints;
        draw.drawJointExtras = options.jointExtras;
        draw.drawIslands = options.islands;
        draw.drawBounds = options.bounds;
        draw.drawContacts = options.contacts;
        draw.drawGraphColors = options.graphColors;
        draw.drawContactNormals = options.contactNormals;
        draw.drawContactImpulses = options.contactImpulses;
        draw.drawFrictionImpulses = options.frictionImpulses;
        b2World_Draw(worldId, draw);
    }

    void clear() {
        fillVertices.clear();
        fillColors.clear();
        lineVertices.clear();
        lineColors.clear();
        for (int i = 0; i < labelCount; ++i) {
            labels.get(i).text = null;
        }
        labelCount = 0;
        bounds.clear();
    }

    void solidPolygon(b2Transform transform, b2Vec2[] vertices, int count, int color) {
        if (count < 2) {
            return;
        }
        float localX = vertices[0].x;
        float localY = vertices[0].y;
        float firstX = transform.p.x + transform.q.c * localX - transform.q.s * localY;
        float firstY = transform.p.y + transform.q.s * localX + transform.q.c * localY;
        for (int i = 1; i < count - 1; ++i) {
            b2Vec2 second = vertices[i];
            b2Vec2 third = vertices[i + 1];
            float secondX = transform.p.x + transform.q.c * second.x - transform.q.s * second.y;
            float secondY = transform.p.y + transform.q.s * second.x + transform.q.c * second.y;
            float thirdX = transform.p.x + transform.q.c * third.x - transform.q.s * third.y;
            float thirdY = transform.p.y + transform.q.s * third.x + transform.q.c * third.y;
            triangle(firstX, firstY, secondX, secondY, thirdX, thirdY, color);
        }
        for (int i = 0; i < count; ++i) {
            b2Vec2 first = vertices[i];
            b2Vec2 second = vertices[(i + 1) % count];
            float x1 = transform.p.x + transform.q.c * first.x - transform.q.s * first.y;
            float y1 = transform.p.y + transform.q.s * first.x + transform.q.c * first.y;
            float x2 = transform.p.x + transform.q.c * second.x - transform.q.s * second.y;
            float y2 = transform.p.y + transform.q.s * second.x + transform.q.c * second.y;
            addLine(x1, y1, x2, y2, color);
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
        int index = 0;
        for (int i = 0; i <= capSegments; ++i) {
            float a = angle - 0.5f * (float) Math.PI + (float) Math.PI * i / capSegments;
            capsuleX[index] = p2.x + radius * (float) Math.cos(a);
            capsuleY[index] = p2.y + radius * (float) Math.sin(a);
            index += 1;
        }
        for (int i = 0; i <= capSegments; ++i) {
            float a = angle + 0.5f * (float) Math.PI + (float) Math.PI * i / capSegments;
            capsuleX[index] = p1.x + radius * (float) Math.cos(a);
            capsuleY[index] = p1.y + radius * (float) Math.sin(a);
            index += 1;
        }
        float centerX = 0.5f * (p1.x + p2.x);
        float centerY = 0.5f * (p1.y + p2.y);
        for (int i = 0; i < count; ++i) {
            bounds.include(capsuleX[i], capsuleY[i]);
            triangle(centerX, centerY, capsuleX[i], capsuleY[i], capsuleX[(i + 1) % count],
                capsuleY[(i + 1) % count], color);
            addLine(capsuleX[i], capsuleY[i], capsuleX[(i + 1) % count], capsuleY[(i + 1) % count], color);
        }
    }

    private void transform(b2Transform transform, float size) {
        float axis = Math.max(0.25f, 8.0f * size);
        addLine(transform.p.x, transform.p.y, transform.p.x + axis * transform.q.c,
            transform.p.y + axis * transform.q.s, 0xF05252);
        addLine(transform.p.x, transform.p.y, transform.p.x - axis * transform.q.s,
            transform.p.y + axis * transform.q.c, 0x41C47A);
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

    private void label(b2Vec2 point, String text, int color) {
        Label label;
        if (labelCount == labels.size()) {
            label = new Label();
            labels.add(label);
        } else {
            label = labels.get(labelCount);
        }
        label.set(point.x, point.y, text, color);
        labelCount += 1;
        bounds.include(point.x, point.y);
    }

    int labelCount() {
        return labelCount;
    }

    static final class DrawOptions {
        boolean shapes = true;
        boolean joints = true;
        boolean jointExtras;
        boolean islands;
        boolean bounds;
        boolean contacts;
        boolean graphColors;
        boolean contactNormals;
        boolean contactImpulses;
        boolean frictionImpulses;

        DrawOptions() {
        }

        DrawOptions(DrawOptions other) {
            shapes = other.shapes;
            joints = other.joints;
            jointExtras = other.jointExtras;
            islands = other.islands;
            bounds = other.bounds;
            contacts = other.contacts;
            graphColors = other.graphColors;
            contactNormals = other.contactNormals;
            contactImpulses = other.contactImpulses;
            frictionImpulses = other.frictionImpulses;
        }

        boolean sameAs(DrawOptions other) {
            return shapes == other.shapes && joints == other.joints && jointExtras == other.jointExtras
                && islands == other.islands && bounds == other.bounds && contacts == other.contacts
                && graphColors == other.graphColors && contactNormals == other.contactNormals
                && contactImpulses == other.contactImpulses && frictionImpulses == other.frictionImpulses;
        }
    }

    static final class Label {
        float x;
        float y;
        String text;
        int color;

        void set(float x, float y, String text, int color) {
            this.x = x;
            this.y = y;
            this.text = text;
            this.color = color;
        }
    }

    static final class Bounds {
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;

        void clear() {
            minX = Float.POSITIVE_INFINITY;
            minY = Float.POSITIVE_INFINITY;
            maxX = Float.NEGATIVE_INFINITY;
            maxY = Float.NEGATIVE_INFINITY;
        }

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

        void clear() {
            size = 0;
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

        void clear() {
            size = 0;
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
