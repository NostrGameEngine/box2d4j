package org.box2d4j.debugger;

import org.box2d4j.b2Polygon;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;
import org.box2d4j.samples.ConvexHull;
import org.box2d4j.samples.DynamicTreeSample;
import org.box2d4j.samples.ManifoldSample;
import org.box2d4j.samples.RayCast;
import org.box2d4j.samples.ShapeCast;
import org.box2d4j.samples.ShapeDistance;
import org.box2d4j.samples.SmoothManifold;
import org.box2d4j.samples.TimeOfImpact;

import static org.box2d4j.B2.b2MakeRot;
import static org.box2d4j.B2.b2MakeSquare;

final class SampleResultDraw {
    private static final int GREEN = 0x98FB98;
    private static final int PINK = 0xFFC0CB;
    private static final int CYAN = 0x40E0D0;
    private static final int AMBER = 0xFFD166;
    private static final int WHITE = 0xF4F7F5;
    private static final int MUTED = 0x879592;

    private SampleResultDraw() {
    }

    static WorldDrawBatch capture(Object result) {
        if (result == null) {
            return null;
        }
        WorldDrawBatch batch = new WorldDrawBatch();
        if (result instanceof ConvexHull.Result) {
            drawConvexHull(batch, (ConvexHull.Result) result);
        } else if (result instanceof ShapeDistance.Result) {
            drawShapeDistance(batch, (ShapeDistance.Result) result);
        } else if (result instanceof RayCast.Result) {
            drawRayCast(batch, (RayCast.Result) result);
        } else if (result instanceof TimeOfImpact.Result) {
            drawTimeOfImpact(batch, (TimeOfImpact.Result) result);
        } else if (result instanceof ShapeCast.Result) {
            drawShapeCast(batch, (ShapeCast.Result) result);
        } else if (result instanceof SmoothManifold.Result) {
            drawSmoothManifold(batch, (SmoothManifold.Result) result);
        } else if (result instanceof ManifoldSample.Result) {
            drawManifolds(batch, (ManifoldSample.Result) result);
        } else if (result instanceof DynamicTreeSample.Result) {
            drawDynamicTree(batch, (DynamicTreeSample.Result) result);
        } else {
            return null;
        }
        return batch.bounds.isValid() ? batch : null;
    }

    private static void drawConvexHull(WorldDrawBatch batch, ConvexHull.Result result) {
        for (b2Vec2 point : result.points) {
            batch.point(point, 0.08f, AMBER);
        }
        polygon(batch, result.hullPoints, result.hullCount, GREEN, true);
    }

    private static void drawShapeDistance(WorldDrawBatch batch, ShapeDistance.Result result) {
        for (int i = 0; i < result.cases.length; ++i) {
            ShapeDistance.CaseResult value = result.cases[i];
            float offsetX = 4.0f * i;
            drawDistanceShape(batch, value.typeA, value.radiusA,
                new b2Transform(new b2Vec2(offsetX, 0.0f), b2MakeRot(0.0f)), GREEN);
            drawDistanceShape(batch, value.typeB, value.radiusB,
                new b2Transform(new b2Vec2(offsetX + value.position.x, value.position.y), b2MakeRot(value.angle)),
                PINK);
            b2Vec2 pointA = new b2Vec2(offsetX + value.output.pointA.x, value.output.pointA.y);
            b2Vec2 pointB = new b2Vec2(offsetX + value.output.pointB.x, value.output.pointB.y);
            batch.line(pointA, pointB, CYAN);
            batch.point(pointA, 0.06f, WHITE);
            batch.point(pointB, 0.06f, WHITE);
        }
    }

    private static void drawDistanceShape(WorldDrawBatch batch, ShapeDistance.ShapeType type, float radius,
                                          b2Transform transform, int color) {
        if (type == ShapeDistance.ShapeType.POINT) {
            batch.circle(transform.p, Math.max(0.05f, radius), color, true);
        } else if (type == ShapeDistance.ShapeType.SEGMENT) {
            b2Vec2 p1 = transformPoint(transform, new b2Vec2(-0.5f, 0.0f));
            b2Vec2 p2 = transformPoint(transform, new b2Vec2(0.5f, 0.0f));
            if (radius > 0.0f) {
                batch.capsule(p1, p2, radius, color);
            } else {
                batch.line(p1, p2, color);
            }
        } else if (type == ShapeDistance.ShapeType.TRIANGLE) {
            b2Vec2[] triangle = {
                new b2Vec2(-0.5f, 0.0f), new b2Vec2(0.5f, 0.0f), new b2Vec2(0.0f, 1.0f)
            };
            batch.solidPolygon(transform, triangle, triangle.length, color);
        } else {
            b2Polygon square = b2MakeSquare(0.5f);
            batch.solidPolygon(transform, square.vertices, square.count, color);
        }
    }

