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

static b2BodyId create_body(b2WorldId worldId, float positionX, float localCenterX)
{
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_kinematicBody;
    bodyDef.position = (b2Vec2){positionX, 0.0f};
    bodyDef.angularVelocity = 0.1f;
    bodyDef.sleepThreshold = 0.5f;
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Circle circle = {{localCenterX, 0.0f}, 1.0f};
    b2CreateCircleShape(bodyId, &shapeDef, &circle);
    return bodyId;
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = b2Vec2_zero;
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyId centered = create_body(worldId, 0.0f, 0.0f);
    b2BodyId offset = create_body(worldId, 20.0f, 10.0f);

    for (int i = 0; i < 60; ++i)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 1);
    }
    b2Rot centeredRotation = b2Body_GetRotation(centered);
    b2Rot offsetRotation = b2Body_GetRotation(offset);
    printf("kinematicExtent %d %d %d %08x %08x %08x %08x\n", b2World_GetAwakeBodyCount(worldId),
           b2Body_IsAwake(centered) ? 1 : 0, b2Body_IsAwake(offset) ? 1 : 0,
           bits(centeredRotation.c), bits(centeredRotation.s), bits(offsetRotation.c), bits(offsetRotation.s));
    b2DestroyWorld(worldId);
    return 0;
}
