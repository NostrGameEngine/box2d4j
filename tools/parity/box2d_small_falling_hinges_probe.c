// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>

static void PrintBody(const char* label, int index, b2BodyId bodyId)
{
    b2Transform transform = b2Body_GetTransform(bodyId);
    b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
    printf("%s %d %.9g %.9g %.9g %.9g %.9g %.9g %.9g\n",
           label,
           index,
           transform.p.x,
           transform.p.y,
           transform.q.c,
           transform.q.s,
           velocity.x,
           velocity.y,
           b2Body_GetAngularVelocity(bodyId));
}

static void RunSmallFallingHinges(const char* prefix, int bodyCount)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.enableSleep = false;
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef groundDef = b2DefaultBodyDef();
    groundDef.position = (b2Vec2){0.0f, -1.0f};
    b2BodyId groundId = b2CreateBody(worldId, &groundDef);
    b2Polygon groundBox = b2MakeBox(20.0f, 1.0f);
    b2ShapeDef groundShapeDef = b2DefaultShapeDef();
    b2CreatePolygonShape(groundId, &groundShapeDef, &groundBox);

    b2BodyId bodyIds[120];

    float h = 0.25f;
    float r = 0.1f * h;
    float offset = 0.4f * h;
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
    jointDef.localAnchorA = (b2Vec2){h, h};
    jointDef.localAnchorB = (b2Vec2){offset, -h};

    b2BodyId prevBodyId = b2_nullBodyId;
    for (int i = 0; i < bodyCount; ++i)
    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.enableSleep = false;
        bodyDef.position.x = offset * i;
        bodyDef.position.y = h + 2.0f * h * i;
        bodyDef.rotation = b2MakeRot(0.1f * i - 1.0f);

        b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
        if ((i & 1) == 0)
        {
            prevBodyId = bodyId;
        }
        else
        {
            jointDef.bodyIdA = prevBodyId;
            jointDef.bodyIdB = bodyId;
            b2CreateRevoluteJoint(worldId, &jointDef);
            prevBodyId = b2_nullBodyId;
        }

        b2CreatePolygonShape(bodyId, &shapeDef, &box);
        bodyIds[i] = bodyId;
    }

    for (int i = 0; i < 90; ++i)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    b2BodyEvents events = b2World_GetBodyEvents(worldId);
    printf("%sFallingHinges %d %d %d\n", prefix, counters.bodyCount, counters.contactCount, events.moveCount);
    for (int i = 0; i < bodyCount; ++i)
    {
        char label[32];
        snprintf(label, sizeof(label), "%sBody", prefix);
        PrintBody(label, i, bodyIds[i]);
    }

    b2DestroyWorld(worldId);
}

static void RunGridFallingHinges(const char* prefix, int stepCount)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.enableSleep = false;
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef groundDef = b2DefaultBodyDef();
    groundDef.position = (b2Vec2){0.0f, -1.0f};
    b2BodyId groundId = b2CreateBody(worldId, &groundDef);
    b2Polygon groundBox = b2MakeBox(20.0f, 1.0f);
    b2ShapeDef groundShapeDef = b2DefaultShapeDef();
    b2CreatePolygonShape(groundId, &groundShapeDef, &groundBox);

    int columnCount = 4;
    int rowCount = 30;
    int bodyCount = columnCount * rowCount;
    b2BodyId bodyIds[120];

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
    jointDef.localAnchorA = (b2Vec2){h, h};
    jointDef.localAnchorB = (b2Vec2){offset, -h};
    jointDef.drawSize = 0.1f;

    int bodyIndex = 0;
    for (int j = 0; j < columnCount; ++j)
    {
        float x = xroot + j * dx;
        b2BodyId prevBodyId = b2_nullBodyId;
        for (int i = 0; i < rowCount; ++i)
        {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.enableSleep = false;
            bodyDef.position.x = x + offset * i;
            bodyDef.position.y = h + 2.0f * h * i;
            bodyDef.rotation = b2MakeRot(0.1f * i - 1.0f);

            b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
            if ((i & 1) == 0)
            {
                prevBodyId = bodyId;
            }
            else
            {
                jointDef.bodyIdA = prevBodyId;
                jointDef.bodyIdB = bodyId;
                b2CreateRevoluteJoint(worldId, &jointDef);
                prevBodyId = b2_nullBodyId;
            }

            b2CreatePolygonShape(bodyId, &shapeDef, &box);
            bodyIds[bodyIndex++] = bodyId;
        }
    }

    for (int i = 0; i < stepCount; ++i)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    b2BodyEvents events = b2World_GetBodyEvents(worldId);
    printf("%sFallingHinges %d %d %d\n", prefix, counters.bodyCount, counters.contactCount, events.moveCount);
    for (int i = 0; i < bodyCount; ++i)
    {
        char label[32];
        snprintf(label, sizeof(label), "%sBody", prefix);
        PrintBody(label, i, bodyIds[i]);
    }

    b2DestroyWorld(worldId);
}

int main(void)
{
    RunSmallFallingHinges("small4", 4);
    RunSmallFallingHinges("small8", 8);
    RunSmallFallingHinges("small16", 16);
    RunSmallFallingHinges("small24", 24);
    RunSmallFallingHinges("small32", 32);
    RunSmallFallingHinges("small48", 48);
    RunSmallFallingHinges("small64", 64);
    RunSmallFallingHinges("small120", 120);
    RunGridFallingHinges("grid120", 90);
    RunGridFallingHinges("grid120long", 165);
    return 0;
}
