package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyEvents;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2RevoluteJointDef;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Transform;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;

import static org.box2d4j.B2.*;

public final class FallingHinges {
    private FallingHinges() {
    }

    public static Result run() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        Data data = create(worldId);

        boolean done = false;
        while (!done) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
            done = update(worldId, data);
        }

        b2DestroyWorld(worldId);
        return new Result(data.sleepStep, data.hash);
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }

    private static Data create(b2WorldId worldId) {
        b2BodyDef groundDef = b2DefaultBodyDef();
        groundDef.position = new b2Vec2(0.0f, -1.0f);
        b2BodyId groundId = b2CreateBody(worldId, groundDef);
        b2CreatePolygonShape(groundId, b2DefaultShapeDef(), b2MakeBox(20.0f, 1.0f));

        int columnCount = 4;
        int rowCount = 30;
        b2BodyId[] bodyIds = new b2BodyId[columnCount * rowCount];

        float h = 0.25f;
        float r = 0.1f * h;
        float offset = 0.4f * h;
        float dx = 10.0f * h;
        float xroot = -0.5f * dx * (columnCount - 1.0f);
        b2Polygon box = b2MakeRoundedBox(h - r, h - r, r);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.3f;

        b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
        jointDef.enableLimit = true;
        jointDef.lowerAngle = -0.1f * B2_PI;
        jointDef.upperAngle = 0.2f * B2_PI;
        jointDef.enableSpring = true;
        jointDef.hertz = 0.5f;
        jointDef.dampingRatio = 0.5f;
        jointDef.localAnchorA = new b2Vec2(h, h);
        jointDef.localAnchorB = new b2Vec2(offset, -h);
        jointDef.drawSize = 0.1f;

        int bodyIndex = 0;
        for (int j = 0; j < columnCount; ++j) {
            float x = xroot + j * dx;
            b2BodyId prevBodyId = b2_nullBodyId;
            for (int i = 0; i < rowCount; ++i) {
                b2BodyDef bodyDef = b2DefaultBodyDef();
                bodyDef.type = b2_dynamicBody;
                bodyDef.position = new b2Vec2(x + offset * i, h + 2.0f * h * i);
                bodyDef.rotation = b2MakeRot(0.1f * i - 1.0f);

                b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
                if ((i & 1) == 0) {
                    prevBodyId = bodyId;
                } else {
                    jointDef.bodyIdA = prevBodyId;
                    jointDef.bodyIdB = bodyId;
                    b2CreateRevoluteJoint(worldId, jointDef);
                    prevBodyId = b2_nullBodyId;
                }

                b2CreatePolygonShape(bodyId, shapeDef, box);
                bodyIds[bodyIndex++] = bodyId;
            }
        }

        if (bodyIndex != bodyIds.length) {
            throw new IllegalStateException("falling hinges body count mismatch");
        }
        return new Data(bodyIds);
    }

    private static boolean update(b2WorldId worldId, Data data) {
        if (data.hash == 0) {
            b2BodyEvents bodyEvents = b2World_GetBodyEvents(worldId);
            if (bodyEvents.moveCount == 0) {
                if (b2World_GetAwakeBodyCount(worldId) != 0) {
                    throw new IllegalStateException("falling hinges stopped moving before all bodies slept");
                }

                int hash = B2_HASH_INIT;
                ByteBuffer buffer = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
                for (b2BodyId bodyId : data.bodyIds) {
                    b2Transform transform = b2Body_GetTransform(bodyId);
                    buffer.clear();
                    buffer.putFloat(transform.p.x);
                    buffer.putFloat(transform.p.y);
                    buffer.putFloat(transform.q.c);
                    buffer.putFloat(transform.q.s);
                    hash = b2Hash(hash, buffer.array(), buffer.capacity());
                }

                data.hash = hash;
                data.sleepStep = data.stepCount;
            }
        }

        data.stepCount += 1;
        if (data.stepCount >= 1000) {
            throw new IllegalStateException("falling hinges did not sleep");
        }
        return data.hash != 0;
    }

    private static final class Data {
        final b2BodyId[] bodyIds;
        int stepCount;
        int sleepStep = -1;
        int hash;

        Data(b2BodyId[] bodyIds) {
            this.bodyIds = bodyIds;
        }
    }

    public static final class Result {
        public final int sleepStep;
        public final int hash;

        public Result(int sleepStep, int hash) {
            this.sleepStep = sleepStep;
            this.hash = hash;
        }

        public String toLine() {
            return String.format(Locale.ROOT, "determinism %d %08x", sleepStep, hash);
        }
    }
}
