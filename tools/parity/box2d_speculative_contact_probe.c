// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdint.h>
#include <stdio.h>
#include <string.h>

static uint32_t bits(float value)
{
    uint32_t output;
    memcpy(&output, &value, sizeof(output));
    return output;
}

static void run_case(const char* label, bool enableSpeculative)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = b2Vec2_zero;
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2World_EnableSpeculative(worldId, enableSpeculative);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    b2Polygon groundBox = b2MakeBox(2.0f, 0.5f);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2CreatePolygonShape(groundId, &shapeDef, &groundBox);

    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){0.0f, 1.015f};
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    b2Polygon bodyBox = b2MakeBox(0.5f, 0.5f);
    b2CreatePolygonShape(bodyId, &shapeDef, &bodyBox);

    b2World_Step(worldId, 1.0f / 60.0f, 1);
    b2ContactData data = {0};
    int count = b2Body_GetContactData(bodyId, &data, 1);
    int pointCount = count == 0 ? 0 : data.manifold.pointCount;
    uint32_t separation0 = pointCount > 0 ? bits(data.manifold.points[0].separation) : 0u;
    uint32_t separation1 = pointCount > 1 ? bits(data.manifold.points[1].separation) : 0u;
    printf("%s %d %d %08x %08x\n", label, count, pointCount, separation0, separation1);
    b2DestroyWorld(worldId);
}

int main(void)
{
    run_case("speculativeOn", true);
    run_case("speculativeOff", false);
    return 0;
}
