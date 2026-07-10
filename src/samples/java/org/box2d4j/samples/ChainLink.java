package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2ChainDef;
import org.box2d4j.b2ChainId;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class ChainLink {
    private static final int STEP_COUNT = 240;

    private ChainLink() {
    }

    public static Result run() {
        return run(STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(worldId, bodyDef);

        b2Vec2[] points1 = {
            new b2Vec2(40.0f, 1.0f),
            new b2Vec2(0.0f, 0.0f),
            new b2Vec2(-40.0f, 0.0f),
            new b2Vec2(-40.0f, -1.0f),
            new b2Vec2(0.0f, -1.0f),
            new b2Vec2(40.0f, -1.0f)
        };
        b2Vec2[] points2 = {
            new b2Vec2(-40.0f, -1.0f),
            new b2Vec2(0.0f, -1.0f),
            new b2Vec2(40.0f, -1.0f),
            new b2Vec2(40.0f, 0.0f),
            new b2Vec2(0.0f, 0.0f),
            new b2Vec2(-40.0f, 0.0f)
        };

        b2ChainId chainId1 = createOpenChain(groundId, points1);
        b2ChainId chainId2 = createOpenChain(groundId, points2);

        bodyDef.type = b2_dynamicBody;
        b2ShapeDef shapeDef = b2DefaultShapeDef();

        b2BodyId[] bodies = new b2BodyId[3];
        bodyDef.position = new b2Vec2(-5.0f, 2.0f);
        bodies[0] = b2CreateBody(worldId, bodyDef);
        b2CreateCircleShape(bodies[0], shapeDef, new b2Circle(new b2Vec2(0.0f, 0.0f), 0.5f));

        bodyDef.position = new b2Vec2(0.0f, 2.0f);
        bodies[1] = b2CreateBody(worldId, bodyDef);
        b2CreateCapsuleShape(bodies[1], shapeDef, new b2Capsule(new b2Vec2(-0.5f, 0.0f), new b2Vec2(0.5f, 0.0f), 0.25f));

        bodyDef.position = new b2Vec2(5.0f, 2.0f);
        bodies[2] = b2CreateBody(worldId, bodyDef);
        b2CreatePolygonShape(bodies[2], shapeDef, b2MakeBox(0.5f, 0.5f));

        for (int step = 0; step < stepCount; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        BodyState[] states = new BodyState[bodies.length];
        for (int i = 0; i < bodies.length; ++i) {
            states[i] = bodyState(bodies[i]);
        }
        b2Counters counters = b2World_GetCounters(worldId);
        Result result = new Result(b2Chain_GetSegmentCount(chainId1), b2Chain_GetSegmentCount(chainId2),
            counters.bodyCount, counters.shapeCount, counters.contactCount, b2World_GetAwakeBodyCount(worldId), states);
        b2DestroyWorld(worldId);
        return result;
    }

    private static b2ChainId createOpenChain(b2BodyId bodyId, b2Vec2[] points) {
        b2ChainDef chainDef = b2DefaultChainDef();
        chainDef.points = points;
        chainDef.count = points.length;
        chainDef.isLoop = false;
        return b2CreateChain(bodyId, chainDef);
    }

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Vec2 p = b2Body_GetPosition(bodyId);
        b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(p.x, p.y, b2Rot_GetAngle(b2Body_GetRotation(bodyId)),
            v.x, v.y, b2Body_GetAngularVelocity(bodyId));
    }

    public static void main(String[] args) {
        int stepCount = args.length > 0 ? Integer.parseInt(args[0]) : STEP_COUNT;
        System.out.println(run(stepCount).toLine());
    }

    public static final class Result {
        public final int chainSegmentCount1;
        public final int chainSegmentCount2;
        public final int bodyCount;
        public final int shapeCount;
        public final int contactCount;
        public final int awakeBodyCount;
        public final BodyState[] states;

        public Result(int chainSegmentCount1, int chainSegmentCount2, int bodyCount, int shapeCount, int contactCount,
                      int awakeBodyCount, BodyState[] states) {
            this.chainSegmentCount1 = chainSegmentCount1;
            this.chainSegmentCount2 = chainSegmentCount2;
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.awakeBodyCount = awakeBodyCount;
            this.states = states;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder();
            builder.append("chainLink ")
                .append(chainSegmentCount1).append(' ')
                .append(chainSegmentCount2).append(' ')
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
        int exponent = Math.max(text.indexOf('e'), text.indexOf('E'));
        String suffix = "";
        if (exponent >= 0) {
            suffix = text.substring(exponent);
            text = text.substring(0, exponent);
        }
        if (text.indexOf('.') >= 0) {
            while (text.endsWith("0")) {
                text = text.substring(0, text.length() - 1);
            }
            if (text.endsWith(".")) {
                text = text.substring(0, text.length() - 1);
            }
        }
        return text + suffix;
    }
}
