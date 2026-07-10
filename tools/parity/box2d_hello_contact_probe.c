// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = (b2Vec2){0.0f, -10.0f};
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef groundBodyDef = b2DefaultBodyDef();
    groundBodyDef.position = (b2Vec2){0.0f, -10.0f};
    b2BodyId groundId = b2CreateBody(worldId, &groundBodyDef);
    b2Polygon groundBox = b2MakeBox(50.0f, 10.0f);
    b2ShapeDef groundShapeDef = b2DefaultShapeDef();
    b2ShapeId groundShapeId = b2CreatePolygonShape(groundId, &groundShapeDef, &groundBox);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){0.0f, 4.0f};
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    b2Polygon dynamicBox = b2MakeBox(1.0f, 1.0f);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.0f;
    shapeDef.material.friction = 0.3f;
    b2ShapeId dynamicShapeId = b2CreatePolygonShape(bodyId, &shapeDef, &dynamicBox);

    for (int i = 0; i < 90; ++i)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Vec2 p = b2Body_GetPosition(bodyId);
    b2Rot q = b2Body_GetRotation(bodyId);
    b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
    printf("body %.9g %.9g %.9g %.9g %.9g %.9g\n", p.x, p.y, b2Rot_GetAngle(q), v.x, v.y,
           b2Body_GetAngularVelocity(bodyId));

    b2ContactData data[4] = {0};
    int count = b2Body_GetContactData(bodyId, data, 4);
    printf("count %d ground %d dynamic %d\n", count, groundShapeId.index1, dynamicShapeId.index1);
    if (count > 0)
    {
        b2Manifold* m = &data[0].manifold;
        printf("ids %d %d normal %.9g %.9g points %d rolling %.9g\n", data[0].shapeIdA.index1, data[0].shapeIdB.index1,
               m->normal.x, m->normal.y, m->pointCount, m->rollingImpulse);
        for (int i = 0; i < m->pointCount; ++i)
        {
            b2ManifoldPoint* mp = m->points + i;
            printf("point%d %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %d %d\n", i, mp->anchorA.x,
                   mp->anchorA.y, mp->anchorB.x, mp->anchorB.y, mp->separation, mp->normalImpulse, mp->tangentImpulse,
                   mp->totalNormalImpulse, mp->normalVelocity, mp->id, mp->persisted ? 1 : 0);
        }
    }

    b2DestroyWorld(worldId);
    return 0;
}
