// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>

static void print_state(b2WorldId worldId, b2BodyId bodyA, b2BodyId bodyB)
{
    printf(" %d %d %d %d", b2World_IsSleepingEnabled(worldId) ? 1 : 0,
           b2World_GetAwakeBodyCount(worldId), b2Body_IsAwake(bodyA) ? 1 : 0, b2Body_IsAwake(bodyB) ? 1 : 0);
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = b2Vec2_zero;
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    b2BodyId bodyA = b2CreateBody(worldId, &bodyDef);
    bodyDef.position = (b2Vec2){3.0f, 0.0f};
    b2BodyId bodyB = b2CreateBody(worldId, &bodyDef);

    for (int i = 0; i < 60; ++i)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 1);
    }
    printf("worldSleepingToggle");
    print_state(worldId, bodyA, bodyB);

    b2World_EnableSleeping(worldId, false);
    print_state(worldId, bodyA, bodyB);
    b2World_EnableSleeping(worldId, false);
    b2World_Step(worldId, 1.0f / 60.0f, 1);
    print_state(worldId, bodyA, bodyB);

    b2World_EnableSleeping(worldId, true);
    print_state(worldId, bodyA, bodyB);
    for (int i = 0; i < 60; ++i)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 1);
    }
    print_state(worldId, bodyA, bodyB);

    printf("\n");
    b2DestroyWorld(worldId);
    return 0;
}
