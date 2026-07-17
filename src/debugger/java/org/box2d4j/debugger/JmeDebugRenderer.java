package org.box2d4j.debugger;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;

final class JmeDebugRenderer {
    private final Mesh fillMesh = new Mesh();
    private final Mesh lineMesh = new Mesh();
    private final Geometry fillGeometry = new Geometry("Box2D fills", fillMesh);
    private final Geometry lineGeometry = new Geometry("Box2D outlines", lineMesh);
    private final Node labelNode = new Node("Box2D labels");
    private final BitmapFont font;
    private final MeshScratch fillScratch = new MeshScratch();
    private final MeshScratch lineScratch = new MeshScratch();
    private final ArrayList<BitmapText> labelTexts = new ArrayList<>();

    JmeDebugRenderer(AssetManager assetManager) {
        font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        fillMesh.setMode(Mesh.Mode.Triangles);
        lineMesh.setMode(Mesh.Mode.Lines);
        Material fillMaterial = vertexColorMaterial(assetManager, true);
        Material lineMaterial = vertexColorMaterial(assetManager, false);
        lineMaterial.getAdditionalRenderState().setLineWidth(1.35f);
        fillGeometry.setMaterial(fillMaterial);
        lineGeometry.setMaterial(lineMaterial);
        fillGeometry.setLocalTranslation(0.0f, 0.0f, 1.0f);
        lineGeometry.setLocalTranslation(0.0f, 0.0f, 2.0f);
    }

    Geometry fillGeometry() {
        return fillGeometry;
    }

    Geometry lineGeometry() {
        return lineGeometry;
    }

    Node labelNode() {
        return labelNode;
    }

    void upload(WorldDrawBatch batch, ViewTransform view, float contentWidth, float viewportHeight) {
        uploadMesh(fillMesh, fillGeometry, batch.fillVertices, batch.fillColors, view, contentWidth, viewportHeight,
            0.48f, fillScratch);
        uploadMesh(lineMesh, lineGeometry, batch.lineVertices, batch.lineColors, view, contentWidth, viewportHeight,
            0.96f, lineScratch);
        int labelCount = batch.labelCount();
        for (int i = 0; i < labelCount; ++i) {
            WorldDrawBatch.Label label = batch.labels.get(i);
            BitmapText text;
            if (i == labelTexts.size()) {
                text = new BitmapText(font);
                labelTexts.add(text);
                labelNode.attachChild(text);
            } else {
                text = labelTexts.get(i);
            }
            text.setCullHint(Spatial.CullHint.Never);
            text.setText(label.text);
            text.setSize(12.0f);
            ColorRGBA labelColor = text.getColor();
            labelColor.set(
                ((label.color >>> 16) & 0xFF) / 255.0f,
                ((label.color >>> 8) & 0xFF) / 255.0f,
                (label.color & 0xFF) / 255.0f,
                1.0f);
            text.setColor(labelColor);
            text.setLocalTranslation(
                0.5f * contentWidth + (label.x - view.centerX) * view.pixelsPerMeter,
                0.5f * viewportHeight + (label.y - view.centerY) * view.pixelsPerMeter,
                3.0f);
        }
        for (int i = labelCount; i < labelTexts.size(); ++i) {
            labelTexts.get(i).setCullHint(Spatial.CullHint.Always);
        }
    }

    void clear() {
        fillGeometry.setCullHint(Spatial.CullHint.Always);
        lineGeometry.setCullHint(Spatial.CullHint.Always);
        for (BitmapText text : labelTexts) {
            text.setCullHint(Spatial.CullHint.Always);
        }
    }

