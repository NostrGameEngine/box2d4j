package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2Counters;
import org.box2d4j.b2MassData;
import org.box2d4j.b2Segment;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class BadBody {
    private BadBody() {
    }

    public static Result run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            b2BodyId groundId = b2CreateBody(worldId, bodyDef);

            b2Segment segment = new b2Segment(new b2Vec2(-20.0f, 0.0f), new b2Vec2(20.0f, 0.0f));
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            b2CreateSegmentShape(groundId, shapeDef, segment);
        }

        b2BodyId badBodyId;
        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = new b2Vec2(0.0f, 3.0f);
            bodyDef.angularVelocity = 0.5f;
            bodyDef.rotation = b2MakeRot(0.25f * B2_PI);
            badBodyId = b2CreateBody(worldId, bodyDef);

            b2Capsule capsule = new b2Capsule(new b2Vec2(0.0f, -1.0f), new b2Vec2(0.0f, 1.0f), 1.0f);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.density = 0.0f;
            b2CreateCapsuleShape(badBodyId, shapeDef, capsule);
        }

        b2BodyId normalBodyId;
        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = new b2Vec2(2.0f, 3.0f);
            bodyDef.rotation = b2MakeRot(0.25f * B2_PI);
            normalBodyId = b2CreateBody(worldId, bodyDef);

            b2Capsule capsule = new b2Capsule(new b2Vec2(0.0f, -1.0f), new b2Vec2(0.0f, 1.0f), 1.0f);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            b2CreateCapsuleShape(normalBodyId, shapeDef, capsule);
        }

        for (int step = 0; step < 120; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
            b2Body_ApplyForceToCenter(badBodyId, new b2Vec2(0.0f, 10.0f), true);
        }

        b2Counters counters = b2World_GetCounters(worldId);
        int awakeBodyCount = b2World_GetAwakeBodyCount(worldId);
        Result result = new Result(counters.contactCount, awakeBodyCount, bodyState(badBodyId), bodyState(normalBodyId));
        b2DestroyWorld(worldId);
        return result;
    }

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Vec2 p = b2Body_GetPosition(bodyId);
        b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
        b2MassData massData = b2Body_GetMassData(bodyId);
        return new BodyState(p.x, p.y, b2Rot_GetAngle(b2Body_GetRotation(bodyId)),
            v.x, v.y, b2Body_GetAngularVelocity(bodyId), massData.mass, massData.center.y,
            massData.rotationalInertia);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }

    public static final class Result {
        public final int contactCount;
        public final int awakeBodyCount;
        public final BodyState badBody;
        public final BodyState normalBody;

        public Result(int contactCount, int awakeBodyCount, BodyState badBody, BodyState normalBody) {
            this.contactCount = contactCount;
            this.awakeBodyCount = awakeBodyCount;
            this.badBody = badBody;
            this.normalBody = normalBody;
        }

        public String toLine() {
            return "badBody " + contactCount + ' ' + awakeBodyCount + badBody.toLinePart() + normalBody.toLinePart();
        }
    }

    public static final class BodyState {
        public final float x;
        public final float y;
        public final float angle;
        public final float velocityX;
        public final float velocityY;
        public final float angularVelocity;
        public final float mass;
        public final float centerY;
        public final float rotationalInertia;

        BodyState(float x, float y, float angle, float velocityX, float velocityY, float angularVelocity,
                  float mass, float centerY, float rotationalInertia) {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.angularVelocity = angularVelocity;
            this.mass = mass;
            this.centerY = centerY;
            this.rotationalInertia = rotationalInertia;
        }

        String toLinePart() {
            return String.format(Locale.ROOT, " %s %s %s %s %s %s %s %s %s",
                formatFloat(x), formatFloat(y), formatFloat(angle), formatFloat(velocityX), formatFloat(velocityY),
                formatFloat(angularVelocity), formatFloat(mass), formatFloat(centerY), formatFloat(rotationalInertia));
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
