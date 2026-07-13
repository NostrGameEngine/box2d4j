package org.box2d4j;

import org.junit.jupiter.api.Test;

import static org.box2d4j.B2.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class PublicInputValidationTest {
    @Test
    void shapeCreationRejectsInputsRejectedByUpstreamAssertions() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyId bodyId = b2CreateBody(worldId, b2DefaultBodyDef());
        try {
            b2Polygon invalidPolygon = b2MakeBox(1.0f, 1.0f);
            invalidPolygon.radius = -1.0f;
            b2Segment shortSegment = new b2Segment(new b2Vec2(), new b2Vec2());

            assertAll(
                () -> assertThrows(AssertionError.class,
                    () -> b2CreatePolygonShape(bodyId, b2DefaultShapeDef(), invalidPolygon)),
                () -> assertThrows(AssertionError.class,
                    () -> b2CreateSegmentShape(bodyId, b2DefaultShapeDef(), shortSegment)),
                () -> assertInvalidShapeDef(bodyId, shapeDef -> shapeDef.density = -1.0f),
                () -> assertInvalidShapeDef(bodyId, shapeDef -> shapeDef.material.friction = Float.NaN),
                () -> assertInvalidShapeDef(bodyId, shapeDef -> shapeDef.material.restitution = -1.0f),
                () -> assertInvalidShapeDef(bodyId, shapeDef -> shapeDef.material.rollingResistance = -1.0f),
                () -> assertInvalidShapeDef(bodyId, shapeDef -> shapeDef.material.tangentSpeed = Float.POSITIVE_INFINITY)
            );
        } finally {
            b2DestroyWorld(worldId);
        }
    }

    @Test
    void publicMutatorsPreserveUpstreamNumericPreconditions() {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());
        b2BodyDef bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);
        b2ShapeId shapeId = b2CreateCircleShape(bodyId, b2DefaultShapeDef(),
            new b2Circle(new b2Vec2(), 0.5f));
        try {
            b2MassData invalidMass = new b2MassData();
            invalidMass.mass = -1.0f;
            assertAll(
                () -> assertThrows(AssertionError.class, () -> b2Body_SetMassData(bodyId, invalidMass)),
                () -> assertThrows(AssertionError.class, () -> b2Body_SetLinearDamping(bodyId, -1.0f)),
                () -> assertThrows(AssertionError.class, () -> b2Body_SetAngularDamping(bodyId, Float.NaN)),
                () -> assertThrows(AssertionError.class, () -> b2Body_SetGravityScale(bodyId, Float.POSITIVE_INFINITY)),
                () -> assertThrows(AssertionError.class, () -> b2Shape_SetDensity(shapeId, -1.0f, true)),
                () -> assertThrows(AssertionError.class, () -> b2Shape_SetFriction(shapeId, -1.0f)),
                () -> assertThrows(AssertionError.class, () -> b2Shape_SetRestitution(shapeId, Float.NaN)),
                () -> assertThrows(AssertionError.class, () -> b2World_SetMaximumLinearSpeed(worldId, 0.0f))
            );
        } finally {
            b2DestroyWorld(worldId);
        }
    }

    private static void assertInvalidShapeDef(b2BodyId bodyId,
                                              java.util.function.Consumer<b2ShapeDef> mutation) {
        b2ShapeDef shapeDef = b2DefaultShapeDef();
        mutation.accept(shapeDef);
        assertThrows(AssertionError.class,
            () -> b2CreateCircleShape(bodyId, shapeDef, new b2Circle(new b2Vec2(), 0.5f)));
    }
}
