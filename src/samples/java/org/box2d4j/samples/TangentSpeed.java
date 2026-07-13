package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2ChainDef;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Counters;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2SurfaceMaterial;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.box2d4j.B2.*;

public final class TangentSpeed {
    private static final int TOTAL_COUNT = 200;
    private static final int STEP_COUNT = 240;
    private static final int[] SAMPLE_INDICES = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    private static final String PATH = "m 613.8334,185.20833 -42.33338,0 h -37.04166 l -34.39581,0 -29.10417,-2.64583 -26.45834,-7.9375 "
        + "-26.45833,-13.22917 -23.81251,-21.16666 h -13.22916 v 44.97916 H 68.791712 V 0 h -21.16671 v "
        + "206.375 l 566.208398,-1e-5 z";

    private TangentSpeed() {
    }

    public static Result run() {
        return run(STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2Vec2[] points = b2Vec2.array(20);
        int pointCount;

        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            b2BodyId groundId = b2CreateBody(worldId, bodyDef);

            b2Vec2 offset = new b2Vec2(-47.375002f, 0.25f);
            pointCount = parsePath(PATH, offset, points, points.length, 0.2f);

            b2SurfaceMaterial[] materials = new b2SurfaceMaterial[20];
            for (int i = 0; i < materials.length; ++i) {
                materials[i] = new b2SurfaceMaterial();
                materials[i].friction = 0.6f;
            }

            materials[0].tangentSpeed = -10.0f;
            materials[0].customColor = b2_colorDarkBlue;
            materials[1].tangentSpeed = -20.0f;
            materials[1].customColor = b2_colorDarkCyan;
            materials[2].tangentSpeed = -30.0f;
            materials[2].customColor = b2_colorDarkGoldenRod;
            materials[3].tangentSpeed = -40.0f;
            materials[3].customColor = b2_colorDarkGray;
            materials[4].tangentSpeed = -50.0f;
            materials[4].customColor = b2_colorDarkGreen;
            materials[5].tangentSpeed = -60.0f;
            materials[5].customColor = b2_colorDarkKhaki;
            materials[6].tangentSpeed = -70.0f;
            materials[6].customColor = b2_colorDarkMagenta;

            b2ChainDef chainDef = b2DefaultChainDef();
            chainDef.points = points;
            chainDef.count = pointCount;
            chainDef.isLoop = true;
            chainDef.materials = materials;
            chainDef.materialCount = pointCount;

            b2CreateChain(groundId, chainDef);
        }

        List<b2BodyId> bodies = new ArrayList<>();
        float[] material = {0.6f, 0.3f};
        int[] runtimeStep = {0};
        boolean interactive = SampleRuntime.isActive();
        Runnable reset = () -> {
            for (b2BodyId body : bodies) {
                if (b2Body_IsValid(body)) {
                    b2DestroyBody(body);
                }
            }
            bodies.clear();
            runtimeStep[0] = 0;
        };
        SampleRuntime.slider("tangent.friction", "Friction", material[0], 0.0f, 2.0f, 0.01f, value -> {
            material[0] = value;
            reset.run();
        });
        SampleRuntime.slider("tangent.rolling", "Rolling Resistance", material[1], 0.0f, 1.0f, 0.01f, value -> {
            material[1] = value;
            reset.run();
        });
        SampleRuntime.beforeStep(() -> dropScheduledBall(worldId, bodies, material, runtimeStep[0]++));
        for (int step = 0; step < stepCount; ++step) {
            if (!interactive) {
                dropScheduledBall(worldId, bodies, material, step);
            }
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        List<BodyState> stateList = new ArrayList<>();
        for (int index : SAMPLE_INDICES) {
            if (index < bodies.size()) {
                stateList.add(bodyState(bodies.get(index)));
            }
        }
        BodyState[] states = stateList.toArray(new BodyState[0]);
        b2Counters counters = b2World_GetCounters(worldId);
        int awakeBodyCount = b2World_GetAwakeBodyCount(worldId);

        b2DestroyWorld(worldId);
        return new Result(points, pointCount, counters.bodyCount, counters.shapeCount, counters.contactCount, awakeBodyCount,
            bodies.size(), states);
    }

    private static void dropScheduledBall(b2WorldId worldId, List<b2BodyId> bodies, float[] material, int step) {
        if (step % 25 == 0 && bodies.size() < TOTAL_COUNT) {
            bodies.add(dropBall(worldId, material[0], material[1]));
        }
    }

    private static b2BodyId dropBall(b2WorldId worldId, float friction, float rollingResistance) {
        b2Circle circle = new b2Circle(b2Vec2_zero, 0.5f);

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(110.0f, -30.0f);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = friction;
        shapeDef.material.rollingResistance = rollingResistance;
        b2CreateCircleShape(bodyId, shapeDef, circle);
        return bodyId;
    }

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Vec2 p = b2Body_GetPosition(bodyId);
        b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(p.x, p.y, b2Rot_GetAngle(b2Body_GetRotation(bodyId)),
            v.x, v.y, b2Body_GetAngularVelocity(bodyId));
    }

