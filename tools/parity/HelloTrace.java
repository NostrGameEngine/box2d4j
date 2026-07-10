package org.box2d4j.tools.parity;

import org.box2d4j.B2;
import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Counters;
import org.box2d4j.b2ContactData;
import org.box2d4j.b2Manifold;
import org.box2d4j.b2ManifoldPoint;
import org.box2d4j.b2Rot;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldDef;
import org.box2d4j.b2WorldId;

public final class HelloTrace {
    private HelloTrace() {
    }

    public static void main(String[] args) {
        b2WorldDef worldDef = B2.b2DefaultWorldDef();
        worldDef.gravity = new b2Vec2(0.0f, -10.0f);
        b2WorldId worldId = B2.b2CreateWorld(worldDef);

        b2BodyDef groundBodyDef = B2.b2DefaultBodyDef();
        groundBodyDef.position = new b2Vec2(0.0f, -10.0f);
        b2BodyId groundId = B2.b2CreateBody(worldId, groundBodyDef);
        B2.b2CreatePolygonShape(groundId, B2.b2DefaultShapeDef(), B2.b2MakeBox(50.0f, 10.0f));

        b2BodyDef bodyDef = B2.b2DefaultBodyDef();
        bodyDef.type = B2.b2_dynamicBody;
        bodyDef.position = new b2Vec2(0.0f, 4.0f);
        b2BodyId bodyId = B2.b2CreateBody(worldId, bodyDef);
        b2ShapeDef shapeDef = B2.b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        shapeDef.material.friction = 0.3f;
        B2.b2CreatePolygonShape(bodyId, shapeDef, B2.b2MakeBox(1.0f, 1.0f));

        printState(0, worldId, bodyId);
        for (int i = 0; i < 90; ++i) {
            B2.b2World_Step(worldId, 1.0f / 60.0f, 4);
            printState(i + 1, worldId, bodyId);
        }

        B2.b2DestroyWorld(worldId);
    }

    private static void printState(int step, b2WorldId worldId, b2BodyId bodyId) {
        b2Vec2 p = B2.b2Body_GetPosition(bodyId);
        b2Rot q = B2.b2Body_GetRotation(bodyId);
        b2Vec2 v = B2.b2Body_GetLinearVelocity(bodyId);
        b2Counters counters = B2.b2World_GetCounters(worldId);
        System.out.printf("%d %s %s %s %s %s %s %s %s %d %d%n",
            step,
            Float.toHexString(p.x),
            Float.toHexString(p.y),
            Float.toHexString(q.c),
            Float.toHexString(q.s),
            Float.toHexString(v.x),
            Float.toHexString(v.y),
            Float.toHexString(B2.b2Body_GetAngularVelocity(bodyId)),
            Float.toHexString(B2.b2Rot_GetAngle(q)),
            counters.contactCount,
            B2.b2World_GetAwakeBodyCount(worldId));

        b2ContactData[] data = new b2ContactData[4];
        int count = B2.b2Body_GetContactData(bodyId, data, data.length);
        if (count > 0) {
            b2Manifold m = data[0].manifold;
            System.out.printf("m %d %s %s %d %s", step,
                Float.toHexString(m.normal.x),
                Float.toHexString(m.normal.y),
                m.pointCount,
                Float.toHexString(m.rollingImpulse));
            for (int i = 0; i < m.pointCount; ++i) {
                b2ManifoldPoint point = m.points[i];
                System.out.printf(" %s %s %s %s %s %s %s %s %s %d %d",
                    Float.toHexString(point.anchorA.x),
                    Float.toHexString(point.anchorA.y),
                    Float.toHexString(point.anchorB.x),
                    Float.toHexString(point.anchorB.y),
                    Float.toHexString(point.separation),
                    Float.toHexString(point.normalImpulse),
                    Float.toHexString(point.tangentImpulse),
                    Float.toHexString(point.totalNormalImpulse),
                    Float.toHexString(point.normalVelocity),
                    point.id,
                    point.persisted ? 1 : 0);
            }
            System.out.println();
        }
    }
}
