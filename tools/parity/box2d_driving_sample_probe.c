// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>
#include <stdlib.h>

enum
{
    DEFAULT_STEP_COUNT = 120,
    BRIDGE_COUNT = 20,
    BOX_COUNT = 5
};

typedef struct CarState
{
    b2BodyId chassisId;
    b2BodyId rearWheelId;
    b2BodyId frontWheelId;
    b2JointId rearAxleId;
    b2JointId frontAxleId;
} CarState;

static void print_body(b2BodyId bodyId)
{
    b2Transform transform = b2Body_GetTransform(bodyId);
    b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
    printf(" %.9g %.9g %.9g %.9g %.9g %.9g %.9g %d",
           transform.p.x,
           transform.p.y,
           transform.q.c,
           transform.q.s,
           velocity.x,
           velocity.y,
           b2Body_GetAngularVelocity(bodyId),
           b2Body_GetContactCapacity(bodyId));
}

static void print_axle(b2JointId jointId)
{
    b2Vec2 force = b2Joint_GetConstraintForce(jointId);
    printf(" %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g",
           b2WheelJoint_GetMotorSpeed(jointId),
           b2WheelJoint_GetMaxMotorTorque(jointId),
           b2WheelJoint_GetMotorTorque(jointId),
           b2WheelJoint_GetSpringHertz(jointId),
           b2WheelJoint_GetSpringDampingRatio(jointId),
           b2Joint_GetLinearSeparation(jointId),
           force.x,
           force.y,
           b2Joint_GetConstraintTorque(jointId));
}

