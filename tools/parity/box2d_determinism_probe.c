// SPDX-License-Identifier: MIT

#include "determinism.h"

#include "box2d/box2d.h"

#include <stdio.h>

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    FallingHingeData data = CreateFallingHinges(worldId);
    bool done = false;
    while (!done)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        done = UpdateFallingHinges(worldId, &data);
    }

    printf("determinism %d %08x\n", data.sleepStep, data.hash);

    DestroyFallingHinges(&data);
    b2DestroyWorld(worldId);
    return 0;
}
