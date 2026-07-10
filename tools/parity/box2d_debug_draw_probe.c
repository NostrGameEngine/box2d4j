// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>

typedef struct Counts
{
    int polygons;
    int solidPolygons;
    int solidCircles;
    int solidCapsules;
    int segments;
    int transforms;
    int points;
    int strings;
} Counts;

static void draw_polygon(const b2Vec2* vertices, int vertexCount, b2HexColor color, void* context)
{
    Counts* counts = context;
    counts->polygons += 1;
    printf("polygon %d %.9g %.9g %.9g %.9g %06x\n", vertexCount, vertices[0].x, vertices[0].y,
           vertices[2].x, vertices[2].y, color);
}

static void draw_solid_polygon(b2Transform transform, const b2Vec2* vertices, int vertexCount, float radius, b2HexColor color,
                               void* context)
{
    Counts* counts = context;
    counts->solidPolygons += 1;
    printf("solidPolygon %d %.9g %.9g %.9g %.9g %.9g %.9g %06x\n", vertexCount, transform.p.x, transform.p.y,
           transform.q.c, transform.q.s, radius, vertices[0].x, color);
}

static void draw_solid_circle(b2Transform transform, float radius, b2HexColor color, void* context)
{
    Counts* counts = context;
    counts->solidCircles += 1;
    printf("solidCircle %.9g %.9g %.9g %.9g %.9g %06x\n", transform.p.x, transform.p.y, transform.q.c, transform.q.s,
           radius, color);
}

static void draw_solid_capsule(b2Vec2 p1, b2Vec2 p2, float radius, b2HexColor color, void* context)
{
    Counts* counts = context;
    counts->solidCapsules += 1;
    printf("solidCapsule %.9g %.9g %.9g %.9g %.9g %06x\n", p1.x, p1.y, p2.x, p2.y, radius, color);
}

static void draw_segment(b2Vec2 p1, b2Vec2 p2, b2HexColor color, void* context)
{
    Counts* counts = context;
    counts->segments += 1;
    printf("segment %.9g %.9g %.9g %.9g %06x\n", p1.x, p1.y, p2.x, p2.y, color);
}

static void draw_transform(b2Transform transform, void* context)
{
    Counts* counts = context;
    counts->transforms += 1;
    printf("transform %.9g %.9g %.9g %.9g\n", transform.p.x, transform.p.y, transform.q.c, transform.q.s);
}

static void draw_point(b2Vec2 p, float size, b2HexColor color, void* context)
{
    Counts* counts = context;
    counts->points += 1;
    printf("point %.9g %.9g %.9g %06x\n", p.x, p.y, size, color);
}

static void draw_string(b2Vec2 p, const char* s, b2HexColor color, void* context)
{
    Counts* counts = context;
    counts->strings += 1;
    printf("string %.9g %.9g %s %06x\n", p.x, p.y, s, color);
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = (b2Vec2){0.0f, 0.0f};
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.name = "ground";
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Polygon box = b2MakeBox(1.0f, 0.5f);
    b2CreatePolygonShape(groundId, &shapeDef, &box);
    b2CreateSegmentShape(groundId, &shapeDef, &(b2Segment){{-2.0f, 1.0f}, {-1.0f, 1.5f}});

    bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.name = "ball";
    bodyDef.position = (b2Vec2){2.0f, 3.0f};
    bodyDef.rotation = b2MakeRot(0.25f);
    b2BodyId dynamicId = b2CreateBody(worldId, &bodyDef);
    shapeDef.density = 1.0f;
    b2CreateCircleShape(dynamicId, &shapeDef, &(b2Circle){{0.25f, -0.5f}, 0.75f});
    b2CreateCapsuleShape(dynamicId, &shapeDef, &(b2Capsule){{-0.5f, 0.0f}, {0.5f, 0.0f}, 0.2f});

    Counts counts = {0};
    b2DebugDraw draw = b2DefaultDebugDraw();
    draw.DrawPolygonFcn = draw_polygon;
    draw.DrawSolidPolygonFcn = draw_solid_polygon;
    draw.DrawSolidCircleFcn = draw_solid_circle;
    draw.DrawSolidCapsuleFcn = draw_solid_capsule;
    draw.DrawSegmentFcn = draw_segment;
    draw.DrawTransformFcn = draw_transform;
    draw.DrawPointFcn = draw_point;
    draw.DrawStringFcn = draw_string;
    draw.drawShapes = true;
    draw.drawBounds = true;
    draw.drawBodyNames = true;
    draw.drawMass = true;
    draw.context = &counts;

    b2World_Draw(worldId, &draw);
    printf("counts %d %d %d %d %d %d %d %d\n", counts.polygons, counts.solidPolygons, counts.solidCircles,
           counts.solidCapsules, counts.segments, counts.transforms, counts.points, counts.strings);

    b2DestroyWorld(worldId);
    return 0;
}
