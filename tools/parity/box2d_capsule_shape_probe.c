// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>

static void print_mass(const char* label, b2BodyId bodyId)
{
    b2MassData mass = b2Body_GetMassData(bodyId);
    printf("%s %.9g %.9g %.9g %.9g\n", label, mass.mass, mass.center.x, mass.center.y, mass.rotationalInertia);
}

static void print_capsule(const char* label, b2Capsule capsule)
{
    printf("%s %.9g %.9g %.9g %.9g %.9g\n", label, capsule.center1.x, capsule.center1.y, capsule.center2.x,
           capsule.center2.y, capsule.radius);
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = (b2Vec2){ 0.0f, -10.0f };
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef groundBodyDef = b2DefaultBodyDef();
    groundBodyDef.position = (b2Vec2){ 0.0f, -10.0f };
    b2BodyId groundId = b2CreateBody(worldId, &groundBodyDef);
    b2Polygon groundBox = b2MakeBox(50.0f, 10.0f);
    b2ShapeDef groundShapeDef = b2DefaultShapeDef();
    groundShapeDef.enableContactEvents = true;
    b2ShapeId groundShapeId = b2CreatePolygonShape(groundId, &groundShapeDef, &groundBox);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){ 0.0f, 1.15f };
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 2.0f;
    shapeDef.material.friction = 0.35f;
    shapeDef.enableContactEvents = true;
    b2Capsule capsule = { { -0.35f, 0.0f }, { 0.45f, 0.0f }, 0.25f };
    b2ShapeId capsuleShapeId = b2CreateCapsuleShape(bodyId, &shapeDef, &capsule);
    print_capsule("capsule0", b2Shape_GetCapsule(capsuleShapeId));
    print_mass("mass0", bodyId);

    b2World_Step(worldId, 1.0f / 60.0f, 4);
    b2Counters counters = b2World_GetCounters(worldId);
    b2ContactData data[4] = { 0 };
    int contactCount = b2Body_GetContactData(bodyId, data, 4);
    int pointCount = contactCount > 0 ? data[0].manifold.pointCount : 0;
    printf("contacts0 %d %d %d %d\n", counters.contactCount, b2Shape_GetContactCapacity(capsuleShapeId),
           b2Shape_GetContactCapacity(groundShapeId), pointCount);

    b2Capsule capsule2 = { { -0.15f, -0.1f }, { 0.15f, 0.6f }, 0.2f };
    b2Shape_SetCapsule(capsuleShapeId, &capsule2);
    print_capsule("capsule1", b2Shape_GetCapsule(capsuleShapeId));
    counters = b2World_GetCounters(worldId);
    printf("contacts1 %d\n", counters.contactCount);
    print_mass("mass1", bodyId);
    b2Body_ApplyMassFromShapes(bodyId);
    print_mass("mass2", bodyId);

    b2DestroyWorld(worldId);
    return 0;
}
