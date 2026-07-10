package org.box2d4j.samples;

import org.box2d4j.b2BodyDef;
import org.box2d4j.b2BodyId;
import org.box2d4j.b2Capsule;
import org.box2d4j.b2ChainDef;
import org.box2d4j.b2ChainId;
import org.box2d4j.b2Circle;
import org.box2d4j.b2Counters;
import org.box2d4j.b2Polygon;
import org.box2d4j.b2ShapeDef;
import org.box2d4j.b2ShapeId;
import org.box2d4j.b2SurfaceMaterial;
import org.box2d4j.b2Vec2;
import org.box2d4j.b2WorldId;

import java.util.Locale;

import static org.box2d4j.B2.*;

public final class ChainShape {
    private static final int STEP_COUNT = 240;

    public static final int CIRCLE_SHAPE = 0;
    public static final int CAPSULE_SHAPE = 1;
    public static final int BOX_SHAPE = 2;

    private ChainShape() {
    }

    public static Result run() {
        if (SampleRuntime.isActive()) {
            return new Result(new VariantResult[] {runVariant(CIRCLE_SHAPE, STEP_COUNT)});
        }
        VariantResult[] variants = {
            runVariant(CIRCLE_SHAPE, STEP_COUNT),
            runVariant(CAPSULE_SHAPE, STEP_COUNT),
            runVariant(BOX_SHAPE, STEP_COUNT)
        };
        return new Result(variants);
    }

    public static VariantResult runVariant(int shapeType, int stepCount) {
        b2WorldId worldId = b2CreateWorld(b2DefaultWorldDef());

        b2BodyDef bodyDef = b2DefaultBodyDef();
        b2BodyId groundId = b2CreateBody(worldId, bodyDef);

        b2SurfaceMaterial material = new b2SurfaceMaterial();
        material.friction = 0.2f;
        material.customColor = b2_colorSteelBlue;
        material.userMaterialId = 42;

        b2ChainDef chainDef = b2DefaultChainDef();
        chainDef.points = chainPoints();
        chainDef.count = chainDef.points.length;
        chainDef.materials = new b2SurfaceMaterial[] {material};
        chainDef.materialCount = 1;
        chainDef.isLoop = true;
        b2ChainId chainId = b2CreateChain(groundId, chainDef);

        bodyDef = b2DefaultBodyDef();
        bodyDef.type = b2_dynamicBody;
        bodyDef.position = new b2Vec2(-55.0f, 13.5f);
        b2BodyId bodyId = b2CreateBody(worldId, bodyDef);

        b2ShapeDef shapeDef = b2DefaultShapeDef();
        shapeDef.density = 1.0f;
        shapeDef.material.friction = 0.2f;
        shapeDef.material.restitution = 0.0f;

        b2ShapeId shapeId;
        if (shapeType == CIRCLE_SHAPE) {
            shapeId = b2CreateCircleShape(bodyId, shapeDef, new b2Circle(new b2Vec2(0.0f, 0.0f), 0.5f));
        } else if (shapeType == CAPSULE_SHAPE) {
            shapeId = b2CreateCapsuleShape(bodyId, shapeDef,
                new b2Capsule(new b2Vec2(-0.5f, 0.0f), new b2Vec2(0.5f, 0.0f), 0.25f));
        } else {
            shapeId = b2CreatePolygonShape(bodyId, shapeDef, b2MakeBox(0.5f, 0.5f));
        }
        SampleRuntime.choice("chainShape.shape", "Shape", shapeType,
            new String[] {"Circle", "Capsule", "Box"}, value -> setShape(shapeId, value));
        SampleRuntime.slider("chainShape.friction", "Friction", shapeDef.material.friction, 0.0f, 1.0f, 0.01f,
            value -> b2Shape_SetFriction(shapeId, value));
        SampleRuntime.slider("chainShape.restitution", "Restitution", shapeDef.material.restitution,
            0.0f, 2.0f, 0.1f, value -> b2Shape_SetRestitution(shapeId, value));
        SampleRuntime.action("chainShape.launch", "Launch", () -> {
            b2Body_SetTransform(bodyId, new b2Vec2(-55.0f, 13.5f), b2Rot_identity);
            b2Body_SetLinearVelocity(bodyId, new b2Vec2(10.0f, 0.0f));
            b2Body_SetAngularVelocity(bodyId, 0.0f);
        });

        for (int step = 0; step < stepCount; ++step) {
            b2World_Step(worldId, 1.0f / 60.0f, 4);
        }

        b2Counters counters = b2World_GetCounters(worldId);
        VariantResult result = new VariantResult(shapeType, b2Chain_GetSegmentCount(chainId), counters.bodyCount,
            counters.shapeCount, counters.contactCount, b2World_GetAwakeBodyCount(worldId), bodyState(bodyId));
        b2DestroyWorld(worldId);
        return result;
    }

