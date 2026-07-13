// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : 180;
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2World_SetGravity(worldId, (b2Vec2){0.0f, -20.0f});
    b2World_SetContactTuning(worldId, 0.25f * 360.0f, 10.0f, 3.0f);

    int shapeIndex = 0;
    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(worldId, &bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.userData = (void*)(intptr_t)shapeIndex;
        shapeIndex += 1;

        b2Segment segment = {{-10.0f, 0.0f}, {10.0f, 0.0f}};
        b2CreateSegmentShape(groundId, &shapeDef, &segment);
    }

    b2BodyId bodies[10];
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;

    b2Circle circle = {0};
    circle.radius = 0.5f;

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.enableHitEvents = true;
    shapeDef.material.friction = 0.0f;

    float y = 0.75f;
    for (int i = 0; i < 10; ++i)
    {
        bodyDef.position.y = y;
        bodies[i] = b2CreateBody(worldId, &bodyDef);

        shapeDef.userData = (void*)(intptr_t)shapeIndex;
        shapeDef.density = 1.0f + 4.0f * i;
        shapeIndex += 1;
        b2CreateCircleShape(bodies[i], &shapeDef, &circle);

        y += 1.25f;
    }

    int totalHits = 0;
    int pairChecksum = 0;
    float speedSum = 0.0f;
    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        b2ContactEvents events = b2World_GetContactEvents(worldId);
        totalHits += events.hitCount;
        for (int i = 0; i < events.hitCount; ++i)
        {
            b2ContactHitEvent* event = events.hitEvents + i;
            int indexA = (int)(intptr_t)b2Shape_GetUserData(event->shapeIdA);
            int indexB = (int)(intptr_t)b2Shape_GetUserData(event->shapeIdB);
            pairChecksum += (indexA + 1) * 31 + (indexB + 1) * 17;
            speedSum += event->approachSpeed;
        }
    }

    b2Vec2 p0 = b2Body_GetPosition(bodies[0]);
    b2Vec2 v0 = b2Body_GetLinearVelocity(bodies[0]);
    b2Vec2 p9 = b2Body_GetPosition(bodies[9]);
    b2Vec2 v9 = b2Body_GetLinearVelocity(bodies[9]);
    b2Counters counters = b2World_GetCounters(worldId);

    printf("circleStack %d %d %.9g %d %d %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g\n",
           totalHits,
           pairChecksum,
           speedSum,
           counters.contactCount,
           b2World_GetAwakeBodyCount(worldId),
           p0.x,
           p0.y,
           b2Rot_GetAngle(b2Body_GetRotation(bodies[0])),
           v0.x,
           p9.x,
           p9.y,
           b2Rot_GetAngle(b2Body_GetRotation(bodies[9])),
           v9.y);

    b2DestroyWorld(worldId);
    return 0;
}
