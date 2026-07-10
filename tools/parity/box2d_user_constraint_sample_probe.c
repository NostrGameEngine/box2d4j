// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>
#include <stdlib.h>

enum
{
    DEFAULT_STEP_COUNT = 120
};

static const float SAMPLE_HERTZ = 60.0f;
static const float CONSTRAINT_HERTZ = 3.0f;
static const float DAMPING_RATIO = 0.7f;
static const float MAX_FORCE = 1000.0f;

static void apply_user_constraint(b2BodyId bodyId, float impulses[2])
{
    float timeStep = 1.0f / SAMPLE_HERTZ;
    float omega = 2.0f * B2_PI * CONSTRAINT_HERTZ;
    float sigma = 2.0f * DAMPING_RATIO + timeStep * omega;
    float s = timeStep * omega * sigma;
    float impulseCoefficient = 1.0f / (1.0f + s);
    float massCoefficient = s * impulseCoefficient;
    float biasCoefficient = omega / sigma;

    b2Vec2 localAnchors[2] = {{1.0f, -0.5f}, {1.0f, 0.5f}};
    float mass = b2Body_GetMass(bodyId);
    float invMass = mass < 0.0001f ? 0.0f : 1.0f / mass;
    float inertiaTensor = b2Body_GetRotationalInertia(bodyId);
    float invI = inertiaTensor < 0.0001f ? 0.0f : 1.0f / inertiaTensor;

    b2Vec2 vB = b2Body_GetLinearVelocity(bodyId);
    float omegaB = b2Body_GetAngularVelocity(bodyId);
    b2Vec2 pB = b2Body_GetWorldCenterOfMass(bodyId);

    for (int i = 0; i < 2; ++i)
    {
        b2Vec2 anchorA = {3.0f, 0.0f};
        b2Vec2 anchorB = b2Body_GetWorldPoint(bodyId, localAnchors[i]);
        b2Vec2 deltaAnchor = b2Sub(anchorB, anchorA);

        float slackLength = 1.0f;
        float length = b2Length(deltaAnchor);
        float C = length - slackLength;
        if (C < 0.0f || length < 0.001f)
        {
            impulses[i] = 0.0f;
            continue;
        }

        b2Vec2 axis = b2Normalize(deltaAnchor);
        b2Vec2 rB = b2Sub(anchorB, pB);
        float Jb = b2Cross(rB, axis);
        float K = invMass + Jb * invI * Jb;
        float invK = K < 0.0001f ? 0.0f : 1.0f / K;

        float Cdot = b2Dot(vB, axis) + Jb * omegaB;
        float impulse = -massCoefficient * invK * (Cdot + biasCoefficient * C);
        float appliedImpulse = b2ClampFloat(impulse, -MAX_FORCE * timeStep, 0.0f);

        vB = b2MulAdd(vB, invMass * appliedImpulse, axis);
        omegaB += appliedImpulse * invI * Jb;
        impulses[i] = appliedImpulse;
    }

    b2Body_SetLinearVelocity(bodyId, vB);
    b2Body_SetAngularVelocity(bodyId, omegaB);
}

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : DEFAULT_STEP_COUNT;
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2Polygon box = b2MakeBox(1.0f, 0.5f);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 20.0f;

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.gravityScale = 1.0f;
    bodyDef.angularDamping = 0.5f;
    bodyDef.linearDamping = 0.2f;
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    b2CreatePolygonShape(bodyId, &shapeDef, &box);

    float impulses[2] = {0.0f, 0.0f};
    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / SAMPLE_HERTZ, 4);
        apply_user_constraint(bodyId, impulses);
    }

    b2Transform transform = b2Body_GetTransform(bodyId);
    b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
    b2Vec2 center = b2Body_GetWorldCenterOfMass(bodyId);
    b2Counters counters = b2World_GetCounters(worldId);
    printf("userConstraint %d %d %d %d %d %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g\n",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           counters.jointCount,
           b2World_GetAwakeBodyCount(worldId),
           transform.p.x,
           transform.p.y,
           transform.q.c,
           transform.q.s,
           velocity.x,
           velocity.y,
           b2Body_GetAngularVelocity(bodyId),
           b2Body_GetMass(bodyId),
           b2Body_GetRotationalInertia(bodyId),
           center.x,
           center.y,
           impulses[0],
           impulses[1],
           impulses[0] * SAMPLE_HERTZ,
           impulses[1] * SAMPLE_HERTZ);

    b2DestroyWorld(worldId);
    return 0;
}
