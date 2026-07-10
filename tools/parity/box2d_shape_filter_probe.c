// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){0.0f, 0.0f};
    b2BodyId bodyA = b2CreateBody(worldId, &bodyDef);
    bodyDef.position = (b2Vec2){0.5f, 0.0f};
    b2BodyId bodyB = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.enableContactEvents = true;
    b2Polygon box = b2MakeBox(1.0f, 1.0f);
    b2ShapeId shapeA = b2CreatePolygonShape(bodyA, &shapeDef, &box);
    b2ShapeId shapeB = b2CreatePolygonShape(bodyB, &shapeDef, &box);

    b2World_Step(worldId, 1.0f / 60.0f, 4);
    printf("before %d %d\n", b2World_GetCounters(worldId).contactCount, b2Body_GetContactCapacity(bodyA));

    b2Filter filter = b2Shape_GetFilter(shapeA);
    printf("filter %llu %llu %d\n", (unsigned long long)filter.categoryBits, (unsigned long long)filter.maskBits,
           filter.groupIndex);
    filter.maskBits = 0;
    b2Shape_SetFilter(shapeA, filter);
    filter.maskBits = 0xffff;
    printf("afterSet %llu %llu %d %d\n", (unsigned long long)b2Shape_GetFilter(shapeA).categoryBits,
           (unsigned long long)b2Shape_GetFilter(shapeA).maskBits, b2Shape_GetFilter(shapeA).groupIndex,
           b2World_GetCounters(worldId).contactCount);

    b2World_Step(worldId, 1.0f / 60.0f, 4);
    printf("afterStep %d %d\n", b2World_GetCounters(worldId).contactCount, b2Body_GetContactCapacity(bodyA));

    b2Shape_EnableSensorEvents(shapeB, true);
    b2Shape_EnableContactEvents(shapeB, false);
    b2Shape_EnablePreSolveEvents(shapeB, true);
    b2Shape_EnableHitEvents(shapeB, true);
    printf("events %d %d %d %d\n", b2Shape_AreSensorEventsEnabled(shapeB) ? 1 : 0,
           b2Shape_AreContactEventsEnabled(shapeB) ? 1 : 0, b2Shape_ArePreSolveEventsEnabled(shapeB) ? 1 : 0,
           b2Shape_AreHitEventsEnabled(shapeB) ? 1 : 0);

    b2DestroyWorld(worldId);
    return 0;
}