    private static void setShape(b2ShapeId shapeId, int shapeType) {
        if (shapeType == CIRCLE_SHAPE) {
            b2Shape_SetCircle(shapeId, new b2Circle(new b2Vec2(), 0.5f));
        } else if (shapeType == CAPSULE_SHAPE) {
            b2Shape_SetCapsule(shapeId,
                new b2Capsule(new b2Vec2(-0.5f, 0.0f), new b2Vec2(0.5f, 0.0f), 0.25f));
        } else {
            b2Shape_SetPolygon(shapeId, b2MakeBox(0.5f, 0.5f));
        }
        b2Body_ApplyMassFromShapes(b2Shape_GetBody(shapeId));
    }

    private static BodyState bodyState(b2BodyId bodyId) {
        b2Vec2 p = b2Body_GetPosition(bodyId);
        b2Vec2 v = b2Body_GetLinearVelocity(bodyId);
        return new BodyState(p.x, p.y, b2Rot_GetAngle(b2Body_GetRotation(bodyId)),
            v.x, v.y, b2Body_GetAngularVelocity(bodyId));
    }

    private static b2Vec2[] chainPoints() {
        return new b2Vec2[] {
            new b2Vec2(-56.885498f, 12.8985004f),
            new b2Vec2(-56.885498f, 16.2057495f),
            new b2Vec2(56.885498f, 16.2057495f),
            new b2Vec2(56.885498f, -16.2057514f),
            new b2Vec2(51.5935059f, -16.2057514f),
            new b2Vec2(43.6559982f, -10.9139996f),
            new b2Vec2(35.7184982f, -10.9139996f),
            new b2Vec2(27.7809982f, -10.9139996f),
            new b2Vec2(21.1664963f, -14.2212505f),
            new b2Vec2(11.9059982f, -16.2057514f),
            new b2Vec2(0.0f, -16.2057514f),
            new b2Vec2(-10.5835037f, -14.8827496f),
            new b2Vec2(-17.1980019f, -13.5597477f),
            new b2Vec2(-21.1665001f, -12.2370014f),
            new b2Vec2(-25.1355019f, -9.5909977f),
            new b2Vec2(-31.75f, -3.63799858f),
            new b2Vec2(-38.3644981f, 6.2840004f),
            new b2Vec2(-42.3334999f, 9.59125137f),
            new b2Vec2(-47.625f, 11.5755005f),
            new b2Vec2(-56.885498f, 12.8985004f)
        };
    }

    public static void main(String[] args) {
        System.out.println(run().toLine());
    }

    public static final class Result {
        public final VariantResult[] variants;

        public Result(VariantResult[] variants) {
            this.variants = variants;
        }

        public String toLine() {
            StringBuilder builder = new StringBuilder();
            builder.append("chainShape ").append(variants.length);
            for (VariantResult variant : variants) {
                builder.append(variant.toLinePart());
            }
            return builder.toString();
        }
    }

    public static final class VariantResult {
        public final int shapeType;
        public final int chainSegmentCount;
        public final int bodyCount;
        public final int shapeCount;
        public final int contactCount;
        public final int awakeBodyCount;
        public final BodyState body;

        public VariantResult(int shapeType, int chainSegmentCount, int bodyCount, int shapeCount, int contactCount,
                             int awakeBodyCount, BodyState body) {
            this.shapeType = shapeType;
            this.chainSegmentCount = chainSegmentCount;
            this.bodyCount = bodyCount;
            this.shapeCount = shapeCount;
            this.contactCount = contactCount;
            this.awakeBodyCount = awakeBodyCount;
            this.body = body;
        }

        String toLinePart() {
            return " " + shapeType + " " + chainSegmentCount + " " + bodyCount + " " + shapeCount + " "
                + contactCount + " " + awakeBodyCount + body.toLinePart();
        }
    }

    public static final class BodyState {
        public final float x;
        public final float y;
        public final float angle;
        public final float velocityX;
        public final float velocityY;
        public final float angularVelocity;

        BodyState(float x, float y, float angle, float velocityX, float velocityY, float angularVelocity) {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.angularVelocity = angularVelocity;
        }

        String toLinePart() {
            return String.format(Locale.ROOT, " %s %s %s %s %s %s",
                formatFloat(x), formatFloat(y), formatFloat(angle), formatFloat(velocityX), formatFloat(velocityY),
                formatFloat(angularVelocity));
        }
    }

    private static String formatFloat(float value) {
        if (value == 0.0f) {
            return "0";
        }
        String text = String.format(Locale.ROOT, "%.9g", value);
        int exponent = Math.max(text.indexOf('e'), text.indexOf('E'));
        String suffix = "";
        if (exponent >= 0) {
            suffix = text.substring(exponent);
            text = text.substring(0, exponent);
        }
        if (text.indexOf('.') >= 0) {
            while (text.endsWith("0")) {
                text = text.substring(0, text.length() - 1);
            }
            if (text.endsWith(".")) {
                text = text.substring(0, text.length() - 1);
            }
        }
        return text + suffix;
    }
}
