package org.box2d4j.tools.parity;

import org.box2d4j.B2;
import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2ContactData;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Manifold;
import org.box2d4j.b2ManifoldPoint;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2Rot;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

public final class DoubleDominoTrace {
    private static final int COUNT = 15;
    private static final int CONTACT_BODY_COUNT = 4;

    private DoubleDominoTrace() {
    }

    public static void main(String[] args) {
        b2WorldId worldId = B2.b2CreateWorld(B2.b2DefaultWorldDef());

        b2BodyDef groundBodyDef = B2.b2DefaultBodyDef();
        groundBodyDef.position = new b2Vec2(0.0f, -1.0f);
        b2BodyId groundId = B2.b2CreateBody(worldId, groundBodyDef);
        B2.b2CreatePolygonShape(groundId, B2.b2DefaultShapeDef(), B2.b2MakeBox(100.0f, 1.0f));

        b2Polygon box = B2.b2MakeBox(0.125f, 0.5f);
        b2ShapeDef shapeDef = B2.b2DefaultShapeDef();
        shapeDef.material.friction = 0.6f;
        b2BodyDef bodyDef = B2.b2DefaultBodyDef();
        bodyDef.type = B2.b2_dynamicBody;

        b2BodyId[] bodies = new b2BodyId[COUNT];
        float x = -0.5f * COUNT;
        for (int i = 0; i < COUNT; ++i) {
            bodyDef.position = new b2Vec2(x, 0.5f);
            b2BodyId bodyId = B2.b2CreateBody(worldId, bodyDef);
            B2.b2CreatePolygonShape(bodyId, shapeDef, box);
            if (i == 0) {
                B2.b2Body_ApplyLinearImpulse(bodyId, new b2Vec2(0.2f, 0.0f), new b2Vec2(x, 1.0f), true);
            }

            bodies[i] = bodyId;
            x += 1.0f;
        }

        printFrame(0, worldId, bodies);
        for (int step = 1; step <= 55; ++step) {
            B2.b2World_Step(worldId, 1.0f / 60.0f, 4);
            if (step >= 53) {
                printFrame(step, worldId, bodies);
            }
        }

        B2.b2DestroyWorld(worldId);
    }

    private static void printFrame(int step, b2WorldId worldId, b2BodyId[] bodies) {
        b2Counters counters = B2.b2World_GetCounters(worldId);
        System.out.printf("frame %d %d %d %d%n", step, counters.contactCount, B2.b2World_GetAwakeBodyCount(worldId),
            counters.bodyCount);
        for (int i = 0; i < bodies.length; ++i) {
            printBody(step, i, bodies[i]);
        }
        for (int i = 0; i < CONTACT_BODY_COUNT; ++i) {
            printContacts(step, i, bodies[i]);
        }
    }

    private static void printBody(int step, int bodyIndex, b2BodyId bodyId) {
        b2Vec2 p = B2.b2Body_GetPosition(bodyId);
        b2Rot q = B2.b2Body_GetRotation(bodyId);
        b2Vec2 v = B2.b2Body_GetLinearVelocity(bodyId);
        System.out.printf("body %d %d %s %s %s %s %s %s %s %s%n",
            step,
            bodyIndex,
            hex(p.x),
            hex(p.y),
            hex(q.c),
            hex(q.s),
            hex(v.x),
            hex(v.y),
            hex(B2.b2Body_GetAngularVelocity(bodyId)),
            hex(B2.b2Rot_GetAngle(q)));
    }

    private static void printContacts(int step, int bodyIndex, b2BodyId bodyId) {
        b2ContactData[] contacts = new b2ContactData[8];
        int contactCount = B2.b2Body_GetContactData(bodyId, contacts, contacts.length);
        System.out.printf("contacts %d %d %d%n", step, bodyIndex, contactCount);
        for (int i = 0; i < contactCount; ++i) {
            b2ContactData contact = contacts[i];
            int other = otherBodyIndex(bodyId, contact);
            b2Manifold manifold = contact.manifold;
            System.out.printf("contact %d %d %d %d %d %d %s %s %s",
                step,
                bodyIndex,
                i,
                other,
                manifold.pointCount,
                contact.shapeIdA.index1 == 0 ? 0 : 1,
                hex(manifold.normal.x),
                hex(manifold.normal.y),
                hex(manifold.rollingImpulse));
            for (int j = 0; j < manifold.pointCount; ++j) {
                b2ManifoldPoint point = manifold.points[j];
                System.out.printf(" %s %s %s %s %s %s %s %s %s %s %d %d",
                    hex(point.point.x),
                    hex(point.point.y),
                    hex(point.anchorA.x),
                    hex(point.anchorA.y),
                    hex(point.anchorB.x),
                    hex(point.anchorB.y),
                    hex(point.separation),
                    hex(point.normalImpulse),
                    hex(point.tangentImpulse),
                    hex(point.totalNormalImpulse),
                    point.id,
                    point.persisted ? 1 : 0);
            }
            System.out.println();
        }
    }

    private static int otherBodyIndex(b2BodyId bodyId, b2ContactData contact) {
        b2BodyId bodyA = B2.b2Shape_GetBody(contact.shapeIdA);
        b2BodyId bodyB = B2.b2Shape_GetBody(contact.shapeIdB);
        if (sameBody(bodyId, bodyA)) {
            return bodyB.index1 - 2;
        }
        return bodyA.index1 - 2;
    }

    private static boolean sameBody(b2BodyId a, b2BodyId b) {
        return a.index1 == b.index1 && a.world0 == b.world0 && a.generation == b.generation;
    }

    private static String hex(float value) {
        return String.format("%08x", Float.floatToRawIntBits(value));
    }
}