    private static void drawRayCast(WorldDrawBatch batch, RayCast.Result result) {
        b2Transform transform = result.transform;
        batch.circle(transformPoint(transform, new b2Vec2(-20.0f, 20.0f)), 2.0f, GREEN, true);
        batch.capsule(transformPoint(transform, new b2Vec2(-11.0f, 21.0f)),
            transformPoint(transform, new b2Vec2(-9.0f, 19.0f)), 1.5f, PINK);
        b2Polygon square = b2MakeSquare(2.0f);
        batch.solidPolygon(new b2Transform(transformPoint(transform, new b2Vec2(0.0f, 20.0f)), transform.q),
            square.vertices, square.count, CYAN);
        b2Vec2[] triangle = {new b2Vec2(-2.0f, 0.0f), new b2Vec2(2.0f, 0.0f), new b2Vec2(2.0f, 3.0f)};
        batch.solidPolygon(new b2Transform(transformPoint(transform, new b2Vec2(10.0f, 20.0f)), transform.q),
            triangle, triangle.length, AMBER);
        batch.line(transformPoint(transform, new b2Vec2(17.0f, 20.0f)),
            transformPoint(transform, new b2Vec2(23.0f, 20.0f)), WHITE);
        batch.line(result.rayStart, result.rayEnd, MUTED);
        RayCast.OutputState hit = result.closestOutput;
        if (hit.hit) {
            b2Vec2 point = new b2Vec2(hit.pointX, hit.pointY);
            batch.line(result.rayStart, point, AMBER);
            batch.point(point, 0.2f, WHITE);
            batch.line(point, new b2Vec2(point.x + hit.normalX, point.y + hit.normalY), CYAN);
        }
    }

    private static void drawTimeOfImpact(WorldDrawBatch batch, TimeOfImpact.Result result) {
        rectangle(batch, -16.25f, 44.75f, -15.75f, 45.25f, GREEN, true);
        segment(batch, result.start.x1, result.start.y1, result.start.x2, result.start.y2, MUTED);
        segment(batch, result.end.x1, result.end.y1, result.end.x2, result.end.y2, PINK);
        segment(batch, result.hit.x1, result.hit.y1, result.hit.x2, result.hit.y2, AMBER);
        batch.point(new b2Vec2(result.hit.x1, result.hit.y1), 0.015f, WHITE);
        batch.point(new b2Vec2(result.hit.x2, result.hit.y2), 0.015f, WHITE);
    }

    private static void drawShapeCast(WorldDrawBatch batch, ShapeCast.Result result) {
        drawDistanceShape(batch, result.typeA, result.radiusA,
            new b2Transform(new b2Vec2(), b2MakeRot(0.0f)), GREEN);
        b2Vec2 start = result.startTransform.p;
        b2Vec2 end = new b2Vec2(start.x + result.translation.x, start.y + result.translation.y);
        b2Transform hitTransform = new b2Transform(new b2Vec2(result.transformX, result.transformY),
            new org.box2d4j.b2Rot(result.transformCos, result.transformSin));
        b2Vec2 hit = hitTransform.p;
        batch.line(start, end, MUTED);
        drawDistanceShape(batch, result.typeB, result.radiusB, result.startTransform, PINK);
        drawDistanceShape(batch, result.typeB, result.radiusB,
            new b2Transform(end, result.startTransform.q), PINK);
        drawDistanceShape(batch, result.typeB, result.radiusB, hitTransform, AMBER);
        b2Vec2 point = new b2Vec2(result.pointX, result.pointY);
        batch.point(point, 0.04f, WHITE);
        batch.line(point, new b2Vec2(point.x + result.normalX * 0.4f, point.y + result.normalY * 0.4f), CYAN);
    }

    private static void drawSmoothManifold(WorldDrawBatch batch, SmoothManifold.Result result) {
        b2Vec2[] chain = smoothChain();
        for (int i = 0; i < chain.length; ++i) {
            batch.line(chain[i], chain[(i + 1) % chain.length], GREEN);
        }
        for (SmoothManifold.CaseResult value : result.cases) {
            if (value.shapeType == SmoothManifold.CIRCLE_SHAPE) {
                batch.circle(new b2Vec2(value.x, value.y), 0.5f, CYAN, true);
            } else {
                b2Polygon square = b2MakeSquare(0.5f);
                batch.solidPolygon(new b2Transform(new b2Vec2(value.x, value.y), b2MakeRot(value.angle)),
                    square.vertices, square.count, value.round > 0.0f ? PINK : AMBER);
            }
            for (SmoothManifold.ManifoldState manifold : value.manifolds) {
                for (SmoothManifold.PointState point : manifold.points) {
                    contact(batch, point.pointX, point.pointY, manifold.normalX, manifold.normalY);
                }
            }
        }
    }

