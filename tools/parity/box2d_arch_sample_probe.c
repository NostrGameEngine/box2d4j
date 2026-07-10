// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>
#include <stdlib.h>

enum
{
    DEFAULT_STEP_COUNT = 120,
    BODY_COUNT = 21
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

    b2Vec2 ps1[9] = {{16.0f, 0.0f},
                     {14.93803712795643f, 5.133601056842984f},
                     {13.79871746027416f, 10.24928069555078f},
                     {12.56252963284711f, 15.34107019122473f},
                     {11.20040987372525f, 20.39856541571217f},
                     {9.66521217819836f, 25.40369899225096f},
                     {7.87179930638133f, 30.3179337000085f},
                     {5.635199558196225f, 35.03820717801641f},
                     {2.405937953536585f, 39.09554102558315f}};

    b2Vec2 ps2[9] = {{24.0f, 0.0f},
                     {22.33619528222415f, 6.02299846205841f},
                     {20.54936888969905f, 12.00964361211476f},
                     {18.60854610798073f, 17.9470321677465f},
                     {16.46769273811807f, 23.81367936585418f},
                     {14.05325025774858f, 29.57079353071012f},
                     {11.23551045834022f, 35.13775818285372f},
                     {7.752568160730571f, 40.30450679009583f},
                     {3.016931552701656f, 44.28891593799322f}};

    float scale = 0.25f;
    for (int i = 0; i < 9; ++i)
    {
        ps1[i] = b2MulSV(scale, ps1[i]);
        ps2[i] = b2MulSV(scale, ps2[i]);
    }

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.material.friction = 0.6f;

    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    b2Segment segment = {{-100.0f, 0.0f}, {100.0f, 0.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &segment);

    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;

    for (int i = 0; i < 8; ++i)
    {
        b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
        b2Vec2 ps[4] = {ps1[i], ps2[i], ps2[i + 1], ps1[i + 1]};
        b2Hull hull = b2ComputeHull(ps, 4);
        b2Polygon polygon = b2MakePolygon(&hull, 0.0f);
        b2CreatePolygonShape(bodyId, &shapeDef, &polygon);
        bodies[bodyIndex++] = bodyId;
    }

    for (int i = 0; i < 8; ++i)
    {
        b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
        b2Vec2 ps[4] = {{-ps2[i].x, ps2[i].y},
                        {-ps1[i].x, ps1[i].y},
                        {-ps1[i + 1].x, ps1[i + 1].y},
                        {-ps2[i + 1].x, ps2[i + 1].y}};
        b2Hull hull = b2ComputeHull(ps, 4);
        b2Polygon polygon = b2MakePolygon(&hull, 0.0f);
        b2CreatePolygonShape(bodyId, &shapeDef, &polygon);
        bodies[bodyIndex++] = bodyId;
    }

    {
        b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
        b2Vec2 ps[4] = {ps1[8], ps2[8], {-ps2[8].x, ps2[8].y}, {-ps1[8].x, ps1[8].y}};
        b2Hull hull = b2ComputeHull(ps, 4);
        b2Polygon polygon = b2MakePolygon(&hull, 0.0f);
        b2CreatePolygonShape(bodyId, &shapeDef, &polygon);
        bodies[bodyIndex++] = bodyId;
    }

    for (int i = 0; i < 4; ++i)
    {
        b2Polygon box = b2MakeBox(2.0f, 0.5f);
        bodyDef.position = (b2Vec2){0.0f, 0.5f + ps2[8].y + 1.0f * i};
        b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
        b2CreatePolygonShape(bodyId, &shapeDef, &box);
        bodies[bodyIndex++] = bodyId;
    }

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("arch %d %d %d %d %d %d",
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
