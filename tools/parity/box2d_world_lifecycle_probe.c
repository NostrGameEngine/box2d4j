// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "constants.h"

#include <stdint.h>
#include <stdio.h>

static void RunEmptyWorld(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    int validStart = b2World_IsValid(worldId);
    for (int i = 0; i < 60; ++i)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 1);
    }
    b2Counters counters = b2World_GetCounters(worldId);
    b2DestroyWorld(worldId);
    printf("empty %d %d %d %d %d\n", validStart, counters.bodyCount, counters.shapeCount, counters.contactCount,
           b2World_IsValid(worldId));
}

static void RunDestroyAllBodies(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    int validStart = b2World_IsValid(worldId);

    enum
    {
        BODY_COUNT = 10
    };
    int count = 0;
    bool creating = true;
    b2BodyId bodyIds[BODY_COUNT];
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    b2Polygon square = b2MakeSquare(0.5f);

    for (int i = 0; i < 2 * BODY_COUNT + 10; ++i)
    {
        if (creating)
        {
            if (count < BODY_COUNT)
            {
                bodyIds[count] = b2CreateBody(worldId, &bodyDef);
                b2ShapeDef shapeDef = b2DefaultShapeDef();
                b2CreatePolygonShape(bodyIds[count], &shapeDef, &square);
                count += 1;
            }
            else
            {
                creating = false;
            }
        }
        else if (count > 0)
        {
            b2DestroyBody(bodyIds[count - 1]);
            bodyIds[count - 1] = b2_nullBodyId;
            count -= 1;
        }

        b2World_Step(worldId, 1.0f / 60.0f, 3);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    b2DestroyWorld(worldId);
    printf("destroyAll %d %d %d %d %d\n", validStart, counters.bodyCount, counters.shapeCount, counters.contactCount,
           b2World_IsValid(worldId));
}

static void RunIdValidity(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyDef bodyDef = b2DefaultBodyDef();

    b2BodyId bodyId1 = b2CreateBody(worldId, &bodyDef);
    b2BodyId bodyId2 = b2CreateBody(worldId, &bodyDef);
    int body1Start = b2Body_IsValid(bodyId1);
    int body2Start = b2Body_IsValid(bodyId2);
    uint64_t body1Store = b2StoreBodyId(bodyId1);
    uint64_t body2Store = b2StoreBodyId(bodyId2);

    b2DestroyBody(bodyId1);
    int body1AfterDestroy = b2Body_IsValid(bodyId1);
    b2DestroyBody(bodyId2);
    int body2AfterDestroy = b2Body_IsValid(bodyId2);
    b2DestroyWorld(worldId);

    printf("isValid %u %llu %llu %d %d %d %d %d %d %d\n", b2StoreWorldId(worldId), (unsigned long long)body1Store,
           (unsigned long long)body2Store, body1Start, body2Start, body1AfterDestroy, body2AfterDestroy,
           b2World_IsValid(worldId), b2Body_IsValid(bodyId1), b2Body_IsValid(bodyId2));
}

static void RunWorldRecycle(void)
{
    enum
    {
        WORLD_COUNT = B2_MAX_WORLDS / 2,
        ITERATION_COUNT = 100
    };

    b2WorldId worldIds[WORLD_COUNT];
    uint32_t firstStore = 0;
    uint32_t firstSlotLastStore = 0;
    uint32_t lastSlotFirstStore = 0;
    uint32_t lastStore = 0;

    for (int i = 0; i < ITERATION_COUNT; ++i)
    {
        b2WorldDef worldDef = b2DefaultWorldDef();
        for (int j = 0; j < WORLD_COUNT; ++j)
        {
            worldIds[j] = b2CreateWorld(&worldDef);
            if (i == 0 && j == 0)
            {
                firstStore = b2StoreWorldId(worldIds[j]);
            }
            if (i == ITERATION_COUNT - 1 && j == 0)
            {
                firstSlotLastStore = b2StoreWorldId(worldIds[j]);
            }
            if (i == 0 && j == WORLD_COUNT - 1)
            {
                lastSlotFirstStore = b2StoreWorldId(worldIds[j]);
            }
            if (i == ITERATION_COUNT - 1 && j == WORLD_COUNT - 1)
            {
                lastStore = b2StoreWorldId(worldIds[j]);
            }

            b2BodyDef bodyDef = b2DefaultBodyDef();
            b2CreateBody(worldIds[j], &bodyDef);
        }

        for (int j = 0; j < WORLD_COUNT; ++j)
        {
            for (int k = 0; k < 10; ++k)
            {
                b2World_Step(worldIds[j], 1.0f / 60.0f, 1);
            }
        }

        for (int j = WORLD_COUNT - 1; j >= 0; --j)
        {
            b2DestroyWorld(worldIds[j]);
        }
    }

    printf("recycle %d %d %u %u %u %u %d %d\n", WORLD_COUNT, ITERATION_COUNT, firstStore, firstSlotLastStore,
           lastSlotFirstStore, lastStore, b2World_IsValid(worldIds[0]), b2World_IsValid(worldIds[WORLD_COUNT - 1]));
}

int main(void)
{
    RunEmptyWorld();
    RunDestroyAllBodies();
    RunIdValidity();
    RunWorldRecycle();
    return 0;
}
