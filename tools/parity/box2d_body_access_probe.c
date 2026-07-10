// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdint.h>
#include <stdio.h>
#include <string.h>

static void print_vec(const char* label, b2Vec2 v)
{
    printf("%s %.9g %.9g\n", label, v.x, v.y);
}

static void print_aabb(const char* label, b2AABB aabb)
{
    printf("%s %.9g %.9g %.9g %.9g\n", label, aabb.lowerBound.x, aabb.lowerBound.y, aabb.upperBound.x, aabb.upperBound.y);
}

static void print_mass(const char* label, b2BodyId bodyId)
{
    b2MassData mass = b2Body_GetMassData(bodyId);
    printf("%s %.9g %.9g %.9g %.9g\n", label, mass.mass, mass.center.x, mass.center.y, mass.rotationalInertia);
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    uintptr_t userDataValue = 0x1234u;
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.name = "abcdefghijklmnopqrstuvwxyzABCDEZZZ";
    bodyDef.userData = (void*)userDataValue;
    bodyDef.linearDamping = 0.2f;
    bodyDef.angularDamping = 0.3f;
    bodyDef.gravityScale = 0.75f;
    bodyDef.isBullet = true;
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 2.0f;
    b2Circle circle = { { 0.2f, -0.1f }, 0.75f };
    b2ShapeId circleId = b2CreateCircleShape(bodyId, &shapeDef, &circle);
    b2Polygon box = b2MakeBox(0.5f, 0.25f);
    b2ShapeId boxId = b2CreatePolygonShape(bodyId, &shapeDef, &box);
    b2Segment segment = { { -1.0f, 0.0f }, { 1.0f, 0.0f } };
    b2ShapeId segmentId = b2CreateSegmentShape(bodyId, &shapeDef, &segment);

    b2WorldId ownerWorldId = b2Body_GetWorld(bodyId);
    printf("identity %d %s %d %d %d %d %.9g %.9g %.9g %d %d %d %d %d\n",
           b2Body_GetType(bodyId), b2Body_GetName(bodyId), b2Body_GetUserData(bodyId) == (void*)userDataValue,
           b2Body_IsEnabled(bodyId), b2Body_IsBullet(bodyId), b2Body_IsFixedRotation(bodyId),
           b2Body_GetLinearDamping(bodyId), b2Body_GetAngularDamping(bodyId), b2Body_GetGravityScale(bodyId),
           ownerWorldId.index1, ownerWorldId.generation, b2Body_GetShapeCount(bodyId), b2Body_IsValid(bodyId),
           b2Shape_IsValid(circleId));

    b2ShapeId shapes[2] = { 0 };
    int shapeCount = b2Body_GetShapes(bodyId, shapes, 2);
    printf("shapes %d %d %d %d %d %d\n", shapeCount, shapes[0].index1, shapes[0].generation, shapes[1].index1,
           shapes[1].generation, segmentId.index1 == shapes[0].index1 && boxId.index1 == shapes[1].index1);
    print_aabb("aabb0", b2Body_ComputeAABB(bodyId));

    b2Rot rotation = { 0.87758255f, 0.47942555f };
    b2Body_SetTransform(bodyId, (b2Vec2){ 1.5f, -0.75f }, rotation);
    b2Transform transform = b2Body_GetTransform(bodyId);
    printf("transform %.9g %.9g %.9g %.9g\n", transform.p.x, transform.p.y, transform.q.c, transform.q.s);
    print_vec("center", b2Body_GetWorldCenterOfMass(bodyId));
    print_aabb("aabb1", b2Body_ComputeAABB(bodyId));

    b2Body_SetLinearVelocity(bodyId, (b2Vec2){ 3.0f, -2.0f });
    b2Body_SetAngularVelocity(bodyId, 4.0f);
    print_vec("linear", b2Body_GetLinearVelocity(bodyId));
    printf("angular %.9g\n", b2Body_GetAngularVelocity(bodyId));
    print_vec("localVelocity", b2Body_GetLocalPointVelocity(bodyId, (b2Vec2){ 0.3f, -0.4f }));
    print_vec("worldVelocity", b2Body_GetWorldPointVelocity(bodyId, (b2Vec2){ 1.2f, 0.7f }));

    b2Body_SetLinearDamping(bodyId, 0.8f);
    b2Body_SetAngularDamping(bodyId, 0.9f);
    b2Body_SetGravityScale(bodyId, -0.5f);
    printf("tuning %.9g %.9g %.9g\n", b2Body_GetLinearDamping(bodyId), b2Body_GetAngularDamping(bodyId),
           b2Body_GetGravityScale(bodyId));

    print_mass("mass0", bodyId);
    b2Body_SetFixedRotation(bodyId, true);
    b2Body_SetAngularVelocity(bodyId, 9.0f);
    print_mass("mass1", bodyId);
    printf("fixed %d %.9g\n", b2Body_IsFixedRotation(bodyId), b2Body_GetAngularVelocity(bodyId));

    b2Body_EnableContactEvents(bodyId, true);
    b2Body_EnableHitEvents(bodyId, true);
    printf("events %d %d %d %d\n", b2Shape_AreContactEventsEnabled(circleId), b2Shape_AreHitEventsEnabled(circleId),
           b2Shape_AreContactEventsEnabled(boxId), b2Shape_AreHitEventsEnabled(segmentId));

    b2Body_SetName(bodyId, NULL);
    b2Body_SetUserData(bodyId, (void*)0x5678u);
    printf("rename %d %llu\n", strlen(b2Body_GetName(bodyId)) == 0, (unsigned long long)(uintptr_t)b2Body_GetUserData(bodyId));

    b2Body_Disable(bodyId);
    printf("enabled0 %d\n", b2Body_IsEnabled(bodyId));
    b2Body_Enable(bodyId);
    printf("enabled1 %d\n", b2Body_IsEnabled(bodyId));

    b2DestroyWorld(worldId);
    return 0;
}
