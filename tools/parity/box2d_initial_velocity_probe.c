#include <box2d/box2d.h>

#include <stdio.h>

static void print_state(const char* label, b2BodyId bodyId)
{
    b2Vec2 linear = b2Body_GetLinearVelocity(bodyId);
    b2Vec2 local = b2Body_GetLocalPointVelocity(bodyId, (b2Vec2){1.0f, 0.0f});
    b2Vec2 world = b2Body_GetWorldPointVelocity(bodyId, (b2Vec2){2.0f, 2.0f});
    printf("%s %d %.9g %.9g %.9g %.9g %.9g %.9g %.9g\n", label, b2Body_IsAwake(bodyId),
           linear.x, linear.y, b2Body_GetAngularVelocity(bodyId), local.x, local.y, world.x, world.y);
}

static b2BodyDef moving_def(void)
{
    b2BodyDef def = b2DefaultBodyDef();
    def.position = (b2Vec2){1.0f, 2.0f};
    def.linearVelocity = (b2Vec2){2.0f, -3.0f};
    def.angularVelocity = 4.0f;
    return def;
}

int main(void)
{
    b2WorldDef worldDef = b2DefaultWorldDef();
    worldDef.gravity = (b2Vec2){0.0f, 0.0f};
    b2WorldId worldId = b2CreateWorld(&worldDef);

    b2BodyDef def = moving_def();
    b2BodyId staticBody = b2CreateBody(worldId, &def);
    print_state("static", staticBody);

    def = moving_def();
    def.type = b2_dynamicBody;
    def.isAwake = false;
    b2BodyId sleepingBody = b2CreateBody(worldId, &def);
    print_state("sleeping", sleepingBody);
    b2Body_SetAwake(sleepingBody, true);
    print_state("woken", sleepingBody);

    def = moving_def();
    def.type = b2_dynamicBody;
    def.isEnabled = false;
    b2BodyId disabledBody = b2CreateBody(worldId, &def);
    print_state("disabled", disabledBody);
    b2Body_Enable(disabledBody);
    print_state("enabled", disabledBody);

    def = moving_def();
    def.type = b2_dynamicBody;
    b2BodyId awakeBody = b2CreateBody(worldId, &def);
    print_state("awake", awakeBody);

    b2DestroyWorld(worldId);
    return 0;
}
