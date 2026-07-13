#include <box2d/box2d.h>

#include <stdio.h>

static b2BodyId make_body(b2WorldId worldId, b2Vec2 position, bool withShape)
{
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = position;
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    if (withShape)
    {
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        b2Circle circle = {{0.0f, 0.0f}, 1.0f};
        b2CreateCircleShape(bodyId, &shapeDef, &circle);
    }
    return bodyId;
}

static b2JointId make_filter_joint(b2WorldId worldId, b2BodyId bodyA, b2BodyId bodyB)
{
    b2FilterJointDef jointDef = b2DefaultFilterJointDef();
    jointDef.bodyIdA = bodyA;
    jointDef.bodyIdB = bodyB;
    return b2CreateFilterJoint(worldId, &jointDef);
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = (b2Vec2){0.0f, 0.0f};

    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyId bodyA = make_body(worldId, (b2Vec2){0.0f, 0.0f}, false);
    b2BodyId bodyB = make_body(worldId, (b2Vec2){0.0f, 0.0f}, false);
    b2Body_SetAwake(bodyB, false);
    make_filter_joint(worldId, bodyA, bodyB);
    printf("createJoint %d %d\n", b2Body_IsAwake(bodyA), b2Body_IsAwake(bodyB));
    b2DestroyWorld(worldId);

    worldId = b2CreateWorld(&worldDef);
    bodyA = make_body(worldId, (b2Vec2){0.0f, 0.0f}, false);
    bodyB = make_body(worldId, (b2Vec2){0.0f, 0.0f}, false);
    make_filter_joint(worldId, bodyA, bodyB);
    b2Body_Disable(bodyA);
    b2Body_SetAwake(bodyB, false);
    b2Body_Enable(bodyA);
    printf("enableBody %d %d\n", b2Body_IsAwake(bodyA), b2Body_IsAwake(bodyB));
    b2DestroyWorld(worldId);

    worldId = b2CreateWorld(&worldDef);
    bodyA = make_body(worldId, (b2Vec2){3.0f, 0.0f}, true);
    bodyB = make_body(worldId, (b2Vec2){0.0f, 0.0f}, true);
    b2World_Step(worldId, 1.0f / 60.0f, 4);
    b2Body_SetAwake(bodyB, false);
    b2Body_SetTransform(bodyA, (b2Vec2){1.5f, 0.0f}, b2Rot_identity);
    b2World_Step(worldId, 1.0f / 60.0f, 4);
    printf("contact %d %d %d\n", b2Body_IsAwake(bodyA), b2Body_IsAwake(bodyB),
           b2World_GetCounters(worldId).contactCount);
    b2DestroyWorld(worldId);
    return 0;
}
