#include <box2d/box2d.h>

#include <stdio.h>

static b2BodyId make_body(b2WorldId worldId, float x)
{
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    bodyDef.position = (b2Vec2){x, 0.0f};
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.0f;
    b2Circle circle = {{0.0f, 0.0f}, 1.0f};
    b2CreateCircleShape(bodyId, &shapeDef, &circle);
    return bodyId;
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = (b2Vec2){0.0f, 0.0f};
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyId bodyA = make_body(worldId, -0.5f);
    b2BodyId bodyB = make_body(worldId, 0.5f);

    b2World_Step(worldId, 1.0f / 60.0f, 4);
    b2Body_SetAwake(bodyA, false);
    printf("before %d %d %d\n", b2Body_IsAwake(bodyA), b2Body_IsAwake(bodyB),
           b2World_GetCounters(worldId).contactCount);
    b2Body_SetType(bodyA, b2_staticBody);
    printf("after %d %d %d %d\n", b2Body_GetType(bodyA), b2Body_IsAwake(bodyA),
           b2Body_IsAwake(bodyB), b2World_GetCounters(worldId).contactCount);

    b2DestroyWorld(worldId);
    return 0;
}
