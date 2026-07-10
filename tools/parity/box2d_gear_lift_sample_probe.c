// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"
#include "random.h"

#include <assert.h>
#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

enum
{
    DEFAULT_STEP_COUNT = 120,
    BODY_COUNT = 243,
    JOINT_COUNT = 44
};

typedef struct Scene
{
    b2BodyId bodies[BODY_COUNT];
    b2JointId joints[JOINT_COUNT];
    int bodyCount;
    int jointCount;
} Scene;

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
    else if (type == b2_prismaticJoint)
    {
        metricA = b2PrismaticJoint_GetTranslation(jointId);
        metricB = b2PrismaticJoint_GetMotorForce(jointId);
        metricC = b2PrismaticJoint_GetMaxMotorForce(jointId);
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

static int is_path_command(char command)
{
    return command == 'M' || command == 'L' || command == 'H' || command == 'V' || command == 'm' || command == 'l' ||
           command == 'h' || command == 'v';
}

static int parse_path(const char* svgPath, b2Vec2 offset, b2Vec2* points, int capacity, float scale)
{
    int pointCount = 0;
    b2Vec2 currentPoint = {0};
    const char* ptr = svgPath;
    char command = *ptr;

    while (*ptr != '\0')
    {
        if (isdigit((unsigned char)*ptr) == 0 && *ptr != '-')
        {
            command = *ptr;
            if (command == 'z')
            {
                break;
            }

            if (is_path_command(command))
            {
                ptr += 1;
                while (isspace((unsigned char)*ptr))
                {
                    ptr += 1;
                }
            }
        }

        assert(isdigit((unsigned char)*ptr) != 0 || *ptr == '-');

        float x = 0.0f;
        float y = 0.0f;
        switch (command)
        {
            case 'M':
            case 'L':
                assert(sscanf(ptr, "%f,%f", &x, &y) == 2);
                currentPoint.x = x;
                currentPoint.y = y;
                break;
            case 'H':
                assert(sscanf(ptr, "%f", &x) == 1);
                currentPoint.x = x;
                break;
            case 'V':
                assert(sscanf(ptr, "%f", &y) == 1);
                currentPoint.y = y;
                break;
            case 'm':
            case 'l':
                assert(sscanf(ptr, "%f,%f", &x, &y) == 2);
                currentPoint.x += x;
                currentPoint.y += y;
                break;
            case 'h':
                assert(sscanf(ptr, "%f", &x) == 1);
                currentPoint.x += x;
                break;
            case 'v':
                assert(sscanf(ptr, "%f", &y) == 1);
                currentPoint.y += y;
                break;
            default:
                assert(false);
                break;
        }

        points[pointCount] = (b2Vec2){scale * (currentPoint.x + offset.x), -scale * (currentPoint.y + offset.y)};
        pointCount += 1;
        if (pointCount == capacity)
        {
            break;
        }

        while (*ptr != '\0' && isspace((unsigned char)*ptr) == 0)
        {
            ptr += 1;
        }

        while (isspace((unsigned char)*ptr))
        {
            ptr += 1;
        }
    }

    return pointCount;
}

static b2BodyId create_gear(b2WorldId worldId, Scene* scene, b2Vec2 position, float gearRadius, float toothHalfWidth,
                            float toothHalfHeight, float toothRadius, bool driver)
{
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = position;
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    add_body(scene, bodyId);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.material.friction = 0.1f;
    shapeDef.material.customColor = b2_colorSaddleBrown;
    b2Circle circle = {b2Vec2_zero, gearRadius};
    b2CreateCircleShape(bodyId, &shapeDef, &circle);

    int count = 16;
    float deltaAngle = 2.0f * B2_PI / 16.0f;
    b2Rot dq = b2MakeRot(deltaAngle);
    b2Vec2 center = {gearRadius + (driver ? toothHalfHeight : toothHalfWidth), 0.0f};
    b2Rot rotation = b2Rot_identity;

    for (int i = 0; i < count; ++i)
    {
        b2Polygon tooth = b2MakeOffsetRoundedBox(toothHalfWidth, toothHalfHeight, center, rotation, toothRadius);
        shapeDef.material.customColor = b2_colorGray;
        b2CreatePolygonShape(bodyId, &shapeDef, &tooth);

        rotation = b2MulRot(dq, rotation);
        center = b2RotateVector(rotation, (b2Vec2){gearRadius + (driver ? toothHalfHeight : toothHalfWidth), 0.0f});
    }

    return bodyId;
}

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : DEFAULT_STEP_COUNT;
    g_randomSeed = RAND_SEED;

    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    Scene scene = {0};

    b2BodyDef groundDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &groundDef);

    const char* path =
        "m 63.500002,201.08333 103.187498,0 1e-5,-37.04166 h -2.64584 l 0,34.39583 h -42.33333 v -2.64583 l "
        "-2.64584,-1e-5 v -2.64583 h -2.64583 v -2.64584 h -2.64584 v -2.64583 H 111.125 v -2.64583 h -2.64583 v "
        "-2.64583 h -2.64583 v -2.64584 l -2.64584,1e-5 v -2.64583 l -2.64583,-1e-5 V 174.625 h -2.645834 v -2.64584 l "
        "-2.645833,1e-5 v -2.64584 H 92.60417 v -2.64583 h -2.645834 v -2.64583 l -26.458334,0 0,37.04166";

    b2Vec2 points[128];
    int pointCount = parse_path(path, (b2Vec2){-120.0f, -200.0f}, points, 64, 0.2f);
    b2SurfaceMaterial material = b2DefaultSurfaceMaterial();
    material.customColor = b2_colorDarkSeaGreen;
    b2ChainDef chainDef = b2DefaultChainDef();
    chainDef.points = points;
    chainDef.count = pointCount;
    chainDef.isLoop = true;
    chainDef.materials = &material;
    chainDef.materialCount = 1;
    b2CreateChain(groundId, &chainDef);

    float gearRadius = 1.0f;
    float toothHalfWidth = 0.09f;
    float toothHalfHeight = 0.06f;
    float toothRadius = 0.03f;
    float linkHalfLength = 0.07f;
    float linkRadius = 0.05f;
    int linkCount = 40;
    float doorHalfHeight = 1.5f;

    b2Vec2 gearPosition1 = {-4.25f, 9.75f};
    b2Vec2 gearPosition2 = b2Add(gearPosition1, (b2Vec2){2.0f, 1.0f});
    b2Vec2 linkAttachPosition =
        b2Add(gearPosition2, (b2Vec2){gearRadius + 2.0f * toothHalfWidth + toothRadius, 0.0f});
    b2Vec2 doorPosition =
        b2Sub(linkAttachPosition, (b2Vec2){0.0f, 2.0f * linkCount * linkHalfLength + doorHalfHeight});

    b2BodyId driverGearId =
        create_gear(worldId, &scene, gearPosition1, gearRadius, toothHalfWidth, toothHalfHeight, toothRadius, true);

    b2RevoluteJointDef revoluteDef = b2DefaultRevoluteJointDef();
    revoluteDef.bodyIdA = groundId;
    revoluteDef.bodyIdB = driverGearId;
    revoluteDef.localAnchorA = b2Body_GetLocalPoint(groundId, gearPosition1);
    revoluteDef.localAnchorB = b2Vec2_zero;
    revoluteDef.enableMotor = true;
    revoluteDef.maxMotorTorque = 80.0f;
    revoluteDef.motorSpeed = 0.0f;
    add_joint(&scene, b2CreateRevoluteJoint(worldId, &revoluteDef));

    b2BodyId followerId =
        create_gear(worldId, &scene, gearPosition2, gearRadius, toothHalfWidth, toothHalfHeight, toothRadius, false);

    revoluteDef = b2DefaultRevoluteJointDef();
    revoluteDef.bodyIdA = groundId;
    revoluteDef.bodyIdB = followerId;
    revoluteDef.localAnchorA = b2Body_GetLocalPoint(groundId, gearPosition2);
    revoluteDef.localAnchorB = b2Vec2_zero;
    revoluteDef.enableMotor = true;
    revoluteDef.maxMotorTorque = 0.5f;
    revoluteDef.referenceAngle = 0.25f * B2_PI;
    revoluteDef.lowerAngle = -0.3f * B2_PI;
    revoluteDef.upperAngle = 0.8f * B2_PI;
    revoluteDef.enableLimit = true;
    add_joint(&scene, b2CreateRevoluteJoint(worldId, &revoluteDef));

    b2BodyId lastLinkId = b2_nullBodyId;
    {
        b2Capsule capsule = {{0.0f, -linkHalfLength}, {0.0f, linkHalfLength}, linkRadius};
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 2.0f;
        shapeDef.material.customColor = b2_colorLightSteelBlue;

        b2RevoluteJointDef jointDef = b2DefaultRevoluteJointDef();
        jointDef.maxMotorTorque = 0.05f;
        jointDef.enableMotor = true;

        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2Vec2 position = b2Add(linkAttachPosition, (b2Vec2){0.0f, -linkHalfLength});

        b2BodyId prevBodyId = followerId;
        for (int i = 0; i < linkCount; ++i)
        {
            bodyDef.position = position;

            b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
            b2CreateCapsuleShape(bodyId, &shapeDef, &capsule);
            add_body(&scene, bodyId);

            b2Vec2 pivot = {position.x, position.y + linkHalfLength};
            jointDef.bodyIdA = prevBodyId;
            jointDef.bodyIdB = bodyId;
            jointDef.localAnchorA = b2Body_GetLocalPoint(jointDef.bodyIdA, pivot);
            jointDef.localAnchorB = b2Body_GetLocalPoint(jointDef.bodyIdB, pivot);
            add_joint(&scene, b2CreateRevoluteJoint(worldId, &jointDef));

            position.y -= 2.0f * linkHalfLength;
            prevBodyId = bodyId;
        }

        lastLinkId = prevBodyId;
    }

    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = doorPosition;
        b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
        add_body(&scene, bodyId);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.material.friction = 0.1f;
        shapeDef.material.customColor = b2_colorDarkCyan;
        b2Polygon box = b2MakeBox(0.15f, doorHalfHeight);
        b2CreatePolygonShape(bodyId, &shapeDef, &box);

        b2Vec2 pivot = b2Add(doorPosition, (b2Vec2){0.0f, doorHalfHeight});
        revoluteDef = b2DefaultRevoluteJointDef();
        revoluteDef.bodyIdA = lastLinkId;
        revoluteDef.bodyIdB = bodyId;
        revoluteDef.localAnchorA = b2Body_GetLocalPoint(lastLinkId, pivot);
        revoluteDef.localAnchorB = (b2Vec2){0.0f, doorHalfHeight};
        revoluteDef.enableMotor = true;
        revoluteDef.maxMotorTorque = 0.05f;
        add_joint(&scene, b2CreateRevoluteJoint(worldId, &revoluteDef));

        b2PrismaticJointDef jointDef = b2DefaultPrismaticJointDef();
        jointDef.bodyIdA = groundId;
        jointDef.bodyIdB = bodyId;
        jointDef.localAnchorA = b2Body_GetLocalPoint(groundId, doorPosition);
        jointDef.localAnchorB = b2Vec2_zero;
        jointDef.localAxisA = (b2Vec2){0.0f, 1.0f};
        jointDef.maxMotorForce = 0.2f;
        jointDef.enableMotor = true;
        jointDef.collideConnected = true;
        add_joint(&scene, b2CreatePrismaticJoint(worldId, &jointDef));
    }

    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.material.rollingResistance = 0.3f;

        b2HexColor colors[5] = {
            b2_colorGray, b2_colorGainsboro, b2_colorLightGray, b2_colorLightSlateGray, b2_colorDarkGray,
        };

        float y = 4.25f;
        int xCount = 10;
        int yCount = 20;
        for (int i = 0; i < yCount; ++i)
        {
            float x = -3.15f;
            for (int j = 0; j < xCount; ++j)
            {
                bodyDef.position = (b2Vec2){x, y};
                b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
                b2Polygon poly = RandomPolygon(0.1f);
                poly.radius = RandomFloatRange(0.01f, 0.02f);
                int colorIndex = RandomIntRange(0, 4);
                shapeDef.material.customColor = colors[colorIndex];
                b2CreatePolygonShape(bodyId, &shapeDef, &poly);
                add_body(&scene, bodyId);
                x += 0.2f;
            }

            y += 0.2f;
        }
    }

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("gearLift %d %d %d %d %d %d %d",
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