    private static void uploadMesh(Mesh mesh, Geometry geometry, WorldDrawBatch.FloatList vertices,
                                   WorldDrawBatch.IntList colors, ViewTransform view, float contentWidth,
                                   float viewportHeight, float alpha, MeshScratch scratch) {
        int vertexCount = vertices.size() / 2;
        if (vertexCount == 0) {
            geometry.setCullHint(Spatial.CullHint.Always);
            return;
        }
        scratch.ensure(vertexCount);
        FloatBuffer positions = scratch.positions;
        FloatBuffer rgba = scratch.colors;
        positions.clear();
        rgba.clear();
        for (int i = 0; i < vertexCount; ++i) {
            float worldX = vertices.get(2 * i);
            float worldY = vertices.get(2 * i + 1);
            positions.put(0.5f * contentWidth + (worldX - view.centerX) * view.pixelsPerMeter);
            positions.put(0.5f * viewportHeight + (worldY - view.centerY) * view.pixelsPerMeter);
            positions.put(0.0f);
            int color = colors.get(i);
            rgba.put(((color >>> 16) & 0xFF) / 255.0f);
            rgba.put(((color >>> 8) & 0xFF) / 255.0f);
            rgba.put((color & 0xFF) / 255.0f);
            rgba.put(alpha);
        }
        positions.flip();
        rgba.flip();
        updateBuffer(mesh, VertexBuffer.Type.Position, 3, positions);
        updateBuffer(mesh, VertexBuffer.Type.Color, 4, rgba);
        mesh.setStreamed();
        mesh.updateCounts();
        mesh.updateBound();
        geometry.setCullHint(Spatial.CullHint.Never);
    }

    private static void updateBuffer(Mesh mesh, VertexBuffer.Type type, int components, FloatBuffer values) {
        VertexBuffer buffer = mesh.getBuffer(type);
        if (buffer == null) {
            mesh.setBuffer(type, components, values);
        } else {
            buffer.updateData(values);
        }
    }

    private static final class MeshScratch {
        FloatBuffer positions = BufferUtils.createFloatBuffer(3 * 1024);
        FloatBuffer colors = BufferUtils.createFloatBuffer(4 * 1024);

        void ensure(int vertexCount) {
            if (positions.capacity() < 3 * vertexCount) {
                int capacity = Math.max(3 * vertexCount, positions.capacity() + positions.capacity() / 2);
                positions = BufferUtils.createFloatBuffer(capacity);
            }
            if (colors.capacity() < 4 * vertexCount) {
                int capacity = Math.max(4 * vertexCount, colors.capacity() + colors.capacity() / 2);
                colors = BufferUtils.createFloatBuffer(capacity);
            }
        }
    }

    private static Material vertexColorMaterial(AssetManager assetManager, boolean blended) {
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setBoolean("VertexColor", true);
        if (blended) {
            material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            material.getAdditionalRenderState().setDepthWrite(false);
        }
        return material;
    }

    static final class ViewTransform {
        float centerX;
        float centerY = 8.0f;
        float pixelsPerMeter = 30.0f;

        void fit(WorldDrawBatch.Bounds bounds, float width, float height) {
            if (!bounds.isValid()) {
                return;
            }
            centerX = 0.5f * (bounds.minX + bounds.maxX);
            centerY = 0.5f * (bounds.minY + bounds.maxY);
            float usableWidth = Math.max(100.0f, width - 64.0f);
            float usableHeight = Math.max(100.0f, height - 96.0f);
            float scaleX = usableWidth / Math.max(1.0f, bounds.width());
            float scaleY = usableHeight / Math.max(1.0f, bounds.height());
            pixelsPerMeter = Math.max(2.0f, Math.min(350.0f, Math.min(scaleX, scaleY)));
        }

        void zoom(float factor, float anchorX, float anchorY, float contentWidth, float viewportHeight) {
            float beforeX = centerX + (anchorX - 0.5f * contentWidth) / pixelsPerMeter;
            float beforeY = centerY + (anchorY - 0.5f * viewportHeight) / pixelsPerMeter;
            pixelsPerMeter = Math.max(2.0f, Math.min(1200.0f, pixelsPerMeter * factor));
            centerX = beforeX - (anchorX - 0.5f * contentWidth) / pixelsPerMeter;
            centerY = beforeY - (anchorY - 0.5f * viewportHeight) / pixelsPerMeter;
        }
    }
}
