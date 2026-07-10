// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>

static b2BodyId create_dynamic_circle(b2WorldId worldId)
{
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.linearDamping = 0.0f;
    bodyDef.angularDamping = 0.0f;
    bodyDef.gravityScale = 0.0f;
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 2.0f;
    b2Circle circle = { { 0.25f, -0.1f }, 0.8f };
    b2CreateCircleShape(bodyId, &shapeDef, &circle);
    return bodyId;
}

static void print_state(const char* label, b2BodyId bodyId)
{
    b2Vec2 p = b2Body_GetPosition(bodyId);
    b2Rot q = b2Body_GetRotation(bodyId);
    b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
    printf("%s %.9g %.9g %.9g %.9g %.9g %.9g %.9g\n", label, p.x, p.y, q.c, q.s, v.x, v.y,
           b2Body_GetAngularVelocity(bodyId));
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = (b2Vec2){ 0.0f, 0.0f };
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyId impulseBody = create_dynamic_circle(worldId);
    b2Body_SetLinearVelocity(impulseBody, (b2Vec2){ 1.0f, -2.0f });
    b2Body_SetAngularVelocity(impulseBody, 0.25f);
    b2Vec2 impulseCenter = b2Body_GetWorldCenterOfMass(impulseBody);
    b2Body_ApplyLinearImpulse(impulseBody, (b2Vec2){ 1.5f, -0.75f }, b2Add(impulseCenter, (b2Vec2){ 0.4f, -0.2f }), true);
    b2Body_ApplyLinearImpulseToCenter(impulseBody, (b2Vec2){ -0.5f, 2.25f }, true);
    b2Body_ApplyAngularImpulse(impulseBody, -0.35f, true);
    print_state("impulse", impulseBody);

    b2BodyId forceBody = create_dynamic_circle(worldId);
    b2Body_SetLinearVelocity(forceBody, (b2Vec2){ -0.5f, 0.75f });
    b2Body_SetAngularVelocity(forceBody, -0.2f);
    b2Vec2 forceCenter = b2Body_GetWorldCenterOfMass(forceBody);
    b2Body_ApplyForce(forceBody, (b2Vec2){ 2.0f, 3.0f }, b2Add(forceCenter, (b2Vec2){ 0.5f, -0.25f }), true);
    b2Body_ApplyForceToCenter(forceBody, (b2Vec2){ -1.0f, 4.0f }, true);
    b2Body_ApplyTorque(forceBody, 0.75f, true);
    b2World_Step(worldId, 1.0f / 30.0f, 3);
    print_state("force1", forceBody);
    b2World_Step(worldId, 1.0f / 30.0f, 3);
    print_state("force2", forceBody);

    b2DestroyWorld(worldId);
    return 0;
}
