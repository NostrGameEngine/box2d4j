#include <box2d/box2d.h>

#include <stdio.h>

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = (b2Vec2){0.0f, 0.0f};
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    b2BodyId bodyA = b2CreateBody(worldId, &bodyDef);
    b2BodyId bodyB = b2CreateBody(worldId, &bodyDef);

    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.0f;
    b2Circle circle = {{0.0f, 0.0f}, 1.0f};
    b2CreateCircleShape(bodyA, &shapeDef, &circle);
    b2CreateCircleShape(bodyB, &shapeDef, &circle);

    b2FilterJointDef jointDef = b2DefaultFilterJointDef();
    jointDef.bodyIdA = bodyA;
    jointDef.bodyIdB = bodyB;
    b2JointId jointId = b2CreateFilterJoint(worldId, &jointDef);

    b2World_Step(worldId, 1.0f / 60.0f, 4);
    b2Body_SetAwake(bodyA, false);
    printf("before %d %d %d\n", b2Body_IsAwake(bodyA), b2Body_IsAwake(bodyB),
           b2World_GetCounters(worldId).contactCount);

    b2DestroyJoint(jointId);
    printf("destroy %d %d %d %d\n", b2Joint_IsValid(jointId), b2Body_IsAwake(bodyA),
           b2Body_IsAwake(bodyB), b2World_GetCounters(worldId).contactCount);

    b2Body_SetTransform(bodyA, (b2Vec2){0.25f, 0.0f}, b2Rot_identity);
    b2World_Step(worldId, 1.0f / 60.0f, 4);
    printf("step %d %d %d\n", b2Body_IsAwake(bodyA), b2Body_IsAwake(bodyB),
           b2World_GetCounters(worldId).contactCount);

    b2DestroyWorld(worldId);

    worldId = b2CreateWorld(&worldDef);
    bodyA = b2CreateBody(worldId, &bodyDef);
    bodyB = b2CreateBody(worldId, &bodyDef);
    jointDef = b2DefaultFilterJointDef();
    jointDef.bodyIdA = bodyA;
    jointDef.bodyIdB = bodyB;
    jointId = b2CreateFilterJoint(worldId, &jointDef);
    b2Body_SetAwake(bodyA, false);
    b2DestroyBody(bodyA);
    printf("bodyDestroy %d %d %d\n", b2Body_IsValid(bodyA), b2Joint_IsValid(jointId), b2Body_IsAwake(bodyB));
    b2DestroyWorld(worldId);
    return 0;
}
