// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>

static void print_aabb(b2AABB aabb)
{
    printf(" %.9g %.9g %.9g %.9g",
           aabb.lowerBound.x,
           aabb.lowerBound.y,
           aabb.upperBound.x,
           aabb.upperBound.y);
}

static void print_body(b2BodyId bodyId)
{
    b2Vec2 p = b2Body_GetPosition(bodyId);
    b2MassData massData = b2Body_GetMassData(bodyId);
    b2ShapeId shapes[1];
    int shapeCount = b2Body_GetShapes(bodyId, shapes, 1);
    b2MassData shapeMass = b2Shape_GetMassData(shapes[0]);

    printf(" %.9g %.9g %.9g %d %.9g %.9g %.9g %.9g",
           p.x,
           p.y,
           b2Rot_GetAngle(b2Body_GetRotation(bodyId)),
           shapeCount,
           massData.mass,
           massData.center.x,
           massData.center.y,
           massData.rotationalInertia);
    print_aabb(b2Body_ComputeAABB(bodyId));
    printf(" %d %.9g %.9g %.9g %.9g",
           b2Shape_GetType(shapes[0]),
           shapeMass.mass,
           shapeMass.center.x,
           shapeMass.center.y,
           shapeMass.rotationalInertia);
    print_aabb(b2Shape_GetAABB(shapes[0]));
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyId bodyIds[3];
    int bodyCount = 0;

    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.position = (b2Vec2){-1.0f, 1.0f};
        b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
        bodyIds[bodyCount++] = groundId;

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2Polygon box = b2MakeOffsetBox(1.0f, 1.0f, (b2Vec2){10.0f, -2.0f}, b2MakeRot(0.5f * B2_PI));
        b2CreatePolygonShape(groundId, &shapeDef, &box);
    }

    {
        b2Capsule capsule = {{-5.0f, 1.0f}, {-4.0f, 1.0f}, 0.25f};
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.position = (b2Vec2){13.5f, -0.75f};
        bodyDef.type = b2_dynamicBody;
        b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
        bodyIds[bodyCount++] = bodyId;
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreateCapsuleShape(bodyId, &shapeDef, &capsule);
    }

    {
        b2Polygon box = b2MakeOffsetBox(0.75f, 0.5f, (b2Vec2){9.0f, 2.0f}, b2MakeRot(0.5f * B2_PI));
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.position = (b2Vec2){0.0f, 0.0f};
        bodyDef.type = b2_dynamicBody;
        b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
        bodyIds[bodyCount++] = bodyId;
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreatePolygonShape(bodyId, &shapeDef, &box);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("offsetShapes %d %d %d %d", counters.bodyCount, counters.shapeCount, counters.contactCount, bodyCount);
    for (int i = 0; i < bodyCount; ++i)
    {
        print_body(bodyIds[i]);
    }
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
