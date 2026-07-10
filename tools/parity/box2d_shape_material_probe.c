// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.material.friction = 0.25f;
    shapeDef.material.restitution = 0.125f;
    shapeDef.material.rollingResistance = 0.05f;
    shapeDef.material.tangentSpeed = 0.75f;
    shapeDef.material.userMaterialId = 7;
    shapeDef.material.customColor = 1234;
    b2Polygon box = b2MakeBox(1.0f, 1.0f);
    b2ShapeId shapeId = b2CreatePolygonShape(bodyId, &shapeDef, &box);

    shapeDef.material.friction = 0.9f;
    printf("initial %.9g %.9g %d\n", b2Shape_GetFriction(shapeId), b2Shape_GetRestitution(shapeId),
           b2Shape_GetMaterial(shapeId));

    b2Shape_SetFriction(shapeId, 0.4f);
    b2Shape_SetRestitution(shapeId, 0.6f);
    b2Shape_SetMaterial(shapeId, 11);
    printf("set %.9g %.9g %d\n", b2Shape_GetFriction(shapeId), b2Shape_GetRestitution(shapeId),
           b2Shape_GetMaterial(shapeId));

    b2SurfaceMaterial material = b2Shape_GetSurfaceMaterial(shapeId);
    printf("surface %.9g %.9g %.9g %.9g %d %u\n", material.friction, material.restitution,
           material.rollingResistance, material.tangentSpeed, material.userMaterialId, material.customColor);

    material.friction = 0.8f;
    material.restitution = 0.2f;
    material.rollingResistance = 0.3f;
    material.tangentSpeed = -0.5f;
    material.userMaterialId = 13;
    material.customColor = 5678;
    b2Shape_SetSurfaceMaterial(shapeId, material);
    material = b2Shape_GetSurfaceMaterial(shapeId);
    printf("surface2 %.9g %.9g %.9g %.9g %d %u\n", material.friction, material.restitution,
           material.rollingResistance, material.tangentSpeed, material.userMaterialId, material.customColor);

    b2DestroyWorld(worldId);
    return 0;
}