    static int parsePath(String svgPath, b2Vec2 offset, b2Vec2[] points, int capacity, float scale) {
        int pointCount = 0;
        b2Vec2 currentPoint = new b2Vec2();
        int ptr = 0;
        char command = svgPath.charAt(ptr);

        while (ptr < svgPath.length()) {
            char ch = svgPath.charAt(ptr);
            if (!isDigit(ch) && ch != '-') {
                command = ch;

                if (command == 'M' || command == 'L' || command == 'H' || command == 'V'
                    || command == 'm' || command == 'l' || command == 'h' || command == 'v') {
                    ptr += 2;
                }

                if (command == 'z') {
                    break;
                }
            }

            float x = 0.0f;
            float y = 0.0f;
            switch (command) {
                case 'M':
                case 'L':
                    x = parseFloat(svgPath, ptr);
                    y = parseFloat(svgPath, nextAfterNumber(svgPath, ptr) + 1);
                    currentPoint.x = x;
                    currentPoint.y = y;
                    break;
                case 'H':
                    x = parseFloat(svgPath, ptr);
                    currentPoint.x = x;
                    break;
                case 'V':
                    y = parseFloat(svgPath, ptr);
                    currentPoint.y = y;
                    break;
                case 'm':
                case 'l':
                    x = parseFloat(svgPath, ptr);
                    y = parseFloat(svgPath, nextAfterNumber(svgPath, ptr) + 1);
                    currentPoint.x += x;
                    currentPoint.y += y;
                    break;
                case 'h':
                    x = parseFloat(svgPath, ptr);
                    currentPoint.x += x;
                    break;
                case 'v':
                    y = parseFloat(svgPath, ptr);
                    currentPoint.y += y;
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported SVG path command: " + command);
            }

            points[pointCount].set(scale * (currentPoint.x + offset.x), -scale * (currentPoint.y + offset.y));
            pointCount += 1;
            if (pointCount == capacity) {
                break;
            }

            while (ptr < svgPath.length() && !Character.isWhitespace(svgPath.charAt(ptr))) {
                ptr++;
            }
            while (ptr < svgPath.length() && Character.isWhitespace(svgPath.charAt(ptr))) {
                ptr++;
            }
        }

        return pointCount;
    }

    private static float parseFloat(String text, int start) {
        int end = nextAfterNumber(text, start);
        return Float.parseFloat(text.substring(start, end));
    }

    private static int nextAfterNumber(String text, int start) {
        int end = start;
        while (end < text.length() && !Character.isWhitespace(text.charAt(end)) && text.charAt(end) != ',') {
            end++;
        }
        return end;
    }

    private static boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    public static void main(String[] args) {
        int stepCount = args.length == 0 ? STEP_COUNT : Integer.parseInt(args[0]);
        System.out.println(run(stepCount).toLine());
    }

    public static final class Result {
        public final b2Vec2[] points;
        public final int pointCount;
        public final int bodyCount;
        public final int shapeCount;
        public final int contactCount;
        public final int awakeBodyCount;
        public final int droppedBodyCount;
        public final BodyState[] states;

        public Result(b2Vec2[] points, int pointCount, int bodyCount, int shapeCount, int contactCount, int awakeBodyCount,
                      int droppedBodyCount, BodyState[] states) {
            this.points = points;
            this.pointCount = pointCount;
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.awakeBodyCount = awakeBodyCount;
            this.droppedBodyCount = droppedBodyCount;
            this.states = states;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder();
            builder.append("tangentSpeed ")
                .append(pointCount).append(' ')
                .append(bodyCount).append(' ')
                .append(shapeCount).append(' ')
                .append(contactCount).append(' ')
                .append(awakeBodyCount).append(' ')
                .append(droppedBodyCount).append(' ')
                .append(states.length);
            for (int i = 0; i < pointCount; ++i) {
                appendVec(builder, points[i]);
            }
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

    private static void appendVec(StringBuilder builder, b2Vec2 v) {
        builder.append(' ').append(formatFloat(v.x)).append(' ').append(formatFloat(v.y));
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
