// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>
#include <stdlib.h>

static void print_aabb(b2AABB aabb)
{
    printf(" %.9g %.9g %.9g %.9g", aabb.lowerBound.x, aabb.lowerBound.y, aabb.upperBound.x, aabb.upperBound.y);
}

static void print_body(b2BodyId bodyId)
{
    b2Vec2 p = b2Body_GetPosition(bodyId);
    b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
    b2MassData massData = b2Body_GetMassData(bodyId);
    printf(" %.9g %.9g %.9g %.9g %.9g %.9g %d %.9g %.9g %.9g %.9g",
           p.x,
           p.y,
           b2Rot_GetAngle(b2Body_GetRotation(bodyId)),
           v.x,
           v.y,
           b2Body_GetAngularVelocity(bodyId),
           b2Body_GetShapeCount(bodyId),
           massData.mass,
           massData.center.x,
           massData.center.y,
           massData.rotationalInertia);
    print_aabb(b2Body_ComputeAABB(bodyId));
}

static b2Polygon make_triangle(b2Vec2 a, b2Vec2 b, b2Vec2 c)
{
    b2Vec2 vertices[3] = {a, b, c};
    b2Hull hull = b2ComputeHull(vertices, 3);
    return b2MakePolygon(&hull, 0.0f);
}

static b2BodyId create_table(b2WorldId worldId, float x, float y, float legHeight)
{
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){x, y};
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Polygon top = b2MakeOffsetBox(3.0f, 0.5f, (b2Vec2){0.0f, 3.5f}, b2Rot_identity);
    b2Polygon leftLeg = b2MakeOffsetBox(0.5f, legHeight, (b2Vec2){-2.5f, legHeight}, b2Rot_identity);
    b2Polygon rightLeg = b2MakeOffsetBox(0.5f, legHeight, (b2Vec2){2.5f, legHeight}, b2Rot_identity);
    b2CreatePolygonShape(bodyId, &shapeDef, &top);
    b2CreatePolygonShape(bodyId, &shapeDef, &leftLeg);
    b2CreatePolygonShape(bodyId, &shapeDef, &rightLeg);
    return bodyId;
}

static b2BodyId create_ship1(b2WorldId worldId)
{
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){5.0f, 1.0f};
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Polygon left = make_triangle((b2Vec2){-2.0f, 0.0f}, (b2Vec2){0.0f, 4.0f / 3.0f}, (b2Vec2){0.0f, 4.0f});
    b2Polygon right = make_triangle((b2Vec2){2.0f, 0.0f}, (b2Vec2){0.0f, 4.0f / 3.0f}, (b2Vec2){0.0f, 4.0f});
    b2CreatePolygonShape(bodyId, &shapeDef, &left);
    b2CreatePolygonShape(bodyId, &shapeDef, &right);
    return bodyId;
}

static b2BodyId create_ship2(b2WorldId worldId)
{
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){15.0f, 1.0f};
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Polygon left = make_triangle((b2Vec2){-2.0f, 0.0f}, (b2Vec2){1.0f, 2.0f}, (b2Vec2){0.0f, 4.0f});
    b2Polygon right = make_triangle((b2Vec2){2.0f, 0.0f}, (b2Vec2){-1.0f, 2.0f}, (b2Vec2){0.0f, 4.0f});
    b2CreatePolygonShape(bodyId, &shapeDef, &left);
    b2CreatePolygonShape(bodyId, &shapeDef, &right);
    return bodyId;
}

static b2BodyId create_table_obstruction(b2WorldId worldId, b2BodyId sourceId)
{
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = b2Body_GetPosition(sourceId);
    bodyDef.rotation = b2Body_GetRotation(sourceId);
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Polygon box = b2MakeOffsetBox(4.0f, 0.1f, (b2Vec2){0.0f, 3.0f}, b2Rot_identity);
    b2CreatePolygonShape(bodyId, &shapeDef, &box);
    return bodyId;
}

static b2BodyId create_ship_obstruction(b2WorldId worldId, b2BodyId sourceId)
{
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = b2Body_GetPosition(sourceId);
    bodyDef.rotation = b2Body_GetRotation(sourceId);
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Circle circle = {{0.0f, 2.0f}, 0.5f};
    b2CreateCircleShape(bodyId, &shapeDef, &circle);
    return bodyId;
}

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : 10;

    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Segment segment = {{50.0f, 0.0f}, {-50.0f, 0.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &segment);

    b2BodyId bodies[8];
    bodies[0] = create_table(worldId, -15.0f, 1.0f, 1.5f);
    bodies[1] = create_table(worldId, -5.0f, 1.0f, 2.0f);
    bodies[2] = create_ship1(worldId);
    bodies[3] = create_ship2(worldId);
    bodies[4] = create_table_obstruction(worldId, bodies[0]);
    bodies[5] = create_table_obstruction(worldId, bodies[1]);
    bodies[6] = create_ship_obstruction(worldId, bodies[2]);
    bodies[7] = create_ship_obstruction(worldId, bodies[3]);

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("compoundShapes %d %d %d %d %d",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           b2World_GetAwakeBodyCount(worldId),
           8);
    for (int i = 0; i < 8; ++i)
    {
        print_body(bodies[i]);
    }
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
