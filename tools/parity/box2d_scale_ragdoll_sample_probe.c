// SPDX-License-Identifier: MIT

#include "box2d/box2d.h"
#include "human.h"
#include "random.h"

#include <stdio.h>
#include <stdlib.h>

enum
{
    DEFAULT_STEP_COUNT = 120
};

static void print_body(b2BodyId bodyId)
{
    b2Transform transform = b2Body_GetTransform(bodyId);
    b2Vec2 velocity = b2Body_GetLinearVelocity(bodyId);
    b2MassData massData = b2Body_GetMassData(bodyId);
    printf(" %.9g %.9g %.9g %.9g %.9g %.9g %.9g %d %d %.9g %.9g",
           transform.p.x,
           transform.p.y,
           transform.q.c,
           transform.q.s,
           velocity.x,
           velocity.y,
           b2Body_GetAngularVelocity(bodyId),
           b2Body_GetShapeCount(bodyId),
           b2Body_GetContactCapacity(bodyId),
           massData.mass,
           massData.rotationalInertia);
}

static void print_joint(b2JointId jointId)
{
    b2Vec2 force = b2Joint_GetConstraintForce(jointId);
    printf(" %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g %.9g",
           b2RevoluteJoint_GetAngle(jointId),
           b2RevoluteJoint_GetMotorTorque(jointId),
           b2RevoluteJoint_GetMaxMotorTorque(jointId),
           b2RevoluteJoint_GetSpringHertz(jointId),
           b2RevoluteJoint_GetSpringDampingRatio(jointId),
           b2Joint_GetLinearSeparation(jointId),
           b2Joint_GetAngularSeparation(jointId),
           force.x,
           force.y,
           b2Joint_GetConstraintTorque(jointId));
}

int main(int argc, char** argv)
{
    int stepCount = argc > 1 ? atoi(argv[1]) : DEFAULT_STEP_COUNT;
    float scale = argc > 2 ? strtof(argv[2], NULL) : 1.75f;
    g_randomSeed = RAND_SEED;

    b2WorldDef worldDef = b2DefaultWorldDef();
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    b2Polygon box = b2MakeOffsetBox(20.0f, 1.0f, (b2Vec2){0.0f, -1.0f}, b2Rot_identity);
    b2CreatePolygonShape(groundId, &shapeDef, &box);

    Human human = {0};
    CreateHuman(&human, worldId, (b2Vec2){0.0f, 5.0f}, 1.0f, 0.03f, 1.0f, 0.5f, 1, NULL, false);
    Human_ApplyRandomAngularImpulse(&human, 10.0f);
    Human_SetScale(&human, scale);

    for (int step = 0; step < stepCount; ++step)
    {
        b2World_Step(worldId, 1.0f / 60.0f, 4);
    }

    b2Counters counters = b2World_GetCounters(worldId);
    printf("scaleRagdoll %d %d %d %d %d %.9g %d %d",
           counters.bodyCount,
           counters.shapeCount,
           counters.contactCount,
           counters.jointCount,
           b2World_GetAwakeBodyCount(worldId),
           scale,
           bone_count,
           bone_count - 1);
    for (int i = 0; i < bone_count; ++i)
    {
        print_body(human.bones[i].bodyId);
    }
    for (int i = 1; i < bone_count; ++i)
    {
        print_joint(human.bones[i].jointId);
    }
    printf("\n");

    b2DestroyWorld(worldId);
    return 0;
}
