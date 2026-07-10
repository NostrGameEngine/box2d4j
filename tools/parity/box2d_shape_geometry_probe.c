// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>

static void print_polygon(const char* label, b2Polygon polygon)
{
    printf("%s %d %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g\n", label, polygon.count, polygon.radius,
           polygon.centroid.x, polygon.centroid.y, polygon.vertices[0].x, polygon.vertices[0].y, polygon.vertices[1].x,
           polygon.vertices[1].y, polygon.normals[0].x, polygon.normals[0].y, polygon.normals[1].x, polygon.normals[1].y);
}

static void print_circle(const char* label, b2Circle circle)
{
    printf("%s %.9g %.9g %.9g\n", label, circle.center.x, circle.center.y, circle.radius);
}

static void print_segment(const char* label, b2Segment segment)
{
    printf("%s %.9g %.9g %.9g %.9g\n", label, segment.point1.x, segment.point1.y, segment.point2.x, segment.point2.y);
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = (b2Vec2){ 0.0f, 0.0f };
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef groundBodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &groundBodyDef);
    b2ShapeDef groundShapeDef = b2DefaultShapeDef();
    b2Polygon groundBox = b2MakeBox(5.0f, 0.5f);
    b2CreatePolygonShape(groundId, &groundShapeDef, &groundBox);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){ 0.0f, 0.75f };
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.0f;
    b2Polygon dynamicBox = b2MakeBox(0.5f, 0.5f);
    b2ShapeId shapeId = b2CreatePolygonShape(bodyId, &shapeDef, &dynamicBox);

    b2World_Step(worldId, 1.0f / 60.0f, 4);
    b2Counters counters = b2World_GetCounters(worldId);
    printf("contacts0 %d\n", counters.contactCount);
    print_polygon("poly0", b2Shape_GetPolygon(shapeId));

    b2Circle circle = { { 0.2f, -0.1f }, 0.35f };
    b2Shape_SetCircle(shapeId, &circle);
    counters = b2World_GetCounters(worldId);
    printf("contacts1 %d\n", counters.contactCount);
    print_circle("circle1", b2Shape_GetCircle(shapeId));

    b2World_Step(worldId, 1.0f / 60.0f, 4);
    counters = b2World_GetCounters(worldId);
    printf("contacts2 %d\n", counters.contactCount);

    b2Segment segment = { { -0.75f, -0.2f }, { 0.9f, -0.2f } };
    b2Shape_SetSegment(shapeId, &segment);
    counters = b2World_GetCounters(worldId);
    printf("contacts3 %d\n", counters.contactCount);
    print_segment("segment1", b2Shape_GetSegment(shapeId));

    b2Polygon offsetBox = b2MakeOffsetBox(0.25f, 0.75f, (b2Vec2){ 0.1f, -0.2f }, b2MakeRot(0.35f));
    b2Shape_SetPolygon(shapeId, &offsetBox);
    counters = b2World_GetCounters(worldId);
    printf("contacts4 %d\n", counters.contactCount);
    print_polygon("poly1", b2Shape_GetPolygon(shapeId));

    b2DestroyWorld(worldId);
    return 0;
}
