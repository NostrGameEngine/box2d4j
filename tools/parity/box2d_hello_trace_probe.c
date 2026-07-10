// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>

static void print_state(int step, b2WorldId worldId, b2BodyId bodyId)
{
    b2Vec2 p = b2Body_GetPosition(bodyId);
    b2Rot q = b2Body_GetRotation(bodyId);
    b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
    b2Counters counters = b2World_GetCounters(worldId);
    printf("%d %a %a %a %a %a %a %a %a %d %d\n", step, (double)p.x, (double)p.y, (double)q.c, (double)q.s, (double)v.x,
           (double)v.y, (double)b2Body_GetAngularVelocity(bodyId), (double)b2Rot_GetAngle(q), counters.contactCount,
           b2World_GetAwakeBodyCount(worldId));

    b2ContactData data[4] = { 0 };
    int count = b2Body_GetContactData(bodyId, data, 4);
    if (count > 0)
    {
        b2Manifold* m = &data[0].manifold;
        printf("m %d %a %a %d %a", step, (double)m->normal.x, (double)m->normal.y, m->pointCount, (double)m->rollingImpulse);
        for (int i = 0; i < m->pointCount; ++i)
        {
            b2ManifoldPoint* p = m->points + i;
            printf(" %a %a %a %a %a %a %a %a %a %d %d", (double)p->anchorA.x, (double)p->anchorA.y,
                   (double)p->anchorB.x, (double)p->anchorB.y, (double)p->separation, (double)p->normalImpulse,
                   (double)p->tangentImpulse, (double)p->totalNormalImpulse, (double)p->normalVelocity, p->id,
                   p->persisted ? 1 : 0);
        }
        printf("\n");
    }
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = (b2Vec2){ 0.0f, -10.0f };
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef groundBodyDef = b2DefaultBodyDef();
    groundBodyDef.position = (b2Vec2){ 0.0f, -10.0f };
    b2BodyId groundId = b2CreateBody(worldId, &groundBodyDef);
    b2Polygon groundBox = b2MakeBox(50.0f, 10.0f);
    b2ShapeDef groundShapeDef = b2DefaultShapeDef();
    b2CreatePolygonShape(groundId, &groundShapeDef, &groundBox);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){ 0.0f, 4.0f };
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    b2Polygon dynamicBox = b2MakeBox(1.0f, 1.0f);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.0f;
    shapeDef.material.friction = 0.3f;
    b2CreatePolygonShape(bodyId, &shapeDef, &dynamicBox);

    print_state(0, worldId, bodyId);
    for (int i = 0; i < 90; ++i)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        print_state(i + 1, worldId, bodyId);
    }

    b2DestroyWorld(worldId);
    return 0;
}
