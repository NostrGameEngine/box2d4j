// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>
#include <stdlib.h>

enum
{
    DEFAULT_STEP_COUNT = 120,
    BODY_COUNT = 40
};

static void print_body(b2BodyId bodyId)
{
    b2Transform transform = b2Body_GetTransform(bodyId);
    b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
    printf(" %.9g %.9g %.9g %.9g %.9g %.9g %.9g %d %d",
           transform.p.x,
           transform.p.y,
           transform.q.c,
           transform.q.s,
           velocity.x,
           velocity.y,
           b2Body_GetAngularVelocity(bodyId),
           b2Body_GetShapeCount(bodyId),
           b2Body_GetContactCapacity(bodyId));
}

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : DEFAULT_STEP_COUNT;

    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyId bodies[BODY_COUNT];
    int bodyIndex = 0;

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.position = (b2Vec2){0.0f, -2.0f};
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.material.friction = 0.7f;

    b2Polygon groundBox = b2MakeBox(40.0f, 2.0f);
    b2CreatePolygonShape(groundId, &shapeDef, &groundBox);

    float cardHeight = 0.2f;
    float cardThickness = 0.001f;

    float angle0 = 25.0f * B2_PI / 180.0f;
    float angle1 = -25.0f * B2_PI / 180.0f;
    float angle2 = 0.5f * B2_PI;

    b2Polygon cardBox = b2MakeBox(cardThickness, cardHeight);
    bodyDef.type = b2_dynamicBody;

    int Nb = 5;
    float z0 = 0.0f;
    float y = cardHeight - 0.02f;
    while (Nb)
    {
        float z = z0;
        for (int i = 0; i < Nb; ++i)
        {
            if (i != Nb - 1)
            {
                bodyDef.position = (b2Vec2){z + 0.25f, y + cardHeight - 0.015f};
                bodyDef.rotation = b2MakeRot(angle2);
                b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
                b2CreatePolygonShape(bodyId, &shapeDef, &cardBox);
                bodies[bodyIndex++] = bodyId;
            }

            bodyDef.position = (b2Vec2){z, y};
            bodyDef.rotation = b2MakeRot(angle1);
            b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
            b2CreatePolygonShape(bodyId, &shapeDef, &cardBox);
            bodies[bodyIndex++] = bodyId;

            z += 0.175f;

            bodyDef.position = (b2Vec2){z, y};
            bodyDef.rotation = b2MakeRot(angle0);
            bodyId = b2CreateBody(worldId, &bodyDef);
            b2CreatePolygonShape(bodyId, &shapeDef, &cardBox);
            bodies[bodyIndex++] = bodyId;

            z += 0.175f;
        }
        y += cardHeight * 2.0f - 0.03f;
        z0 += 0.175f;
        Nb -= 1;
    }

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("cardHouse %d %d %d %d %d %d",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           counters.jointCount,
           b2World_GetAwakeBodyCount(worldId),
           BODY_COUNT);
    for (int i = 0; i < BODY_COUNT; ++i)
    {
        print_body(bodies[i]);
    }
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
