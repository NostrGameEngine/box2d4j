// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>
#include <string.h>

enum
{
    count = 15,
    contactBodyCount = 4
};

static int same_body(b2BodyId a, b2BodyId b)
{
    return a.index1 == b.index1 && a.world0 == b.world0 && a.generation == b.generation;
}

static int other_body_index(b2BodyId bodyId, b2ContactData* contact)
{
    b2BodyId bodyA = b2Shape_GetBody(contact->shapeIdA);
    b2BodyId bodyB = b2Shape_GetBody(contact->shapeIdB);
    return same_body(bodyId, bodyA) ? bodyB.index1 - 2 : bodyA.index1 - 2;
}

static unsigned int hex_float(float value)
{
    unsigned int bits = 0;
    memcpy(&bits, &value, sizeof(bits));
    return bits;
}

static void print_body(int step, int bodyIndex, b2BodyId bodyId)
{
    b2Vec2 p = b2Body_GetPosition(bodyId);
    b2Rot q = b2Body_GetRotation(bodyId);
    b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
    printf("body %d %d %08x %08x %08x %08x %08x %08x %08x %08x\n", step, bodyIndex, hex_float(p.x), hex_float(p.y),
           hex_float(q.c), hex_float(q.s), hex_float(v.x), hex_float(v.y), hex_float(b2Body_GetAngularVelocity(bodyId)),
           hex_float(b2Rot_GetAngle(q)));
}

static void print_contacts(int step, int bodyIndex, b2BodyId bodyId)
{
    b2ContactData contacts[8] = { 0 };
    int contactCount = b2Body_GetContactData(bodyId, contacts, 8);
    printf("contacts %d %d %d\n", step, bodyIndex, contactCount);
    for (int i = 0; i < contactCount; ++i)
    {
        b2ContactData* contact = contacts + i;
        b2Manifold* manifold = &contact->manifold;
        printf("contact %d %d %d %d %d %d %08x %08x %08x", step, bodyIndex, i, other_body_index(bodyId, contact),
               manifold->pointCount, contact->shapeIdA.index1 == 0 ? 0 : 1, hex_float(manifold->normal.x),
               hex_float(manifold->normal.y), hex_float(manifold->rollingImpulse));
        for (int j = 0; j < manifold->pointCount; ++j)
        {
            b2ManifoldPoint* point = manifold->points + j;
            printf(" %08x %08x %08x %08x %08x %08x %08x %08x %08x %08x %d %d", hex_float(point->point.x),
                   hex_float(point->point.y), hex_float(point->anchorA.x), hex_float(point->anchorA.y),
                   hex_float(point->anchorB.x), hex_float(point->anchorB.y), hex_float(point->separation),
                   hex_float(point->normalImpulse), hex_float(point->tangentImpulse), hex_float(point->totalNormalImpulse),
                   point->id, point->persisted ? 1 : 0);
        }
        printf("\n");
    }
}

static void print_frame(int step, b2WorldId worldId, b2BodyId* bodies)
{
    b2Counters counters = b2World_GetCounters(worldId);
    printf("frame %d %d %d %d\n", step, counters.contactCount, b2World_GetAwakeBodyCount(worldId), counters.bodyCount);
    for (int i = 0; i < count; ++i)
    {
        print_body(step, i, bodies[i]);
    }
    for (int i = 0; i < contactBodyCount; ++i)
    {
        print_contacts(step, i, bodies[i]);
    }
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef groundBodyDef = b2DefaultBodyDef();
    groundBodyDef.position = (b2Vec2){ 0.0f, -1.0f };
    b2BodyId groundId = b2CreateBody(worldId, &groundBodyDef);
    b2Polygon groundBox = b2MakeBox(100.0f, 1.0f);
    b2ShapeDef groundShapeDef = b2DefaultShapeDef();
    b2CreatePolygonShape(groundId, &groundShapeDef, &groundBox);

    b2Polygon box = b2MakeBox(0.125f, 0.5f);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.material.friction = 0.6f;
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;

    b2BodyId bodies[count];
    float x = -0.5f * count;
    for (int i = 0; i < count; ++i)
    {
        bodyDef.position = (b2Vec2){ x, 0.5f };
        b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
        b2CreatePolygonShape(bodyId, &shapeDef, &box);
        if (i == 0)
        {
            b2Body_ApplyLinearImpulse(bodyId, (b2Vec2){ 0.2f, 0.0f }, (b2Vec2){ x, 1.0f }, true);
        }

        bodies[i] = bodyId;
        x += 1.0f;
    }

    print_frame(0, worldId, bodies);
    for (int step = 1; step <= 55; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        if (step >= 53)
        {
            print_frame(step, worldId, bodies);
        }
    }

    b2DestroyWorld(worldId);
    return 0;
}
