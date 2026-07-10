// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "box2d/math_functions.h"

#include <stdio.h>
#include <stdlib.h>

enum
{
    DEFAULT_STEP_COUNT = 0
};

typedef struct Scene
{
    b2BodyType type;
    bool enabled;
    b2BodyId platformId;
    b2BodyId bodies[7];
} Scene;

static void set_type(Scene* scene, b2BodyType type)
{
    scene->type = type;
    b2Body_SetType(scene->bodies[2], type);
    if (type == b2_kinematicBody)
    {
        b2Body_SetLinearVelocity(scene->bodies[2], (b2Vec2){-3.0f, 0.0f});
        b2Body_SetAngularVelocity(scene->bodies[2], 0.0f);
    }
    b2Body_SetType(scene->bodies[1], type);
    b2Body_SetType(scene->bodies[4], type);
    b2Body_SetType(scene->bodies[5], type);
    b2Body_SetType(scene->bodies[6], type);
}

static void set_enabled(Scene* scene, bool enabled)
{
    scene->enabled = enabled;
    int indices[5] = {2, 1, 4, 5, 6};
    if (enabled)
    {
        for (int i = 0; i < 5; ++i)
        {
            b2Body_Enable(scene->bodies[indices[i]]);
        }
        if (scene->type == b2_kinematicBody)
        {
            b2Body_SetLinearVelocity(scene->platformId, (b2Vec2){-3.0f, 0.0f});
            b2Body_SetAngularVelocity(scene->platformId, 0.0f);
        }
    }
    else
    {
        for (int i = 0; i < 5; ++i)
        {
            b2Body_Disable(scene->bodies[indices[i]]);
        }
    }
}

static void step_scene(Scene* scene)
{
    if (scene->type == b2_kinematicBody)
    {
        b2Vec2 p = b2Body_GetPosition(scene->platformId);
        b2Vec2 v = b2Body_GetLinearVelocity(scene->platformId);

        if ((p.x < -14.0f && v.x < 0.0f) || (p.x > 6.0f && v.x > 0.0f))
        {
            v.x = -v.x;
            b2Body_SetLinearVelocity(scene->platformId, v);
        }
    }
}

