package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class VerticalStack {
    private static final int ROWS = 12;
    private static final int COLUMNS = 1;
    private static final int STEP_COUNT = 2400;

    private VerticalStack() {
    }

    public static Result run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.position = new b2Vec2(0.0f, 0.0f);
            b2BodyId groundId = b2CreateBody(worldId, bodyDef);

            b2ShapeDef shapeDef = b2DefaultShapeDef();
            b2Segment segment = new b2Segment(new b2Vec2(10.0f, 0.0f), new b2Vec2(10.0f, 20.0f));
            b2CreateSegmentShape(groundId, shapeDef, segment);

            segment = new b2Segment(new b2Vec2(-30.0f, 0.0f), new b2Vec2(30.0f, 0.0f));
            b2CreateSegmentShape(groundId, shapeDef, segment);
        }

        if (SampleRuntime.isActive()) {
            return runInteractive(worldId);
        }

        b2BodyId[] bodies = new b2BodyId[ROWS * COLUMNS];
        b2Polygon box = b2MakeRoundedBox(0.45f, 0.45f, 0.05f);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        shapeDef.material.friction = 0.3f;

        float offset = 0.01f;
        float dx = -3.0f;
        float xroot = 8.0f;

        for (int j = 0; j < COLUMNS; ++j) {
            float x = xroot + j * dx;

            for (int i = 0; i < ROWS; ++i) {
                b2BodyDef bodyDef = b2DefaultBodyDef();
                bodyDef.type = b2_dynamicBody;

                int n = j * ROWS + i;
                float shift = i % 2 == 0 ? -offset : offset;
                bodyDef.position = new b2Vec2(x + shift, 0.5f + 1.0f * i);
                bodies[n] = b2CreateBody(worldId, bodyDef);

                b2CreatePolygonShape(bodies[n], shapeDef, box);
            }
        }

        for (int step = 0; step < STEP_COUNT; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        BodyState[] states = new BodyState[bodies.length];
        for (int i = 0; i < bodies.length; ++i) {
            states[i] = bodyState(bodies[i]);
        }
        b2Counters counters = b2World_GetCounters(worldId);
        int awakeBodyCount = b2World_GetAwakeBodyCount(worldId);

        b2DestroyWorld(worldId);
        return new Result(counters.bodyCount, counters.shapeCount, counters.contactCount, awakeBodyCount, states);
    }

    private static Result runInteractive(b2WorldId worldId) {
        InteractiveScene scene = new InteractiveScene(worldId);
        scene.createStacks();
        SampleRuntime.choice("vertical.shape", "Shape", scene.shapeType, new String[] {"Circle", "Box"}, value -> {
            scene.shapeType = value;
            scene.createStacks();
        });
        SampleRuntime.integer("vertical.rows", "Rows", scene.rows, 1, 15, 1, value -> {
            scene.rows = value;
            scene.createStacks();
        });
        SampleRuntime.integer("vertical.columns", "Columns", scene.columns, 1, 10, 1, value -> {
            scene.columns = value;
            scene.createStacks();
        });
        SampleRuntime.integer("vertical.bullets", "Bullets", scene.bulletCount, 1, 8, 1,
            value -> scene.bulletCount = value);
        SampleRuntime.choice("vertical.bulletShape", "Bullet Shape", scene.bulletType,
            new String[] {"Circle", "Box"}, value -> scene.bulletType = value);
        SampleRuntime.action("vertical.fire", "Fire Bullets", "B", scene::fireBullets);
        SampleRuntime.action("vertical.destroy", "Destroy Body", scene::destroyBody);
        SampleRuntime.action("vertical.reset", "Reset Stack", scene::createStacks);

        for (int step = 0; step < STEP_COUNT; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }
        java.util.List<BodyState> states = new java.util.ArrayList<>();
        for (b2BodyId body : scene.bodies) {
            if (body != null && b2Body_IsValid(body)) {
                states.add(bodyState(body));
            }
        }
        b2Counters counters = b2World_GetCounters(worldId);
        Result result = new Result(counters.bodyCount, counters.shapeCount, counters.contactCount,
            b2World_GetAwakeBodyCount(worldId), states.toArray(new BodyState[0]));
        b2DestroyWorld(worldId);
        return result;
    }

    private static final class InteractiveScene {
        final b2WorldId worldId;
        final b2BodyId[] bodies = new b2BodyId[150];
        final b2BodyId[] bullets = new b2BodyId[8];
        int shapeType = 1;
        int rows = ROWS;
        int columns = COLUMNS;
        int bulletCount = 1;
        int bulletType;

        InteractiveScene(b2WorldId worldId) {
            this.worldId = worldId;
        }

        void createStacks() {
            destroyAll(bodies);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.density = 1.0f;
            shapeDef.material.friction = 0.3f;
            float offset = shapeType == 0 ? 0.0f : 0.01f;
            for (int column = 0; column < columns; ++column) {
                float x = 8.0f - 3.0f * column;
                for (int row = 0; row < rows; ++row) {
                    int index = column * rows + row;
                    b2BodyDef bodyDef = b2DefaultBodyDef();
                    bodyDef.type = b2_dynamicBody;
                    float shift = row % 2 == 0 ? -offset : offset;
                    bodyDef.position = new b2Vec2(x + shift, 0.5f + row);
                    bodies[index] = b2CreateBody(worldId, bodyDef);
                    if (shapeType == 0) {
                        b2CreateCircleShape(bodies[index], shapeDef, new org.box2d4j.b2Circle(new b2Vec2(), 0.5f));
                    } else {
                        b2CreatePolygonShape(bodies[index], shapeDef, b2MakeRoundedBox(0.45f, 0.45f, 0.05f));
                    }
                }
            }
        }

        void fireBullets() {
            destroyAll(bullets);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.density = 4.0f;
            for (int i = 0; i < bulletCount; ++i) {
                b2BodyDef bodyDef = b2DefaultBodyDef();
                bodyDef.type = b2_dynamicBody;
                bodyDef.position = new b2Vec2(-26.7f - i, 6.0f);
                bodyDef.linearVelocity = new b2Vec2(225.0f + 5.0f * i, 0.0f);
                bodyDef.isBullet = true;
                bullets[i] = b2CreateBody(worldId, bodyDef);
                if (bulletType == 0) {
                    b2CreateCircleShape(bullets[i], shapeDef,
                        new org.box2d4j.b2Circle(new b2Vec2(), 0.25f));
                } else {
                    b2CreatePolygonShape(bullets[i], shapeDef, b2MakeBox(0.25f, 0.25f));
                }
            }
        }

        void destroyBody() {
            for (int i = 0; i < bodies.length; ++i) {
                if (bodies[i] != null && b2Body_IsValid(bodies[i])) {
                    b2DestroyBody(bodies[i]);
                    bodies[i] = b2_nullBodyId;
                    return;
                }
            }
        }

        private static void destroyAll(b2BodyId[] bodyIds) {
            for (int i = 0; i < bodyIds.length; ++i) {
                if (bodyIds[i] != null && b2Body_IsValid(bodyIds[i])) {
                    b2DestroyBody(bodyIds[i]);
                }
                bodyIds[i] = b2_nullBodyId;
            }
        }
    }

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Vec2 p = b2Body_GetPosition(bodyId);
        b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(p.x, p.y, b2Rot_GetAngle(b2Body_GetRotation(bodyId)),
            v.x, v.y, b2Body_GetAngularVelocity(bodyId));
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }

    public static final class Result {
        public final int bodyCount;
        public final int shapeCount;
        public final int contactCount;
        public final int awakeBodyCount;
        public final BodyState[] states;

        public Result(int bodyCount, int shapeCount, int contactCount, int awakeBodyCount, BodyState[] states) {
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.awakeBodyCount = awakeBodyCount;
            this.states = states;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder();
            builder.append("verticalStack ")
                .append(bodyCount).append(' ')
                .append(shapeCount).append(' ')
                .append(contactCount).append(' ')
                .append(awakeBodyCount).append(' ')
                .append(states.length);
            for (BodyState state : states) {
                builder.append(state.toLinePart());
            }
            return builder.toString();
        }
    }

    public static final class BodyState {
        public final float x;
        public final float y;
        public final float angle;
        public final float velocityX;
        public final float velocityY;
        public final float angularVelocity;

        BodyState(float x, float y, float angle, float velocityX, float velocityY, float angularVelocity) {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.angularVelocity = angularVelocity;
        }

        String toLinePart() {
            return String.format(Locale.ROOT, " %s %s %s %s %s %s",
                formatFloat(x), formatFloat(y), formatFloat(angle), formatFloat(velocityX), formatFloat(velocityY),
                formatFloat(angularVelocity));
        }
    }

    private static String formatFloat(float value) {
        if (value == 0.0f) {
            return "0";
        }
        String text = String.format(Locale.ROOT, "%.9g", value);
        if (text.indexOf('e') < 0 && text.indexOf('E') < 0 && text.indexOf('.') >= 0) {
            while (text.endsWith("0")) {
                text = text.substring(0, text.length() - 1);
            }
            if (text.endsWith(".")) {
                text = text.substring(0, text.length() - 1);
            }
        }
        return text;
    }
}
