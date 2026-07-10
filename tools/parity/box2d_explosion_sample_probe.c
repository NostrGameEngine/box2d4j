// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>
#include <stdlib.h>

static void print_body(b2BodyId bodyId)
{
    b2Vec2 p = b2Body_GetPosition(bodyId);
    b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
    printf(" %.9g %.9g %.9g %.9g %.9g %.9g",
           p.x,
           p.y,
           b2Rot_GetAngle(b2Body_GetRotation(bodyId)),
           v.x,
           v.y,
           b2Body_GetAngularVelocity(bodyId));
}

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : 120;

    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);

    bodyDef.type = b2_dynamicBody;
    bodyDef.gravityScale = 0.0f;
    b2ShapeDef shapeDef = b2DefaultShapeDef();

    float referenceAngle = 0.0f;

    b2WeldJointDef weldDef = b2DefaultWeldJointDef();
    weldDef.referenceAngle = referenceAngle;
    weldDef.angularHertz = 0.5f;
    weldDef.angularDampingRatio = 0.7f;
    weldDef.linearHertz = 0.5f;
    weldDef.linearDampingRatio = 0.7f;
    weldDef.bodyIdA = groundId;
    weldDef.localAnchorB = b2Vec2_zero;

    b2BodyId bodies[12];
    b2JointId joints[12];
    float r = 8.0f;
    int index = 0;
    for (float angle = 0.0f; angle < 360.0f; angle += 30.0f)
    {
        b2CosSin cosSin = b2ComputeCosSin(angle * B2_PI / 180.0f);
        bodyDef.position = (b2Vec2){r * cosSin.cosine, r * cosSin.sine};
        b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

        b2Polygon box = b2MakeBox(1.0f, 0.1f);
        b2CreatePolygonShape(bodyId, &shapeDef, &box);

        weldDef.localAnchorA = bodyDef.position;
        weldDef.bodyIdB = bodyId;
        b2JointId jointId = b2CreateWeldJoint(worldId, &weldDef);
        bodies[index] = bodyId;
        joints[index] = jointId;
        index += 1;
    }

    float radius = 7.0f;
    float falloff = 3.0f;
    float impulse = 10.0f;
    b2ExplosionDef def = b2DefaultExplosionDef();
    def.position = b2Vec2_zero;
    def.radius = radius;
    def.falloff = falloff;
    def.impulsePerLength = impulse;
    b2World_Explode(worldId, &def);

    for (int step = 0; step < stepCount; ++step)
    {
        referenceAngle += 60.0f * B2_PI / 180.0f / 60.0f;
        referenceAngle = b2UnwindAngle(referenceAngle);
        for (int i = 0; i < 12; ++i)
        {
            b2Joint_SetReferenceAngle(joints[i], referenceAngle);
        }
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("explosion %.9g %d %d %d %d %d %d",
           referenceAngle,
           counters.bodyCount,
           counters.shapeCount,
           counters.jointCount,
           counters.contactCount,
           b2World_GetAwakeBodyCount(worldId),
           12);
    for (int i = 0; i < 12; ++i)
    {
        print_body(bodies[i]);
    }
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
