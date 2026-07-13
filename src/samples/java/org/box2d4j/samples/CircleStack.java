package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Circle;
import org.box2d4j.b2ContactEvents;
import org.box2d4j.b2ContactHitEvent;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class CircleStack {
    private static final int DEFAULT_STEP_COUNT = 180;

    private CircleStack() {
    }

    public static Result run() {
        return run(DEFAULT_STEP_COUNT);
    }

    public static Result run(int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2World_SetGravity(worldId, new b2Vec2(0.0f, -20.0f));
        b2World_SetContactTuning(worldId, 0.25f * 360.0f, 10.0f, 3.0f);

        int shapeIndex = 0;
        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            b2BodyId groundId = b2CreateBody(worldId, bodyDef);

            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.userData = shapeIndex;
            shapeIndex += 1;

            b2Segment segment = new b2Segment(new b2Vec2(-10.0f, 0.0f), new b2Vec2(10.0f, 0.0f));
            b2CreateSegmentShape(groundId, shapeDef, segment);
        }

        b2BodyId[] bodies = new b2BodyId[10];
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;

        b2Circle circle = new b2Circle();
        circle.radius = 0.5f;

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.enableHitEvents = true;
        shapeDef.material.friction = 0.0f;

        float y = 0.75f;
        for (int i = 0; i < 10; ++i) {
            bodyDef.position.y = y;
            bodies[i] = b2CreateBody(worldId, bodyDef);

            shapeDef.userData = shapeIndex;
            shapeDef.density = 1.0f + 4.0f * i;
            shapeIndex += 1;
            b2CreateCircleShape(bodies[i], shapeDef, circle);

            y += 1.25f;
        }

        int totalHits = 0;
        int pairChecksum = 0;
        float speedSum = 0.0f;
        for (int step = 0; step < stepCount; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
            b2ContactEvents events = b2World_GetContactEvents(worldId);
            totalHits += events.hitCount;
            for (int i = 0; i < events.hitCount; ++i) {
                b2ContactHitEvent event = events.hitEvents[i];
                int indexA = (Integer) b2Shape_GetUserData(event.shapeIdA);
                int indexB = (Integer) b2Shape_GetUserData(event.shapeIdB);
                pairChecksum += (indexA + 1) * 31 + (indexB + 1) * 17;
                speedSum += event.approachSpeed;
            }
        }

        b2Vec2 p0 = b2Body_GetPosition(bodies[0]);
        b2Vec2 v0 = b2Body_GetLinearVelocity(bodies[0]);
        b2Vec2 p9 = b2Body_GetPosition(bodies[9]);
        b2Vec2 v9 = b2Body_GetLinearVelocity(bodies[9]);
        b2Counters counters = b2World_GetCounters(worldId);
        int awakeBodyCount = b2World_GetAwakeBodyCount(worldId);
        float angle0 = b2Rot_GetAngle(b2Body_GetRotation(bodies[0]));
        float angle9 = b2Rot_GetAngle(b2Body_GetRotation(bodies[9]));

        b2DestroyWorld(worldId);
        return new Result(totalHits, pairChecksum, speedSum, counters.contactCount, awakeBodyCount,
            p0.x, p0.y, angle0, v0.x, p9.x, p9.y, angle9, v9.y);
    }

    public static void main(String[] args) {
        int stepCount = args.length == 0 ? DEFAULT_STEP_COUNT : Integer.parseInt(args[0]);
        System.out.println(run(stepCount).toLine());
    }

    public static final class Result {
        public final int totalHits;
        public final int pairChecksum;
        public final float speedSum;
        public final int contactCount;
        public final int awakeBodyCount;
        public final float bottomX;
        public final float bottomY;
        public final float bottomAngle;
        public final float bottomVelocityX;
        public final float topX;
        public final float topY;
        public final float topAngle;
        public final float topVelocityY;

        public Result(int totalHits, int pairChecksum, float speedSum, int contactCount, int awakeBodyCount,
                      float bottomX, float bottomY, float bottomAngle, float bottomVelocityX,
                      float topX, float topY, float topAngle, float topVelocityY) {
            this.totalHits = totalHits;
            this.pairChecksum = pairChecksum;
            this.speedSum = speedSum;
            this.contactCount = contactCount;
            this.awakeBodyCount = awakeBodyCount;
            this.bottomX = bottomX;
            this.bottomY = bottomY;
            this.bottomAngle = bottomAngle;
            this.bottomVelocityX = bottomVelocityX;
            this.topX = topX;
            this.topY = topY;
            this.topAngle = topAngle;
            this.topVelocityY = topVelocityY;
        }

        public String toLine() {
            return String.format(Locale.ROOT, "circleStack %d %d %s %d %d %s %s %s %s %s %s %s %s",
                totalHits, pairChecksum, formatFloat(speedSum), contactCount, awakeBodyCount,
                formatFloat(bottomX), formatFloat(bottomY), formatFloat(bottomAngle), formatFloat(bottomVelocityX),
                formatFloat(topX), formatFloat(topY), formatFloat(topAngle), formatFloat(topVelocityY));
        }

        private static String formatFloat(float value) {
            if (value == 0.0f) {
                return "0";
            }
            return String.format(Locale.ROOT, "%.9g", value);
        }
    }
}
