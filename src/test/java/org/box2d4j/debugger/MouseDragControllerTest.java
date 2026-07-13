package org.box2d4j.debugger;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;
import org.junit.jupiter.api.Test;

import static org.box2d4j.B2.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MouseDragControllerTest {
    @Test
    void dynamicBodyCanBePickedMovedAndReleased() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        try {
            b2BodyDef bodyDef = b2DefaultBodyDef();
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = new b2Vec2(0.0f, 4.0f);
            b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
            b2ShapeDef shapeDef = b2DefaultShapeDef();
            shapeDef.density = 1.0f;
            b2CreatePolygonShape(bodyId, shapeDef, b2MakeBox(1.0f, 1.0f));

            MouseDragController controller = new MouseDragController();
            controller.handle(worldId, MouseDragController.DOWN, 0.0f, 4.0f, 0);

            assertTrue(b2Joint_IsValid(controller.jointId()));
            assertEquals(2, b2World_GetCounters(worldId).bodyCount);
            assertEquals(1, b2World_GetCounters(worldId).jointCount);

            controller.handle(worldId, MouseDragController.MOVE, 2.0f, 5.0f, -1);
            assertEquals(2.0f, b2MouseJoint_GetTarget(controller.jointId()).x, 0.0f);
            assertEquals(5.0f, b2MouseJoint_GetTarget(controller.jointId()).y, 0.0f);

            controller.handle(worldId, MouseDragController.UP, 2.0f, 5.0f, 0);
            assertEquals(1, b2World_GetCounters(worldId).bodyCount);
            assertEquals(0, b2World_GetCounters(worldId).jointCount);
        } finally {
            b2DestroyWorld(worldId);
        }
    }
}
