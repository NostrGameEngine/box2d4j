// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>
#include <stdlib.h>

static void print_body(b2BodyId bodyId)
{
    b2Vec2 p = b2Body_GetPosition(bodyId);
    b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
    printf(" %.9g %.9g %.9g %.9g %.9g %.9g",
           p.x,
           p.y,
           b2Rot_GetAngle(b2Body_GetRotation(bodyId)),
           v.x,
           v.y,
           b2Body_GetAngularVelocity(bodyId));
}

static b2BodyId create_body(b2WorldId worldId, int shapeType, b2ChainId* chainId)
{
    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);

    b2Vec2 points[] = {
        {-56.885498f, 12.8985004f},
        {-56.885498f, 16.2057495f},
        {56.885498f, 16.2057495f},
        {56.885498f, -16.2057514f},
        {51.5935059f, -16.2057514f},
        {43.6559982f, -10.9139996f},
        {35.7184982f, -10.9139996f},
        {27.7809982f, -10.9139996f},
        {21.1664963f, -14.2212505f},
        {11.9059982f, -16.2057514f},
        {0.0f, -16.2057514f},
        {-10.5835037f, -14.8827496f},
        {-17.1980019f, -13.5597477f},
        {-21.1665001f, -12.2370014f},
        {-25.1355019f, -9.5909977f},
        {-31.75f, -3.63799858f},
        {-38.3644981f, 6.2840004f},
        {-42.3334999f, 9.59125137f},
        {-47.625f, 11.5755005f},
        {-56.885498f, 12.8985004f},
    };

    b2SurfaceMaterial material = {0};
    material.friction = 0.2f;
    material.customColor = b2_colorSteelBlue;
    material.userMaterialId = 42;

    b2ChainDef chainDef = b2DefaultChainDef();
    chainDef.points = points;
    chainDef.count = (int)(sizeof(points) / sizeof(points[0]));
    chainDef.materials = &material;
    chainDef.materialCount = 1;
    chainDef.isLoop = true;
    *chainId = b2CreateChain(groundId, &chainDef);

    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){-55.0f, 13.5f};
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.0f;
    shapeDef.material.friction = 0.2f;
    shapeDef.material.restitution = 0.0f;

    if (shapeType == 0)
    {
        b2Circle circle = {{0.0f, 0.0f}, 0.5f};
        b2CreateCircleShape(bodyId, &shapeDef, &circle);
    }
    else if (shapeType == 1)
    {
        b2Capsule capsule = {{-0.5f, 0.0f}, {0.5f, 0.0f}, 0.25f};
        b2CreateCapsuleShape(bodyId, &shapeDef, &capsule);
    }
    else
    {
        b2Polygon box = b2MakeBox(0.5f, 0.5f);
        b2CreatePolygonShape(bodyId, &shapeDef, &box);
    }

    return bodyId;
}

static void run_variant(int shapeType, int stepCount)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2ChainId chainId;
    b2BodyId bodyId = create_body(worldId, shapeType, &chainId);

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf(" %d %d %d %d %d %d",
           shapeType,
           b2Chain_GetSegmentCount(chainId),
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           b2World_GetAwakeBodyCount(worldId));
    print_body(bodyId);

    b2DestroyWorld(worldId);
}

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : 240;
    printf("chainShape 3");
    run_variant(0, stepCount);
    run_variant(1, stepCount);
    run_variant(2, stepCount);
    printf("\n");
    return 0;
}
