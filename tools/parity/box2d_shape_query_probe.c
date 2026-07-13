// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdint.h>
#include <stdio.h>

static void print_aabb(const char* label, b2ShapeId shapeId)
{
    b2AABB aabb = b2Shape_GetAABB(shapeId);
    printf("%s %.9g %.9g %.9g %.9g\n", label, aabb.lowerBound.x, aabb.lowerBound.y, aabb.upperBound.x,
           aabb.upperBound.y);
}

static void print_mass(const char* label, b2ShapeId shapeId)
{
    b2MassData mass = b2Shape_GetMassData(shapeId);
    printf("%s %.9g %.9g %.9g %.9g\n", label, mass.mass, mass.center.x, mass.center.y, mass.rotationalInertia);
}

static void print_ray(const char* label, b2ShapeId shapeId, b2RayCastInput input)
{
    b2CastOutput output = b2Shape_RayCast(shapeId, &input);
    printf("%s %d %.9g %.9g %.9g %.9g %.9g %d\n", label, output.hit ? 1 : 0, output.point.x, output.point.y,
           output.normal.x, output.normal.y, output.fraction, output.iterations);
}

static void print_point(const char* label, b2ShapeId shapeId, b2Vec2 point)
{
    b2Vec2 closest = b2Shape_GetClosestPoint(shapeId, point);
    printf("%s %d %.9g %.9g\n", label, b2Shape_TestPoint(shapeId, point) ? 1 : 0, closest.x, closest.y);
}

static uint32_t random_u32(uint32_t* state)
{
    *state = *state * 1664525u + 1013904223u;
    return *state;
}

static float random_grid_float(uint32_t* state)
{
    return ((int)(random_u32(state) % 4097u) - 2048) / 256.0f;
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = (b2Vec2){ 0.0f, 0.0f };
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.position = (b2Vec2){ 1.25f, -0.5f };
    bodyDef.rotation = b2MakeRot(0.35f);
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.7f;
    b2Polygon polygon = b2MakeOffsetBox(0.6f, 0.25f, (b2Vec2){ 0.1f, 0.2f }, b2MakeRot(-0.2f));
    b2ShapeId polygonShapeId = b2CreatePolygonShape(bodyId, &shapeDef, &polygon);
    b2Capsule capsule = { { -0.4f, -0.15f }, { 0.35f, 0.45f }, 0.18f };
    b2ShapeId capsuleShapeId = b2CreateCapsuleShape(bodyId, &shapeDef, &capsule);
    b2Circle circle = { { 0.15f, -0.25f }, 0.32f };
    b2ShapeId circleShapeId = b2CreateCircleShape(bodyId, &shapeDef, &circle);
    b2Segment segment = { { -0.7f, -0.4f }, { 0.8f, -0.1f } };
    b2ShapeId segmentShapeId = b2CreateSegmentShape(bodyId, &shapeDef, &segment);
    b2Vec2 chainPoints[] = { { -1.5f, 0.4f }, { -0.75f, 0.1f }, { 0.75f, 0.1f }, { 1.5f, 0.4f } };
    b2ChainDef chainDef = b2DefaultChainDef();
    chainDef.points = chainPoints;
    chainDef.count = 4;
    b2ChainId chainId = b2CreateChain(bodyId, &chainDef);
    b2ShapeId chainShapeId;
    b2Chain_GetSegments(chainId, &chainShapeId, 1);

    print_aabb("aabb-poly", polygonShapeId);
    print_mass("mass-poly", polygonShapeId);
    print_point("point-poly", polygonShapeId, (b2Vec2){ 1.35f, -0.25f });
    print_ray("ray-poly", polygonShapeId,
              (b2RayCastInput){ .origin = { -0.25f, -0.5f }, .translation = { 3.0f, 0.2f }, .maxFraction = 1.0f });

    print_aabb("aabb-capsule", capsuleShapeId);
    print_mass("mass-capsule", capsuleShapeId);
    print_point("point-capsule", capsuleShapeId, (b2Vec2){ 1.1f, -0.5f });
    print_ray("ray-capsule", capsuleShapeId,
              (b2RayCastInput){ .origin = { 0.1f, -1.0f }, .translation = { 2.4f, 1.4f }, .maxFraction = 1.0f });

    print_aabb("aabb-circle", circleShapeId);
    print_mass("mass-circle", circleShapeId);
    print_point("point-circle", circleShapeId, (b2Vec2){ 1.35f, -0.85f });
    print_ray("ray-circle", circleShapeId,
              (b2RayCastInput){ .origin = { 0.4f, -1.1f }, .translation = { 2.0f, 0.8f }, .maxFraction = 1.0f });

    print_aabb("aabb-segment", segmentShapeId);
    print_mass("mass-segment", segmentShapeId);
    print_point("point-segment", segmentShapeId, (b2Vec2){ 1.0f, -0.8f });
    print_ray("ray-segment", segmentShapeId,
              (b2RayCastInput){ .origin = { 0.3f, -1.0f }, .translation = { 1.6f, 1.0f }, .maxFraction = 1.0f });

    print_ray("ray-chain-front", chainShapeId,
              (b2RayCastInput){ .origin = { 1.25f, -1.5f }, .translation = { 0.0f, 2.5f }, .maxFraction = 1.0f });
    print_ray("ray-chain-back", chainShapeId,
              (b2RayCastInput){ .origin = { 1.25f, 0.5f }, .translation = { 0.0f, -2.5f }, .maxFraction = 1.0f });

    b2ShapeId shapeIds[] = { polygonShapeId, capsuleShapeId, circleShapeId, segmentShapeId, chainShapeId };
    uint32_t state = 0x8d12e47bu;
    for (int i = 0; i < 512; ++i)
    {
        int shapeIndex = (int)(random_u32(&state) % 5u);
        b2RayCastInput input = {
            .origin = { random_grid_float(&state), random_grid_float(&state) },
            .translation = { random_grid_float(&state), random_grid_float(&state) },
            .maxFraction = (float)(1u + random_u32(&state) % 8u) * 0.125f,
        };
        b2CastOutput output = b2Shape_RayCast(shapeIds[shapeIndex], &input);
        printf("fuzz-ray %d %.9g %.9g %.9g %.9g %.9g %d %.9g %.9g %.9g %.9g %.9g %d\n", shapeIndex,
               input.origin.x, input.origin.y, input.translation.x, input.translation.y, input.maxFraction,
               output.hit ? 1 : 0, output.point.x, output.point.y, output.normal.x, output.normal.y, output.fraction,
               output.iterations);
    }

    b2DestroyWorld(worldId);
    return 0;
}
