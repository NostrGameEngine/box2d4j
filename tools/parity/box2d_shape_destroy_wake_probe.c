#include <box2d/box2d.h>

#include <stdio.h>

static b2BodyId make_circle_body(b2WorldId worldId, b2BodyType type, b2Vec2 position, b2ShapeId* shapeId)
{
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = type;
    bodyDef.position = position;
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = type == b2_dynamicBody ? 1.0f : 0.0f;
    b2Circle circle = {{0.0f, 0.0f}, 1.0f};
    *shapeId = b2CreateCircleShape(bodyId, &shapeDef, &circle);
    return bodyId;
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = (b2Vec2){0.0f, 0.0f};

    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2ShapeId staticShape;
    b2ShapeId dynamicShape;
    make_circle_body(worldId, b2_staticBody, (b2Vec2){0.0f, 0.0f}, &staticShape);
    b2BodyId dynamicBody = make_circle_body(worldId, b2_dynamicBody, (b2Vec2){1.0f, 0.0f}, &dynamicShape);
    b2World_Step(worldId, 1.0f / 60.0f, 4);
    b2Body_SetAwake(dynamicBody, false);
    printf("shapeBefore %d %d\n", b2Body_IsAwake(dynamicBody), b2World_GetCounters(worldId).contactCount);
    b2DestroyShape(staticShape, false);
    printf("shapeAfter %d %d %d\n", b2Body_IsAwake(dynamicBody), b2Shape_IsValid(staticShape),
           b2World_GetCounters(worldId).contactCount);
    b2DestroyWorld(worldId);

    worldId = b2CreateWorld(&worldDef);
    b2BodyDef groundDef = b2DefaultBodyDef();
    b2BodyId groundId = b2CreateBody(worldId, &groundDef);
    b2Vec2 points[4] = {{-2.0f, -1.0f}, {2.0f, -1.0f}, {2.0f, 1.0f}, {-2.0f, 1.0f}};
    b2ChainDef chainDef = b2DefaultChainDef();
    chainDef.points = points;
    chainDef.count = 4;
    chainDef.isLoop = true;
    b2ChainId chainId = b2CreateChain(groundId, &chainDef);
    dynamicBody = make_circle_body(worldId, b2_dynamicBody, (b2Vec2){0.0f, -1.5f}, &dynamicShape);
    b2World_Step(worldId, 1.0f / 60.0f, 4);
    b2Body_SetAwake(dynamicBody, false);
    printf("chainBefore %d %d\n", b2Body_IsAwake(dynamicBody), b2World_GetCounters(worldId).contactCount);
    b2DestroyChain(chainId);
    printf("chainAfter %d %d %d\n", b2Body_IsAwake(dynamicBody), b2Chain_IsValid(chainId),
           b2World_GetCounters(worldId).contactCount);
    b2DestroyWorld(worldId);
    return 0;
}