static void create_scene(b2WorldId worldId, Scene* scene)
{
    scene->type = b2_dynamicBody;
    scene->enabled = true;

    b2BodyId groundId;
    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        groundId = b2CreateBody(worldId, &bodyDef);

        b2Segment segment = {{-20.0f, 0.0f}, {20.0f, 0.0f}};
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        b2CreateSegmentShape(groundId, &shapeDef, &segment);
    }

    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = (b2Vec2){-2.0f, 3.0f};
        scene->bodies[0] = b2CreateBody(worldId, &bodyDef);

        b2Polygon box = b2MakeBox(0.5f, 2.0f);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        b2CreatePolygonShape(scene->bodies[0], &shapeDef, &box);
    }

    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = scene->type;
        bodyDef.isEnabled = scene->enabled;
        bodyDef.position = (b2Vec2){3.0f, 3.0f};
        scene->bodies[1] = b2CreateBody(worldId, &bodyDef);

        b2Polygon box = b2MakeBox(0.5f, 2.0f);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        b2CreatePolygonShape(scene->bodies[1], &shapeDef, &box);
    }

    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = scene->type;
        bodyDef.isEnabled = scene->enabled;
        bodyDef.position = (b2Vec2){-4.0f, 5.0f};
        scene->bodies[2] = b2CreateBody(worldId, &bodyDef);
        scene->platformId = scene->bodies[2];

        b2Polygon box = b2MakeOffsetBox(0.5f, 4.0f, (b2Vec2){4.0f, 0.0f}, b2MakeRot(0.5f * B2_PI));
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 2.0f;
        b2CreatePolygonShape(scene->bodies[2], &shapeDef, &box);

        b2RevoluteJointDef revoluteDef = b2DefaultRevoluteJointDef();
        b2Vec2 pivot = {-2.0f, 5.0f};
        revoluteDef.bodyIdA = scene->bodies[0];
        revoluteDef.bodyIdB = scene->bodies[2];
        revoluteDef.localAnchorA = b2Body_GetLocalPoint(scene->bodies[0], pivot);
        revoluteDef.localAnchorB = b2Body_GetLocalPoint(scene->bodies[2], pivot);
        revoluteDef.maxMotorTorque = 50.0f;
        revoluteDef.enableMotor = true;
        b2CreateRevoluteJoint(worldId, &revoluteDef);

        pivot = (b2Vec2){3.0f, 5.0f};
        revoluteDef.bodyIdA = scene->bodies[1];
        revoluteDef.bodyIdB = scene->bodies[2];
        revoluteDef.localAnchorA = b2Body_GetLocalPoint(scene->bodies[1], pivot);
        revoluteDef.localAnchorB = b2Body_GetLocalPoint(scene->bodies[2], pivot);
        revoluteDef.maxMotorTorque = 50.0f;
        revoluteDef.enableMotor = true;
        b2CreateRevoluteJoint(worldId, &revoluteDef);

        b2PrismaticJointDef prismaticDef = b2DefaultPrismaticJointDef();
        b2Vec2 anchor = {0.0f, 5.0f};
        prismaticDef.bodyIdA = groundId;
        prismaticDef.bodyIdB = scene->bodies[2];
        prismaticDef.localAnchorA = b2Body_GetLocalPoint(groundId, anchor);
        prismaticDef.localAnchorB = b2Body_GetLocalPoint(scene->bodies[2], anchor);
        prismaticDef.localAxisA = (b2Vec2){1.0f, 0.0f};
        prismaticDef.maxMotorForce = 1000.0f;
        prismaticDef.motorSpeed = 0.0f;
        prismaticDef.enableMotor = true;
        prismaticDef.lowerTranslation = -10.0f;
        prismaticDef.upperTranslation = 10.0f;
        prismaticDef.enableLimit = true;
        b2CreatePrismaticJoint(worldId, &prismaticDef);
    }

    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = (b2Vec2){-3.0f, 8.0f};
        scene->bodies[3] = b2CreateBody(worldId, &bodyDef);

        b2Polygon box = b2MakeBox(0.75f, 0.75f);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 2.0f;
        b2CreatePolygonShape(scene->bodies[3], &shapeDef, &box);
    }

    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = scene->type;
        bodyDef.isEnabled = scene->enabled;
        bodyDef.position = (b2Vec2){2.0f, 8.0f};
        scene->bodies[4] = b2CreateBody(worldId, &bodyDef);

        b2Polygon box = b2MakeBox(0.75f, 0.75f);
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 2.0f;
        b2CreatePolygonShape(scene->bodies[4], &shapeDef, &box);
    }

    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = scene->type;
        bodyDef.isEnabled = scene->enabled;
        bodyDef.position = (b2Vec2){8.0f, 0.2f};
        scene->bodies[5] = b2CreateBody(worldId, &bodyDef);

        b2Capsule capsule = {{0.0f, 0.0f}, {1.0f, 0.0f}, 0.25f};
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 2.0f;
        b2CreateCapsuleShape(scene->bodies[5], &shapeDef, &capsule);
    }

    {
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = scene->type;
        bodyDef.isEnabled = scene->enabled;
        bodyDef.position = (b2Vec2){-8.0f, 12.0f};
        bodyDef.gravityScale = 0.0f;
        scene->bodies[6] = b2CreateBody(worldId, &bodyDef);

        b2Circle circle = {{0.0f, 0.5f}, 0.25f};
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 2.0f;
        b2CreateCircleShape(scene->bodies[6], &shapeDef, &circle);
    }
}

static void print_body(b2BodyId bodyId)
{
    b2Vec2 p = b2Body_GetPosition(bodyId);
    b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
    printf(" %d %d %d %d %.9g %.9g %.9g %.9g %.9g %.9g",
           b2Body_GetType(bodyId),
           b2Body_IsEnabled(bodyId) ? 1 : 0,
           b2Body_IsAwake(bodyId) ? 1 : 0,
           b2Body_GetContactCapacity(bodyId),
           p.x,
           p.y,
           b2Rot_GetAngle(b2Body_GetRotation(bodyId)),
           v.x,
           v.y,
           b2Body_GetAngularVelocity(bodyId));
}

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : DEFAULT_STEP_COUNT;
    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);
    Scene scene;
    create_scene(worldId, &scene);
    set_type(&scene, b2_kinematicBody);
    set_enabled(&scene, false);
    set_enabled(&scene, true);
    set_type(&scene, b2_staticBody);
    set_type(&scene, b2_dynamicBody);

    for (int step = 0; step < stepCount; ++step)
    {
        step_scene(&scene);
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("bodyType %d %d %d %d %d %d %d 7",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           counters.jointCount,
           b2World_GetAwakeBodyCount(worldId),
           scene.type,
           scene.enabled ? 1 : 0);
    for (int i = 0; i < 7; ++i)
    {
        print_body(scene.bodies[i]);
    }
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
