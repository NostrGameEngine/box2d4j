// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"

#include <stdio.h>

static void DrawPolygon(const b2Vec2* vertices, int count, b2HexColor color, void* context)
{
    (void)context;
    printf("polygon %d %.9g %.9g %.9g %.9g %06x\n", count,
           vertices[0].x, vertices[0].y, vertices[2].x, vertices[2].y, color);
}

static void DrawCircle(b2Vec2 center, float radius, b2HexColor color, void* context)
{
    (void)context;
    printf("circle %.9g %.9g %.9g %06x\n", center.x, center.y, radius, color);
}

static void DrawSegment(b2Vec2 p1, b2Vec2 p2, b2HexColor color, void* context)
{
    (void)context;
    printf("segment %.9g %.9g %.9g %.9g %06x\n", p1.x, p1.y, p2.x, p2.y, color);
}

static void DrawPoint(b2Vec2 point, float size, b2HexColor color, void* context)
{
    (void)context;
    printf("point %.9g %.9g %.9g %06x\n", point.x, point.y, size, color);
}

static void DrawString(b2Vec2 point, const char* text, b2HexColor color, void* context)
{
    (void)context;
    printf("string %.9g %.9g %s %06x\n", point.x, point.y, text, color);
}

static b2DebugDraw MakeDraw(void)
{
    b2DebugDraw draw = b2DefaultDebugDraw();
    draw.DrawPolygonFcn = DrawPolygon;
    draw.DrawCircleFcn = DrawCircle;
    draw.DrawSegmentFcn = DrawSegment;
    draw.DrawPointFcn = DrawPoint;
    draw.DrawStringFcn = DrawString;
    return draw;
}

static void DrawJoints(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = b2Vec2_zero;
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.position = (b2Vec2){-1.0f, 0.0f};
    bodyDef.rotation = b2MakeRot(-0.15f);
    b2BodyId bodyA = b2CreateBody(worldId, &bodyDef);
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){2.0f, 1.0f};
    bodyDef.rotation = b2MakeRot(0.35f);
    b2BodyId bodyB = b2CreateBody(worldId, &bodyDef);

    b2DistanceJointDef distance = b2DefaultDistanceJointDef();
    distance.bodyIdA = bodyA;
    distance.bodyIdB = bodyB;
    distance.localAnchorA = (b2Vec2){0.1f, 0.2f};
    distance.localAnchorB = (b2Vec2){-0.2f, 0.3f};
    distance.length = 3.0f;
    distance.hertz = 2.0f;
    distance.enableSpring = true;
    distance.enableLimit = true;
    distance.minLength = 1.0f;
    distance.maxLength = 4.0f;
    b2CreateDistanceJoint(worldId, &distance);

    b2FilterJointDef filter = b2DefaultFilterJointDef();
    filter.bodyIdA = bodyA;
    filter.bodyIdB = bodyB;
    b2CreateFilterJoint(worldId, &filter);

    b2MotorJointDef motor = b2DefaultMotorJointDef();
    motor.bodyIdA = bodyA;
    motor.bodyIdB = bodyB;
    b2CreateMotorJoint(worldId, &motor);

    b2MouseJointDef mouse = b2DefaultMouseJointDef();
    mouse.bodyIdA = bodyA;
    mouse.bodyIdB = bodyB;
    mouse.target = (b2Vec2){1.0f, 2.0f};
    mouse.maxForce = 10.0f;
    b2CreateMouseJoint(worldId, &mouse);

    b2PrismaticJointDef prismatic = b2DefaultPrismaticJointDef();
    prismatic.bodyIdA = bodyA;
    prismatic.bodyIdB = bodyB;
    prismatic.localAxisA = (b2Vec2){1.0f, 0.25f};
    prismatic.enableLimit = true;
    prismatic.lowerTranslation = -1.0f;
    prismatic.upperTranslation = 2.0f;
    b2CreatePrismaticJoint(worldId, &prismatic);

    b2RevoluteJointDef revolute = b2DefaultRevoluteJointDef();
    revolute.bodyIdA = bodyA;
    revolute.bodyIdB = bodyB;
    revolute.referenceAngle = 0.2f;
    revolute.enableLimit = true;
    revolute.lowerAngle = -0.5f;
    revolute.upperAngle = 0.7f;
    revolute.drawSize = 0.4f;
    b2CreateRevoluteJoint(worldId, &revolute);

    b2WeldJointDef weld = b2DefaultWeldJointDef();
    weld.bodyIdA = bodyA;
    weld.bodyIdB = bodyB;
    b2CreateWeldJoint(worldId, &weld);

    b2WheelJointDef wheel = b2DefaultWheelJointDef();
    wheel.bodyIdA = bodyA;
    wheel.bodyIdB = bodyB;
    wheel.localAxisA = (b2Vec2){1.0f, 0.25f};
    wheel.enableLimit = true;
    wheel.lowerTranslation = -1.0f;
    wheel.upperTranslation = 2.0f;
    b2CreateWheelJoint(worldId, &wheel);

    b2DebugDraw draw = MakeDraw();
    draw.drawJoints = true;
    draw.drawJointExtras = true;
    printf("joints\n");
    b2World_Draw(worldId, &draw);
    b2DestroyWorld(worldId);
}

static void DrawIslands(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){-1.0f, 0.0f};
    b2BodyId bodyA = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Polygon square = b2MakeSquare(0.5f);
    b2CreatePolygonShape(bodyA, &shapeDef, &square);
    bodyDef.position = (b2Vec2){2.0f, 1.0f};
    b2BodyId bodyB = b2CreateBody(worldId, &bodyDef);
    square = b2MakeSquare(0.75f);
    b2CreatePolygonShape(bodyB, &shapeDef, &square);
    b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
    jointDef.bodyIdA = bodyA;
    jointDef.bodyIdB = bodyB;
    b2CreateRevoluteJoint(worldId, &jointDef);

    b2DebugDraw draw = MakeDraw();
    draw.drawIslands = true;
    printf("islands\n");
    b2World_Draw(worldId, &draw);
    b2DestroyWorld(worldId);
}

int main(void)
{
    DrawJoints();
    DrawIslands();
    return 0;
}
