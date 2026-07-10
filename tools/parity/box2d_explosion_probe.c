// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>

static void print_body(const char* label, b2BodyId bodyId)
{
    b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
    printf("%s %.9g %.9g %.9g\n", label, velocity.x, velocity.y, b2Body_GetAngularVelocity(bodyId));
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = (b2Vec2){0.0f, 0.0f};
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;

    bodyDef.position = (b2Vec2){1.0f, 0.2f};
    bodyDef.rotation = b2MakeRot(0.25f);
    b2BodyId circleBody = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.2f;
    shapeDef.filter.categoryBits = 0x0001;
    b2Circle circle = {{0.1f, -0.05f}, 0.35f};
    b2CreateCircleShape(circleBody, &shapeDef, &circle);

    bodyDef.position = (b2Vec2){-0.75f, 0.85f};
    bodyDef.rotation = b2MakeRot(-0.4f);
    b2BodyId capsuleBody = b2CreateBody(worldId, &bodyDef);
    shapeDef = b2DefaultShapeDef();
    shapeDef.density = 0.9f;
    shapeDef.filter.categoryBits = 0x0002;
    b2Capsule capsule = {{-0.25f, 0.0f}, {0.3f, 0.1f}, 0.18f};
    b2CreateCapsuleShape(capsuleBody, &shapeDef, &capsule);

    bodyDef.position = (b2Vec2){0.25f, -1.15f};
    bodyDef.rotation = b2MakeRot(0.7f);
    b2BodyId boxBody = b2CreateBody(worldId, &bodyDef);
    shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.5f;
    shapeDef.filter.categoryBits = 0x0001;
    b2Polygon box = b2MakeOffsetBox(0.35f, 0.25f, (b2Vec2){0.12f, -0.08f}, b2MakeRot(0.2f));
    b2CreatePolygonShape(boxBody, &shapeDef, &box);

    bodyDef.position = (b2Vec2){0.45f, 0.45f};
    bodyDef.rotation = b2MakeRot(0.1f);
    b2BodyId filteredBody = b2CreateBody(worldId, &bodyDef);
    shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.0f;
    shapeDef.filter.categoryBits = 0x0004;
    b2Polygon filteredBox = b2MakeBox(0.2f, 0.2f);
    b2CreatePolygonShape(filteredBody, &shapeDef, &filteredBox);

    b2ExplosionDef explosionDef = b2DefaultExplosionDef();
    explosionDef.maskBits = 0x0003;
    explosionDef.position = (b2Vec2){0.0f, 0.0f};
    explosionDef.radius = 0.45f;
    explosionDef.falloff = 1.25f;
    explosionDef.impulsePerLength = 3.75f;
    b2World_Explode(worldId, &explosionDef);

    print_body("circle", circleBody);
    print_body("capsule", capsuleBody);
    print_body("box", boxBody);
    print_body("filtered", filteredBody);

    b2DestroyWorld(worldId);
    return 0;
}