    private static void drawManifolds(WorldDrawBatch batch, ManifoldSample.Result result) {
        ManifoldSample.PassResult pass = result.passes[result.passes.length - 1];
        for (ManifoldSample.ManifoldState manifold : pass.manifolds) {
            for (ManifoldSample.PointState point : manifold.points) {
                contact(batch, point.pointX, point.pointY, manifold.normalX, manifold.normalY);
            }
        }
    }

    private static void drawDynamicTree(WorldDrawBatch batch, DynamicTreeSample.Result result) {
        DynamicTreeSample.VariantResult value = result.variants[0];
        rectangle(batch, value.lowerX, value.lowerY, value.upperX, value.upperY, MUTED, false);
        rectangle(batch, value.queryLower.x, value.queryLower.y, value.queryUpper.x, value.queryUpper.y,
            CYAN, false);
        batch.line(value.rayStart, value.rayEnd, AMBER);
        for (DynamicTreeSample.ProxyState proxy : value.representatives) {
            rectangle(batch, proxy.fatLowerX, proxy.fatLowerY, proxy.fatUpperX, proxy.fatUpperY, MUTED, false);
            rectangle(batch, proxy.boxLowerX, proxy.boxLowerY, proxy.boxUpperX, proxy.boxUpperY,
                proxy.moved ? PINK : GREEN, true);
        }
    }

    private static void contact(WorldDrawBatch batch, float x, float y, float normalX, float normalY) {
        b2Vec2 point = new b2Vec2(x, y);
        batch.point(point, 0.08f, WHITE);
        batch.line(point, new b2Vec2(x + 0.4f * normalX, y + 0.4f * normalY), CYAN);
    }

    private static void polygon(WorldDrawBatch batch, b2Vec2[] vertices, int count, int color, boolean solid) {
        if (count < 2) {
            return;
        }
        if (solid && count >= 3) {
            batch.solidPolygon(new b2Transform(new b2Vec2(), b2MakeRot(0.0f)), vertices, count, color);
            return;
        }
        for (int i = 0; i < count; ++i) {
            batch.line(vertices[i], vertices[(i + 1) % count], color);
        }
    }

    private static void rectangle(WorldDrawBatch batch, float minX, float minY, float maxX, float maxY,
                                  int color, boolean solid) {
        b2Vec2[] vertices = {
            new b2Vec2(minX, minY), new b2Vec2(maxX, minY),
            new b2Vec2(maxX, maxY), new b2Vec2(minX, maxY)
        };
        polygon(batch, vertices, vertices.length, color, solid);
    }

    private static void segment(WorldDrawBatch batch, float x1, float y1, float x2, float y2, int color) {
        batch.line(new b2Vec2(x1, y1), new b2Vec2(x2, y2), color);
    }

    private static b2Vec2 transformPoint(b2Transform transform, b2Vec2 point) {
        return new b2Vec2(transform.p.x + transform.q.c * point.x - transform.q.s * point.y,
            transform.p.y + transform.q.s * point.x + transform.q.c * point.y);
    }

    private static b2Vec2[] smoothChain() {
        return new b2Vec2[] {
            new b2Vec2(-20.58325f, 14.54175f), new b2Vec2(-21.90625f, 15.8645f),
            new b2Vec2(-24.552f, 17.1875f), new b2Vec2(-27.198f, 11.89575f),
            new b2Vec2(-29.84375f, 15.8645f), new b2Vec2(-29.84375f, 21.15625f),
            new b2Vec2(-25.875f, 23.802f), new b2Vec2(-20.58325f, 25.125f),
            new b2Vec2(-25.875f, 29.09375f), new b2Vec2(-20.58325f, 31.7395f),
            new b2Vec2(-11.009f, 23.229f), new b2Vec2(-8.677f, 21.15625f),
            new b2Vec2(-6.03125f, 21.15625f), new b2Vec2(-7.35425f, 29.09375f),
            new b2Vec2(-3.3855f, 29.09375f), new b2Vec2(1.90625f, 30.41675f),
            new b2Vec2(5.875f, 17.1875f), new b2Vec2(11.16675f, 25.125f),
            new b2Vec2(9.84375f, 29.09375f), new b2Vec2(13.8125f, 31.7395f),
            new b2Vec2(21.75f, 30.41675f), new b2Vec2(28.3645f, 26.448f),
            new b2Vec2(25.71875f, 18.5105f), new b2Vec2(24.39575f, 13.21875f),
            new b2Vec2(17.78125f, 11.89575f), new b2Vec2(15.1355f, 7.927f),
            new b2Vec2(5.875f, 9.25f), new b2Vec2(1.90625f, 11.89575f),
            new b2Vec2(-3.25f, 11.89575f), new b2Vec2(-3.25f, 9.9375f),
            new b2Vec2(-4.70825f, 9.25f), new b2Vec2(-8.677f, 9.25f),
            new b2Vec2(-11.323f, 11.89575f), new b2Vec2(-13.96875f, 11.89575f),
            new b2Vec2(-15.29175f, 14.54175f), new b2Vec2(-19.2605f, 14.54175f)
        };
    }
}
