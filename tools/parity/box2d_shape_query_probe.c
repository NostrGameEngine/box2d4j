// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

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

    b2DestroyWorld(worldId);
    return 0;
}
