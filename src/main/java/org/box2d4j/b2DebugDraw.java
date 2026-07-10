package org.box2d4j;

public class b2DebugDraw {
    @FunctionalInterface
    public interface DrawPolygonFcn {
        void invoke(b2Vec2[] vertices, int vertexCount, int color);
    }

    @FunctionalInterface
    public interface DrawSolidPolygonFcn {
        void invoke(b2Transform transform, b2Vec2[] vertices, int vertexCount, float radius, int color);
    }

    @FunctionalInterface
    public interface DrawCircleFcn {
        void invoke(b2Vec2 center, float radius, int color);
    }

    @FunctionalInterface
    public interface DrawSolidCircleFcn {
        void invoke(b2Transform transform, float radius, int color);
    }

    @FunctionalInterface
    public interface DrawSolidCapsuleFcn {
        void invoke(b2Vec2 p1, b2Vec2 p2, float radius, int color);
    }

    @FunctionalInterface
    public interface DrawSegmentFcn {
        void invoke(b2Vec2 p1, b2Vec2 p2, int color);
    }

    @FunctionalInterface
    public interface DrawTransformFcn {
        void invoke(b2Transform transform);
    }

    @FunctionalInterface
    public interface DrawPointFcn {
        void invoke(b2Vec2 p, float size, int color);
    }

    @FunctionalInterface
    public interface DrawStringFcn {
        void invoke(b2Vec2 p, String text, int color);
    }

    public DrawPolygonFcn DrawPolygonFcn = (vertices, vertexCount, color) -> {};
    public DrawSolidPolygonFcn DrawSolidPolygonFcn = (transform, vertices, vertexCount, radius, color) -> {};
    public DrawCircleFcn DrawCircleFcn = (center, radius, color) -> {};
    public DrawSolidCircleFcn DrawSolidCircleFcn = (transform, radius, color) -> {};
    public DrawSolidCapsuleFcn DrawSolidCapsuleFcn = (p1, p2, radius, color) -> {};
    public DrawSegmentFcn DrawSegmentFcn = (p1, p2, color) -> {};
    public DrawTransformFcn DrawTransformFcn = transform -> {};
    public DrawPointFcn DrawPointFcn = (p, size, color) -> {};
    public DrawStringFcn DrawStringFcn = (p, text, color) -> {};

    public b2AABB drawingBounds = new b2AABB();
    public boolean useDrawingBounds;
    public boolean drawShapes;
    public boolean drawJoints;
    public boolean drawJointExtras;
    public boolean drawBounds;
    public boolean drawMass;
    public boolean drawBodyNames;
    public boolean drawContacts;
    public boolean drawGraphColors;
    public boolean drawContactNormals;
    public boolean drawContactImpulses;
    public boolean drawContactFeatures;
    public boolean drawFrictionImpulses;
    public boolean drawIslands;
    public Object context;
}
