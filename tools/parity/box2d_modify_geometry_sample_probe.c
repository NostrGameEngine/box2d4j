// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>

static void print_state(const char* label, b2WorldId worldId, b2BodyId bodyId, b2ShapeId shapeId)
{
    b2Vec2 p = b2Body_GetPosition(bodyId);
    b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
    b2MassData massData = b2Body_GetMassData(bodyId);
    b2AABB aabb = b2Shape_GetAABB(shapeId);
    b2Counters counters = b2World_GetCounters(worldId);

    printf(" %s %d %d %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %d %d",
           label,
           b2Body_GetType(bodyId),
           b2Shape_GetType(shapeId),
           p.x,
           p.y,
           b2Rot_GetAngle(b2Body_GetRotation(bodyId)),
           v.x,
           v.y,
           b2Body_GetAngularVelocity(bodyId),
           massData.mass,
           massData.center.x,
           massData.center.y,
           massData.rotationalInertia,
           aabb.lowerBound.x,
           aabb.lowerBound.y,
           aabb.upperBound.x,
           aabb.upperBound.y,
           counters.contactCount,
           b2World_GetAwakeBodyCount(worldId));
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2Polygon box = b2MakeOffsetBox(10.0f, 1.0f, (b2Vec2){0.0f, -1.0f}, b2Rot_identity);
        b2CreatePolygonShape(groundId, &shapeDef, &box);
    }

    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = (b2Vec2){0.0f, 4.0f};
        b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2Polygon box = b2MakeBox(1.0f, 1.0f);
        b2CreatePolygonShape(bodyId, &shapeDef, &box);
    }

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_kinematicBody;
    bodyDef.position = (b2Vec2){0.0f, 1.0f};
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Circle circle = {{0.0f, 0.0f}, 0.5f};
    b2ShapeId shapeId = b2CreateCircleShape(bodyId, &shapeDef, &circle);

    printf("modifyGeometry");
    print_state("circle", worldId, bodyId, shapeId);

    b2Capsule capsule = {{-0.75f, 0.0f}, {0.0f, 0.75f}, 0.75f};
    b2Shape_SetCapsule(shapeId, &capsule);
    b2Body_ApplyMassFromShapes(bodyId);
    print_state("capsule", worldId, bodyId, shapeId);

    b2Segment segment = {{-0.375f, 0.0f}, {0.5625f, 0.0f}};
    b2Shape_SetSegment(shapeId, &segment);
    b2Body_ApplyMassFromShapes(bodyId);
    print_state("segment", worldId, bodyId, shapeId);

    b2Polygon polygon = b2MakeBox(0.625f, 0.9375f);
    b2Shape_SetPolygon(shapeId, &polygon);
    b2Body_SetType(bodyId, b2_dynamicBody);
    b2Body_ApplyMassFromShapes(bodyId);
    print_state("polygonDynamic", worldId, bodyId, shapeId);

    b2Body_SetType(bodyId, b2_staticBody);
    b2Body_ApplyMassFromShapes(bodyId);
    print_state("polygonStatic", worldId, bodyId, shapeId);
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
