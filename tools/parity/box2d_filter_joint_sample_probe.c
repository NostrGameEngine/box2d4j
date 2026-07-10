// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>
#include <stdlib.h>

enum
{
    DEFAULT_STEP_COUNT = 120
};

static void print_body(b2BodyId bodyId)
{
    b2Vec2 p = b2Body_GetPosition(bodyId);
    b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
    printf(" %d %.9g %.9g %.9g %.9g %.9g %.9g",
           b2Body_GetContactCapacity(bodyId),
           p.x,
           p.y,
           b2Rot_GetAngle(b2Body_GetRotation(bodyId)),
           v.x,
           v.y,
           b2Body_GetAngularVelocity(bodyId));
}

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : DEFAULT_STEP_COUNT;
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2Segment segment = {{-20.0f, 0.0f}, {20.0f, 0.0f}};
        b2CreateSegmentShape(groundId, &shapeDef, &segment);
    }

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){-4.0f, 2.0f};
    b2BodyId bodyId1 = b2CreateBody(worldId, &bodyDef);

    b2Polygon box = b2MakeSquare(2.0f);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2CreatePolygonShape(bodyId1, &shapeDef, &box);

    bodyDef.position = (b2Vec2){4.0f, 2.0f};
    b2BodyId bodyId2 = b2CreateBody(worldId, &bodyDef);
    b2CreatePolygonShape(bodyId2, &shapeDef, &box);

    b2FilterJointDef jointDef = b2DefaultFilterJointDef();
    jointDef.bodyIdA = bodyId1;
    jointDef.bodyIdB = bodyId2;
    b2JointId jointId = b2CreateFilterJoint(worldId, &jointDef);

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("filterJoint %d %d %d %d %d %d %d 2",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           counters.jointCount,
           b2World_GetAwakeBodyCount(worldId),
           b2Joint_GetType(jointId),
           b2Joint_GetCollideConnected(jointId) ? 1 : 0);
    print_body(bodyId1);
    print_body(bodyId2);
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
