// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.0f;
    b2Polygon box = b2MakeBox(1.0f, 1.0f);
    b2CreatePolygonShape(bodyId, &shapeDef, &box);

    printf("initial %d %d %.9g\n", b2Body_IsAwake(bodyId) ? 1 : 0, b2Body_IsSleepEnabled(bodyId) ? 1 : 0,
           b2Body_GetSleepThreshold(bodyId));
    b2Body_SetSleepThreshold(bodyId, 0.25f);
    b2Body_SetAwake(bodyId, false);
    printf("sleep %.9g %d %.9g\n", b2Body_GetSleepThreshold(bodyId), b2Body_IsAwake(bodyId) ? 1 : 0,
           b2Body_GetLinearVelocity(bodyId).y);
    b2Body_EnableSleep(bodyId, false);
    printf("disabled %d %d\n", b2Body_IsSleepEnabled(bodyId) ? 1 : 0, b2Body_IsAwake(bodyId) ? 1 : 0);

    b2DestroyWorld(worldId);
    return 0;
}
