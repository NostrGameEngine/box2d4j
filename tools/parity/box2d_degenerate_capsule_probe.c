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

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 2.0f;
    b2Capsule shortCapsule = {{1.25f, -0.5f}, {1.25f, -0.5f}, 0.7f};
    b2ShapeId shortId = b2CreateCapsuleShape(bodyId, &shapeDef, &shortCapsule);
    b2Circle circle = b2Shape_GetCircle(shortId);
    b2MassData mass = b2Shape_GetMassData(shortId);

    b2Capsule capsule = {{-0.01f, 0.0f}, {0.01f, 0.0f}, 0.2f};
    b2ShapeId capsuleId = b2CreateCapsuleShape(bodyId, &shapeDef, &capsule);
    printf("degenerateCapsule %d %08x %08x %08x %08x %08x %d %d\n", b2Shape_GetType(shortId),
           bits(circle.center.x), bits(circle.center.y), bits(circle.radius), bits(mass.mass),
           bits(mass.rotationalInertia), b2Shape_GetType(capsuleId), b2Body_GetShapeCount(bodyId));

    b2DestroyWorld(worldId);
    return 0;
}