static b2BodyId create_ground(b2WorldId worldId)
{
    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);

    b2Vec2 points[25];
    int count = 24;

    points[count--] = (b2Vec2){-20.0f, -20.0f};
    points[count--] = (b2Vec2){-20.0f, 0.0f};
    points[count--] = (b2Vec2){20.0f, 0.0f};

    float hs[10] = {0.25f, 1.0f, 4.0f, 0.0f, 0.0f, -1.0f, -2.0f, -2.0f, -1.25f, 0.0f};
    float x = 20.0f;
    float dx = 5.0f;
    for (int j = 0; j < 2; ++j)
    {
        for (int i = 0; i < 10; ++i)
        {
            points[count--] = (b2Vec2){x + dx, hs[i]};
            x += dx;
        }
    }

    points[count--] = (b2Vec2){x + 40.0f, 0.0f};
    points[count--] = (b2Vec2){x + 40.0f, -20.0f};

    b2ChainDef chainDef = b2DefaultChainDef();
    chainDef.points = points;
    chainDef.count = 25;
    chainDef.isLoop = true;
    b2CreateChain(groundId, &chainDef);

    x += 80.0f;
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Segment segment = {{x, 0.0f}, {x + 40.0f, 0.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &segment);
    x += 40.0f;
    segment = (b2Segment){{x, 0.0f}, {x + 10.0f, 5.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &segment);
    x += 20.0f;
    segment = (b2Segment){{x, 0.0f}, {x + 40.0f, 0.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &segment);
    x += 40.0f;
    segment = (b2Segment){{x, 0.0f}, {x, 20.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &segment);
    return groundId;
}

static b2BodyId create_teeter(b2WorldId worldId, b2BodyId groundId)
{
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.position = (b2Vec2){140.0f, 1.0f};
    bodyDef.angularVelocity = 1.0f;
    bodyDef.type = b2_dynamicBody;
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Polygon box = b2MakeBox(10.0f, 0.25f);
    b2CreatePolygonShape(bodyId, &shapeDef, &box);

    b2Vec2 pivot = bodyDef.position;
    b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
    jointDef.bodyIdA = groundId;
    jointDef.bodyIdB = bodyId;
    jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
    jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
    jointDef.lowerAngle = -8.0f * B2_PI / 180.0f;
    jointDef.upperAngle = 8.0f * B2_PI / 180.0f;
    jointDef.enableLimit = true;
    b2CreateRevoluteJoint(worldId, &jointDef);
    return bodyId;
}

static void create_bridge(b2WorldId worldId, b2BodyId groundId, b2BodyId bridge[BRIDGE_COUNT])
{
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Capsule capsule = {{-1.0f, 0.0f}, {1.0f, 0.0f}, 0.125f};
    b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
    b2BodyId prevBodyId = groundId;
    for (int i = 0; i < BRIDGE_COUNT; ++i)
    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = (b2Vec2){161.0f + 2.0f * i, -0.125f};
        b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
        b2CreateCapsuleShape(bodyId, &shapeDef, &capsule);

        b2Vec2 pivot = {160.0f + 2.0f * i, -0.125f};
        jointDef.bodyIdA = prevBodyId;
        jointDef.bodyIdB = bodyId;
        jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
        jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
        b2CreateRevoluteJoint(worldId, &jointDef);
        bridge[i] = bodyId;
        prevBodyId = bodyId;
    }

    b2Vec2 pivot = {160.0f + 2.0f * BRIDGE_COUNT, -0.125f};
    jointDef.bodyIdA = prevBodyId;
    jointDef.bodyIdB = groundId;
    jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
    jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
    jointDef.enableMotor = true;
    jointDef.maxMotorTorque = 50.0f;
    b2CreateRevoluteJoint(worldId, &jointDef);
}

static void create_boxes(b2WorldId worldId, b2BodyId boxes[BOX_COUNT])
{
    b2Polygon box = b2MakeBox(0.5f, 0.5f);
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.material.friction = 0.25f;
    shapeDef.material.restitution = 0.25f;
    shapeDef.density = 0.25f;
    for (int i = 0; i < BOX_COUNT; ++i)
    {
        bodyDef.position = (b2Vec2){230.0f, 0.5f + i};
        boxes[i] = b2CreateBody(worldId, &bodyDef);
        b2CreatePolygonShape(boxes[i], &shapeDef, &box);
    }
}

static CarState spawn_car(b2WorldId worldId, b2Vec2 position, float scale, float hertz, float dampingRatio, float torque)
{
    CarState car = {0};
    b2Vec2 vertices[6] = {
        {-1.5f, -0.5f}, {1.5f, -0.5f}, {1.5f, 0.0f}, {0.0f, 0.9f}, {-1.15f, 0.9f}, {-1.5f, 0.2f},
    };

    for (int i = 0; i < 6; ++i)
    {
        vertices[i].x *= 0.85f * scale;
        vertices[i].y *= 0.85f * scale;
    }

    b2Hull hull = b2ComputeHull(vertices, 6);
    b2Polygon chassis = b2MakePolygon(&hull, 0.15f * scale);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.0f / scale;
    shapeDef.material.friction = 0.2f;

    b2Circle circle = {{0.0f, 0.0f}, 0.4f * scale};
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = b2Add((b2Vec2){0.0f, 1.0f * scale}, position);
    car.chassisId = b2CreateBody(worldId, &bodyDef);
    b2CreatePolygonShape(car.chassisId, &shapeDef, &chassis);

    shapeDef.density = 2.0f / scale;
    shapeDef.material.friction = 1.5f;
    shapeDef.material.rollingResistance = 0.1f;

    bodyDef.position = b2Add((b2Vec2){-1.0f * scale, 0.35f * scale}, position);
    bodyDef.allowFastRotation = true;
    car.rearWheelId = b2CreateBody(worldId, &bodyDef);
    b2CreateCircleShape(car.rearWheelId, &shapeDef, &circle);

    bodyDef.position = b2Add((b2Vec2){1.0f * scale, 0.4f * scale}, position);
    bodyDef.allowFastRotation = true;
    car.frontWheelId = b2CreateBody(worldId, &bodyDef);
    b2CreateCircleShape(car.frontWheelId, &shapeDef, &circle);

    b2Vec2 axis = {0.0f, 1.0f};
    b2Vec2 pivot = b2Body_GetPosition(car.rearWheelId);
    b2WheelJointDef jointDef = b2DefaultWheelJointDef();
    jointDef.bodyIdA = car.chassisId;
    jointDef.bodyIdB = car.rearWheelId;
    jointDef.localAxisA = b2Body_GetLocalVector(jointDef.bodyIdA, axis);
    jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
    jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
    jointDef.motorSpeed = 0.0f;
    jointDef.maxMotorTorque = torque;
    jointDef.enableMotor = true;
    jointDef.hertz = hertz;
    jointDef.dampingRatio = dampingRatio;
    jointDef.lowerTranslation = -0.25f * scale;
    jointDef.upperTranslation = 0.25f * scale;
    jointDef.enableLimit = true;
    car.rearAxleId = b2CreateWheelJoint(worldId, &jointDef);

    pivot = b2Body_GetPosition(car.frontWheelId);
    jointDef.bodyIdA = car.chassisId;
    jointDef.bodyIdB = car.frontWheelId;
    jointDef.localAxisA = b2Body_GetLocalVector(jointDef.bodyIdA, axis);
    jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
    jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
    jointDef.motorSpeed = 0.0f;
    jointDef.maxMotorTorque = torque;
    jointDef.enableMotor = true;
    jointDef.hertz = hertz;
    jointDef.dampingRatio = dampingRatio;
    jointDef.lowerTranslation = -0.25f * scale;
    jointDef.upperTranslation = 0.25f * scale;
    jointDef.enableLimit = true;
    car.frontAxleId = b2CreateWheelJoint(worldId, &jointDef);
    return car;
}

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : DEFAULT_STEP_COUNT;
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyId groundId = create_ground(worldId);
    b2BodyId teeter = create_teeter(worldId, groundId);
    b2BodyId bridge[BRIDGE_COUNT];
    create_bridge(worldId, groundId, bridge);
    b2BodyId boxes[BOX_COUNT];
    create_boxes(worldId, boxes);
    CarState car = spawn_car(worldId, (b2Vec2){0.0f, 0.0f}, 1.0f, 5.0f, 0.7f, 5.0f);

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("driving %d %d %d %d %d %d %d %d %d",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           counters.jointCount,
           b2World_GetAwakeBodyCount(worldId),
           3,
           3,
           BOX_COUNT,
           2);
    print_body(teeter);
    print_body(car.chassisId);
    print_body(car.rearWheelId);
    print_body(car.frontWheelId);
    print_body(bridge[0]);
    print_body(bridge[BRIDGE_COUNT / 2]);
    print_body(bridge[BRIDGE_COUNT - 1]);
    for (int i = 0; i < BOX_COUNT; ++i)
    {
        print_body(boxes[i]);
    }
    print_axle(car.rearAxleId);
    print_axle(car.frontAxleId);
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
