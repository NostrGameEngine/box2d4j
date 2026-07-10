// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>

static float friction_callback(float frictionA, int materialA, float frictionB, int materialB)
{
    (void)frictionA;
    (void)materialA;
    (void)frictionB;
    (void)materialB;
    return 0.1f;
}

static float restitution_callback(float restitutionA, int materialA, float restitutionB, int materialB)
{
    (void)restitutionA;
    (void)materialA;
    (void)restitutionB;
    (void)materialB;
    return 1.0f;
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2World_SetFrictionCallback(worldId, friction_callback);
    b2World_SetRestitutionCallback(worldId, restitution_callback);

    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(worldId, &bodyDef);

        b2Segment segment = {{-20.0f, 0.0f}, {20.0f, 0.0f}};
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreateSegmentShape(groundId, &shapeDef, &segment);
    }

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){0.0f, 3.0f};
    bodyDef.rotation = b2MakeRot(0.25f * B2_PI);
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    b2Capsule capsule = {{0.0f, -1.0f}, {0.0f, 1.0f}, 1.0f};
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2CreateCapsuleShape(bodyId, &shapeDef, &capsule);

    float mass = b2Body_GetMass(bodyId);
    float inertiaTensor = b2Body_GetRotationalInertia(bodyId);
    float offset = 1.5f;
    inertiaTensor += mass * offset * offset;

    b2MassData massData = {mass, {0.0f, -offset}, inertiaTensor};
    b2Body_SetMassData(bodyId, massData);

    for (int step = 0; step < 240; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Vec2 p = b2Body_GetPosition(bodyId);
    b2Vec2 linearVelocity = b2Body_GetLinearVelocity(bodyId);
    float angularVelocity = b2Body_GetAngularVelocity(bodyId);
    b2Vec2 localPoint = {0.0f, 2.0f};
    b2Vec2 worldPoint = b2Body_GetWorldPoint(bodyId, localPoint);
    b2Vec2 localVelocity = b2Body_GetLocalPointVelocity(bodyId, localPoint);
    b2Vec2 worldVelocity = b2Body_GetWorldPointVelocity(bodyId, worldPoint);
    b2MassData currentMass = b2Body_GetMassData(bodyId);
    b2Counters counters = b2World_GetCounters(worldId);

    printf("weeble %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %d %d\n",
           p.x,
           p.y,
           b2Rot_GetAngle(b2Body_GetRotation(bodyId)),
           linearVelocity.x,
           linearVelocity.y,
           angularVelocity,
           worldPoint.x,
           worldPoint.y,
           localVelocity.x,
           localVelocity.y,
           worldVelocity.x,
           worldVelocity.y,
           currentMass.mass,
           currentMass.center.y,
           currentMass.rotationalInertia,
           counters.contactCount,
           b2World_GetAwakeBodyCount(worldId));

    b2DestroyWorld(worldId);
    return 0;
}
