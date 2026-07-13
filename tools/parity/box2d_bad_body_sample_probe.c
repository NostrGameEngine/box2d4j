// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>
#include <stdlib.h>

static void print_body(b2BodyId bodyId)
{
    b2Vec2 p = b2Body_GetPosition(bodyId);
    b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
    b2MassData massData = b2Body_GetMassData(bodyId);
    printf(" %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g",
           p.x,
           p.y,
           b2Rot_GetAngle(b2Body_GetRotation(bodyId)),
           v.x,
           v.y,
           b2Body_GetAngularVelocity(bodyId),
           massData.mass,
           massData.center.y,
           massData.rotationalInertia);
}

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : 120;
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(worldId, &bodyDef);

        b2Segment segment = {{-20.0f, 0.0f}, {20.0f, 0.0f}};
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreateSegmentShape(groundId, &shapeDef, &segment);
    }

    b2BodyId badBodyId;
    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = (b2Vec2){0.0f, 3.0f};
        bodyDef.angularVelocity = 0.5f;
        bodyDef.rotation = b2MakeRot(0.25f * B2_PI);
        badBodyId = b2CreateBody(worldId, &bodyDef);

        b2Capsule capsule = {{0.0f, -1.0f}, {0.0f, 1.0f}, 1.0f};
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 0.0f;
        b2CreateCapsuleShape(badBodyId, &shapeDef, &capsule);
    }

    b2BodyId normalBodyId;
    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = (b2Vec2){2.0f, 3.0f};
        bodyDef.rotation = b2MakeRot(0.25f * B2_PI);
        normalBodyId = b2CreateBody(worldId, &bodyDef);

        b2Capsule capsule = {{0.0f, -1.0f}, {0.0f, 1.0f}, 1.0f};
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreateCapsuleShape(normalBodyId, &shapeDef, &capsule);
    }

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
        b2Body_ApplyForceToCenter(badBodyId, (b2Vec2){0.0f, 10.0f}, true);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("badBody %d %d", counters.contactCount, b2World_GetAwakeBodyCount(worldId));
    print_body(badBodyId);
    print_body(normalBodyId);
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
