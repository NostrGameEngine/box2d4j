#include <box2d/box2d.h>

#include <stdio.h>

static void print_velocity(const char* label, b2BodyId bodyId)
{
    b2Vec2 linearVelocity = b2Body_GetLinearVelocity(bodyId);
    printf("%s %.9g %.9g %.9g %d\n", label, linearVelocity.x, linearVelocity.y,
           b2Body_GetAngularVelocity(bodyId), b2Body_IsAwake(bodyId));
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = (b2Vec2){0.0f, 0.0f};
    b2WorldId worldId = b2CreateWorld(&worldDef);
    b2BodyDef bodyDef = b2DefaultBodyDef();
    bodyDef.type = b2_dynamicBody;
    b2BodyId bodyId = b2CreateBody(worldId, &bodyDef);
    b2ShapeDef shapeDef = b2DefaultShapeDef();
    shapeDef.density = 1.0f;
    b2Polygon box = b2MakeBox(1.0f, 1.0f);
    b2CreatePolygonShape(bodyId, &shapeDef, &box);

    b2Body_Disable(bodyId);
    b2Transform target = {{10.0f, -3.0f}, b2MakeRot(0.5f)};
    b2Body_SetTargetTransform(bodyId, target, 0.5f);
    print_velocity("disabled", bodyId);
    b2Body_Enable(bodyId);
    print_velocity("enabled", bodyId);

    b2DestroyWorld(worldId);
    return 0;
}
