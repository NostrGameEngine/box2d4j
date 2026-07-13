#include <box2d/box2d.h>

#include <stdio.h>

static int graph_count(b2WorldId worldId)
{
    b2Counters counters = b2World_GetCounters(worldId);
    int count = 0;
    for (int i = 0; i < 12; ++i)
    {
        count += counters.colorCounts[i];
    }
    return count;
}

static b2BodyId make_body(b2WorldId worldId)
{
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    return b2CreateBody(worldId, &bodyDef);
}

static b2JointId make_joint(b2WorldId worldId, b2BodyId bodyA, b2BodyId bodyB)
{
    b2DistanceJointDef jointDef = b2DefaultDistanceJointDef();
    jointDef.bodyIdA = bodyA;
    jointDef.bodyIdB = bodyB;
    jointDef.length = 1.0f;
    return b2CreateDistanceJoint(worldId, &jointDef);
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = (b2Vec2){0.0f, 0.0f};

    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyId bodyA = make_body(worldId);
    b2BodyId bodyB = make_body(worldId);
    b2Body_Disable(bodyA);
    b2JointId jointId = make_joint(worldId, bodyA, bodyB);
    printf("disabled %d %d %d\n", b2Joint_IsValid(jointId), graph_count(worldId),
           b2World_GetCounters(worldId).jointCount);
    b2Body_Enable(bodyA);
    printf("enabled %d %d %d\n", b2Body_IsAwake(bodyA), b2Body_IsAwake(bodyB), graph_count(worldId));
    b2DestroyWorld(worldId);

    worldId = b2CreateWorld(&worldDef);
    bodyA = make_body(worldId);
    bodyB = make_body(worldId);
    b2Body_SetAwake(bodyA, false);
    b2Body_SetAwake(bodyB, false);
    jointId = make_joint(worldId, bodyA, bodyB);
    printf("sleeping %d %d %d\n", b2Body_IsAwake(bodyA), b2Body_IsAwake(bodyB), graph_count(worldId));
    b2Body_SetAwake(bodyA, true);
    printf("woken %d %d %d\n", b2Body_IsAwake(bodyA), b2Body_IsAwake(bodyB), graph_count(worldId));
    b2DestroyWorld(worldId);

    worldId = b2CreateWorld(&worldDef);
    b2BodyDef staticDef = b2DefaultBodyDef();
    bodyA = b2CreateBody(worldId, &staticDef);
    bodyB = make_body(worldId);
    b2Body_Disable(bodyA);
    make_joint(worldId, bodyA, bodyB);
    b2Body_SetAwake(bodyB, false);
    b2Body_Enable(bodyA);
    printf("staticEnable %d %d %d\n", b2Body_IsAwake(bodyA), b2Body_IsAwake(bodyB), graph_count(worldId));
    b2DestroyWorld(worldId);

    worldId = b2CreateWorld(&worldDef);
    bodyA = make_body(worldId);
    bodyB = make_body(worldId);
    jointId = make_joint(worldId, bodyA, bodyB);
    b2Body_SetAwake(bodyA, false);
    b2Joint_WakeBodies(jointId);
    printf("jointWake %d %d %d\n", b2Body_IsAwake(bodyA), b2Body_IsAwake(bodyB), graph_count(worldId));
    b2DestroyWorld(worldId);
    return 0;
}
