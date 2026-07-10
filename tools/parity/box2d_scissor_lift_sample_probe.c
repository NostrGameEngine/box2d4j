// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>
#include <stdlib.h>

enum
{
    DEFAULT_STEP_COUNT = 120,
    LINK_PAIR_COUNT = 3,
    BODY_COUNT = 10,
    JOINT_COUNT = 14
};

typedef struct Scene
{
    b2BodyId bodies[BODY_COUNT];
    b2JointId joints[JOINT_COUNT];
    int bodyCount;
    int jointCount;
} Scene;

typedef struct CarState
{
    b2BodyId chassisId;
    b2BodyId rearWheelId;
    b2BodyId frontWheelId;
    b2JointId rearAxleId;
    b2JointId frontAxleId;
} CarState;

static void add_body(Scene* scene, b2BodyId bodyId)
{
    scene->bodies[scene->bodyCount++] = bodyId;
}

static void add_joint(Scene* scene, b2JointId jointId)
{
    scene->joints[scene->jointCount++] = jointId;
}

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

static void print_joint(b2JointId jointId)
{
    int type = (int)b2Joint_GetType(jointId);
    float metricA = 0.0f;
    float metricB = 0.0f;
    float metricC = 0.0f;
    if (type == b2_revoluteJoint)
    {
        metricA = b2RevoluteJoint_GetAngle(jointId);
        metricB = b2RevoluteJoint_GetMotorTorque(jointId);
        metricC = b2RevoluteJoint_GetMaxMotorTorque(jointId);
    }
    else if (type == b2_wheelJoint)
    {
        metricA = b2WheelJoint_GetMotorTorque(jointId);
        metricB = b2WheelJoint_GetMaxMotorTorque(jointId);
        metricC = b2WheelJoint_GetSpringHertz(jointId);
    }
    else if (type == b2_distanceJoint)
    {
        metricA = b2DistanceJoint_GetCurrentLength(jointId);
        metricB = b2DistanceJoint_GetMotorForce(jointId);
        metricC = b2DistanceJoint_GetMaxMotorForce(jointId);
    }
    b2Vec2 force = b2Joint_GetConstraintForce(jointId);
    printf(" %d %.9g %.9g %.9g %.9g %.9g %.9g %.9g",
           type,
           b2Joint_GetLinearSeparation(jointId),
           metricA,
           metricB,
           metricC,
           force.x,
           force.y,
           b2Joint_GetConstraintTorque(jointId));
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
    Scene scene = {0};

    b2BodyDef groundDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &groundDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Segment segment = {{-20.0f, 0.0f}, {20.0f, 0.0f}};
    b2CreateSegmentShape(groundId, &shapeDef, &segment);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.sleepThreshold = 0.01f;
    b2Capsule capsule = {{-2.5f, 0.0f}, {2.5f, 0.0f}, 0.15f};

    b2BodyId baseId1 = groundId;
    b2BodyId baseId2 = groundId;
    b2Vec2 baseAnchor1 = {-2.5f, 0.2f};
    b2Vec2 baseAnchor2 = {2.5f, 0.2f};
    float y = 0.5f;
    b2BodyId linkId1 = b2_nullBodyId;

    for (int i = 0; i < LINK_PAIR_COUNT; ++i)
    {
        bodyDef.position = (b2Vec2){0.0f, y};
        bodyDef.rotation = b2MakeRot(0.15f);
        b2BodyId bodyId1 = b2CreateBody(worldId, &bodyDef);
        b2CreateCapsuleShape(bodyId1, &shapeDef, &capsule);
        add_body(&scene, bodyId1);

        bodyDef.position = (b2Vec2){0.0f, y};
        bodyDef.rotation = b2MakeRot(-0.15f);
        b2BodyId bodyId2 = b2CreateBody(worldId, &bodyDef);
        b2CreateCapsuleShape(bodyId2, &shapeDef, &capsule);
        add_body(&scene, bodyId2);

        if (i == 1)
        {
            linkId1 = bodyId2;
        }

        b2RevoluteJointDef revoluteDef = b2DefaultRevoluteJointDef();
        revoluteDef.bodyIdA = baseId1;
        revoluteDef.bodyIdB = bodyId1;
        revoluteDef.localAnchorA = baseAnchor1;
        revoluteDef.localAnchorB = (b2Vec2){-2.5f, 0.0f};
        revoluteDef.collideConnected = i == 0;
        add_joint(&scene, b2CreateRevoluteJoint(worldId, &revoluteDef));

        if (i == 0)
        {
            b2WheelJointDef wheelDef = b2DefaultWheelJointDef();
            wheelDef.bodyIdA = baseId2;
            wheelDef.bodyIdB = bodyId2;
            wheelDef.localAxisA = (b2Vec2){1.0f, 0.0f};
            wheelDef.localAnchorA = baseAnchor2;
            wheelDef.localAnchorB = (b2Vec2){2.5f, 0.0f};
            wheelDef.enableSpring = false;
            wheelDef.collideConnected = true;
            add_joint(&scene, b2CreateWheelJoint(worldId, &wheelDef));
        }
        else
        {
            revoluteDef.bodyIdA = baseId2;
            revoluteDef.bodyIdB = bodyId2;
            revoluteDef.localAnchorA = baseAnchor2;
            revoluteDef.localAnchorB = (b2Vec2){2.5f, 0.0f};
            revoluteDef.collideConnected = false;
            add_joint(&scene, b2CreateRevoluteJoint(worldId, &revoluteDef));
        }

        revoluteDef.bodyIdA = bodyId1;
        revoluteDef.bodyIdB = bodyId2;
        revoluteDef.localAnchorA = (b2Vec2){0.0f, 0.0f};
        revoluteDef.localAnchorB = (b2Vec2){0.0f, 0.0f};
        revoluteDef.collideConnected = false;
        add_joint(&scene, b2CreateRevoluteJoint(worldId, &revoluteDef));

        baseId1 = bodyId2;
        baseId2 = bodyId1;
        baseAnchor1 = (b2Vec2){-2.5f, 0.0f};
        baseAnchor2 = (b2Vec2){2.5f, 0.0f};
        y += 1.0f;
    }

    bodyDef.position = (b2Vec2){0.0f, y};
    bodyDef.rotation = b2Rot_identity;
    b2BodyId platformId = b2CreateBody(worldId, &bodyDef);
    b2Polygon box = b2MakeBox(3.0f, 0.2f);
    b2CreatePolygonShape(platformId, &shapeDef, &box);
    add_body(&scene, platformId);

    b2RevoluteJointDef revoluteDef = b2DefaultRevoluteJointDef();
    revoluteDef.bodyIdA = platformId;
    revoluteDef.bodyIdB = baseId1;
    revoluteDef.localAnchorA = (b2Vec2){-2.5f, -0.4f};
    revoluteDef.localAnchorB = baseAnchor1;
    revoluteDef.collideConnected = true;
    add_joint(&scene, b2CreateRevoluteJoint(worldId, &revoluteDef));

    b2WheelJointDef wheelDef = b2DefaultWheelJointDef();
    wheelDef.bodyIdA = platformId;
    wheelDef.bodyIdB = baseId2;
    wheelDef.localAxisA = (b2Vec2){1.0f, 0.0f};
    wheelDef.localAnchorA = (b2Vec2){2.5f, -0.4f};
    wheelDef.localAnchorB = baseAnchor2;
    wheelDef.enableSpring = false;
    wheelDef.collideConnected = true;
    add_joint(&scene, b2CreateWheelJoint(worldId, &wheelDef));

    b2DistanceJointDef distanceDef = b2DefaultDistanceJointDef();
    distanceDef.bodyIdA = groundId;
    distanceDef.bodyIdB = linkId1;
    distanceDef.localAnchorA = (b2Vec2){-2.5f, 0.2f};
    distanceDef.localAnchorB = (b2Vec2){0.5f, 0.0f};
    distanceDef.enableSpring = true;
    distanceDef.minLength = 0.2f;
    distanceDef.maxLength = 5.5f;
    distanceDef.enableLimit = true;
    distanceDef.enableMotor = false;
    distanceDef.motorSpeed = 0.25f;
    distanceDef.maxMotorForce = 2000.0f;
    add_joint(&scene, b2CreateDistanceJoint(worldId, &distanceDef));

    CarState car = spawn_car(worldId, (b2Vec2){0.0f, y + 2.0f}, 1.0f, 3.0f, 0.7f, 0.0f);
    add_body(&scene, car.chassisId);
    add_body(&scene, car.rearWheelId);
    add_body(&scene, car.frontWheelId);
    add_joint(&scene, car.rearAxleId);
    add_joint(&scene, car.frontAxleId);

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 8);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("scissorLift %d %d %d %d %d %d %d",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           counters.jointCount,
           b2World_GetAwakeBodyCount(worldId),
           scene.bodyCount,
           scene.jointCount);
    for (int i = 0; i < scene.bodyCount; ++i)
    {
        print_body(scene.bodies[i]);
    }
    for (int i = 0; i < scene.jointCount; ++i)
    {
        print_joint(scene.joints[i]);
    }
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
