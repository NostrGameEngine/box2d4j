// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "constants.h"

#include <stdio.h>

int main(void)
{
    b2WorldId worlds[B2_MAX_WORLDS];
    for (int i = 0; i < B2_MAX_WORLDS; ++i)
    {
        b2WorldDef worldDef = b2DefaultWorldDef();
        worlds[i] = b2CreateWorld(&worldDef);
    }
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId overflow = b2CreateWorld(&worldDef);
    b2WorldId old = worlds[37];
    b2DestroyWorld(old);
    b2WorldId recycled = b2CreateWorld(&worldDef);

    printf("worldCapacity %d %d %d %d %d %d %d\n", overflow.index1, overflow.generation,
           b2World_IsValid(overflow) ? 1 : 0, recycled.index1, recycled.generation,
           b2World_IsValid(recycled) ? 1 : 0, b2World_IsValid(old) ? 1 : 0);
    for (int i = 0; i < B2_MAX_WORLDS; ++i)
    {
        if (i != 37)
        {
            b2DestroyWorld(worlds[i]);
        }
    }
    b2DestroyWorld(recycled);
    return 0;
}
