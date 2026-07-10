// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>

typedef struct Counts
{
    int segments;
    int points;
    int strings;
} Counts;

static void draw_segment(b2Vec2 p1, b2Vec2 p2, b2HexColor color, void* context)
{
    Counts* counts = context;
    counts->segments += 1;
    printf("segment %.9g %.9g %.9g %.9g %06x\n", p1.x, p1.y, p2.x, p2.y, color);
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

static b2WorldId create_world(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = (b2Vec2){0.0f, -10.0f};
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef groundDef = b2DefaultBodyDef();
    groundDef.position = (b2Vec2){0.0f, -0.5f};
    b2BodyId groundId = b2CreateBody(worldId, &groundDef);
    b2ShapeDef groundShapeDef = b2DefaultShapeDef();
    groundShapeDef.material.friction = 0.7f;
    b2Polygon groundBox = b2MakeBox(3.0f, 0.5f);
    b2CreatePolygonShape(groundId, &groundShapeDef, &groundBox);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){0.0f, 0.45f};
    bodyDef.linearVelocity = (b2Vec2){1.5f, -1.0f};
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.0f;
    shapeDef.material.friction = 0.5f;
    b2Polygon box = b2MakeBox(0.4f, 0.4f);
    b2CreatePolygonShape(bodyId, &shapeDef, &box);

    b2World_Step(worldId, 1.0f / 60.0f, 4);
    return worldId;
}

int main(void)
{
    Counts counts = {0};
    b2DebugDraw draw = b2DefaultDebugDraw();
    draw.DrawSegmentFcn = draw_segment;
    draw.DrawPointFcn = draw_point;
    draw.DrawStringFcn = draw_string;
    draw.drawContacts = true;
    draw.drawContactImpulses = true;
    draw.drawContactFeatures = true;
    draw.drawFrictionImpulses = true;
    draw.context = &counts;

    b2WorldId worldId = create_world();
    b2World_Draw(worldId, &draw);
    printf("counts %d %d %d\n", counts.segments, counts.points, counts.strings);

    counts = (Counts){0};
    draw.drawContactImpulses = false;
    draw.drawContactFeatures = false;
    draw.drawFrictionImpulses = false;
    draw.drawContactNormals = true;
    draw.drawGraphColors = true;
    b2World_Draw(worldId, &draw);
    printf("countsGraphNormals %d %d %d\n", counts.segments, counts.points, counts.strings);

    b2DestroyWorld(worldId);
    return 0;
}
