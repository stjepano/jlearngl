package dev.stjepano.math.geometry.mesh;

import dev.stjepano.math.Transform;
import dev.stjepano.math.Vec2;
import dev.stjepano.math.Vec3;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public final class TriangleMeshGen implements MeshGen {
    private static final int INITIAL_VERTEX_CAPACITY = 128;  // vertices
    private static final int INITIAL_INDEX_CAPACITY = 128;   // indices

    private final Transform origin = new Transform();
    private final IndexedMesh.VertexFormat vertexFormat;

    private final Vec3 currentColor = new Vec3(1.0f, 1.0f, 1.0f);
    private final Vec3 currentNormal = new Vec3(0.0f, 0.0f, 1.0f);
    private final Vec2 currentTexCoord = new Vec2(0.0f, 0.0f);

    private final int colorOffset;
    private final int normalOffset;
    private final int texCoordOffset;
    private final boolean hasColor;
    private final boolean hasNormal;
    private final boolean hasTexCoord;


    private float[] vertexData;
    private int[] indices;
    private int vertexCount = 0;
    private int indexCount = 0;

    private final float[] currentVertex;
    private int activeTriangleBatchVertexStart = 0;
    private int activeTriangleBatchVertexCount = 0;
    private int activeTriangleBatchIndexStart = 0;
    private int activeTriangleBatchIndexCount = 0;

    private Vec3 activeColor = null;
    private Vec3 activeNormal = null;
    private Vec2 activeTexCoord = null;

    private final HashMap<Vec3, List<Integer>> vertexPositionLookup = new HashMap<>();


    /// Construct a new `TriangleMeshGen`.
    /// @param attributes a list of additional attributes, do not add POSITION as it is implicitly added. Order does not
    ///                   matter, resulting vertex data is always POSITION, NORMAL, COLOR, TEX_COORD
    public TriangleMeshGen(IndexedMesh.VertexFormat.AttributeType... attributes) {
        IndexedMesh.VertexFormat.AttributeType[] attributeTypes = new IndexedMesh.VertexFormat.AttributeType[1 + attributes.length];
        attributeTypes[0] = IndexedMesh.VertexFormat.AttributeType.POSITION;
        int i = 1;
        for (IndexedMesh.VertexFormat.AttributeType additionalAttribute : attributes) {
            attributeTypes[i++] = additionalAttribute;
        }
        vertexFormat = new IndexedMesh.VertexFormat(attributeTypes);
        colorOffset = vertexFormat.colorOffset();
        hasColor = colorOffset != -1;
        normalOffset = vertexFormat.normalOffset();
        hasNormal = normalOffset != -1;
        texCoordOffset = vertexFormat.textureCoordinateOffset();
        hasTexCoord = texCoordOffset != -1;

        this.vertexData = new float[INITIAL_VERTEX_CAPACITY * vertexFormat.stride()];
        this.indices = new int[INITIAL_INDEX_CAPACITY];

        this.currentVertex = new float[vertexFormat.stride()];
    }

    @Override
    public Transform origin() {
        return this.origin;
    }

    /// Set the color for subsequent sub-meshes.
    public MeshGen setColor(float r, float g, float b) {
        this.currentColor.set(r, g, b);
        return this;
    }

    private void beginTriangles() {
        Arrays.fill(currentVertex, 0.0f);
        activeTriangleBatchVertexStart = vertexCount;
        activeTriangleBatchVertexCount = 0;
        activeTriangleBatchIndexStart = indexCount;
        activeTriangleBatchIndexCount = 0;

        this.activeColor = null;
        this.activeNormal = null;
        this.activeTexCoord = null;
    }

    private void color(float r, float g, float b) {
        if (hasColor) {
            this.currentColor.set(r, g, b);
            this.activeColor = this.currentColor;
        }
    }

    private void normal(float x, float y, float z) {
        if (hasNormal) {
            this.currentNormal.set(x, y, z);
            this.activeNormal = this.currentNormal;
        }
    }

    private void texCoord(float s, float t) {
        if (hasTexCoord) {
            this.currentTexCoord.set(s, t);
            this.activeTexCoord = this.currentTexCoord;
        }
    }

    private void position(float x, float y, float z) {
        // set position
        currentVertex[0] = x;
        currentVertex[1] = y;
        currentVertex[2] = z;

        // set normal
        if (hasNormal && activeNormal != null) {
            currentVertex[normalOffset] = activeNormal.x;
            currentVertex[normalOffset + 1] = activeNormal.y;
            currentVertex[normalOffset + 2] = activeNormal.z;
        }

        // set color
        if (hasColor && activeColor != null) {
            currentVertex[colorOffset] = activeColor.x;
            currentVertex[colorOffset + 1] = activeColor.y;
            currentVertex[colorOffset + 2] = activeColor.z;
        }

        // set tex coord
        if (hasTexCoord && activeTexCoord != null) {
            currentVertex[texCoordOffset] = activeTexCoord.x;
            currentVertex[texCoordOffset + 1] = activeTexCoord.y;
        }

        int index = findIndexOfVertex(currentVertex);
        if (index == -1) {
            pushVertexAndIndex();
        } else {
            pushIndex(index);
        }
    }

    private int findIndexOfVertex(float[] currentVertex) {
        Vec3 searchKey = new Vec3(currentVertex[0], currentVertex[1], currentVertex[2]);
        List<Integer> candidateIndices = vertexPositionLookup.get(searchKey);
        if (candidateIndices != null) {
            for (int candidateIndex : candidateIndices) {
                boolean match = true;
                int vertexStart = candidateIndex * vertexFormat.stride();
                for (int i = 0; i < vertexFormat.stride(); i++) {
                    int a = Float.floatToIntBits(currentVertex[i]);
                    int b = Float.floatToIntBits(vertexData[vertexStart + i]);
                    if (a != b) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    return candidateIndex;
                }
            }
        }
        return -1;
    }

    private void pushIndex(int index) {
        if (activeTriangleBatchIndexStart + activeTriangleBatchIndexCount + 1 >= indices.length) {
            // expand index array
            int newCapacity = Math.max(indices.length * 2, indexCount + activeTriangleBatchIndexCount + 1);
            indices = Arrays.copyOf(indices, newCapacity);
        }
        indices[indexCount + activeTriangleBatchIndexCount] = index;
        activeTriangleBatchIndexCount++;
    }

    private int pushVertex() {
        int vertexCapacity = vertexData.length / vertexFormat.stride();
        if (activeTriangleBatchVertexStart + activeTriangleBatchVertexCount + 1 >= vertexCapacity) {
            // expand vertex array
            int newCapacity = Math.max(vertexCapacity * 2, vertexCount + activeTriangleBatchVertexCount + 1);
            vertexData = Arrays.copyOf(vertexData, newCapacity * vertexFormat.stride());
        }
        System.arraycopy(currentVertex, 0, vertexData, (vertexCount + activeTriangleBatchVertexCount) * vertexFormat.stride(), vertexFormat.stride());
        activeTriangleBatchVertexCount++;

        return vertexCount + activeTriangleBatchVertexCount - 1;
    }

    private void pushVertexAndIndex() {
        // This function is called only if vertex is not already present in lookup, so we can directly add it
        int vertexIndex = pushVertex();

        Vec3 positionKey = new Vec3(currentVertex[0], currentVertex[1], currentVertex[2]);
        vertexPositionLookup.computeIfAbsent(positionKey, k -> new java.util.ArrayList<>()).add(vertexIndex);

        pushIndex(vertexIndex);
    }

    private void endTriangles() {
        if (activeTriangleBatchIndexCount == 0) {
            throw new IllegalStateException("No triangles were added between beginTriangles() and endTriangles()");
        }

        this.origin.transformPosition(vertexData, activeTriangleBatchVertexCount, this.activeTriangleBatchVertexStart * vertexFormat.stride(), vertexFormat.stride());
        if (hasNormal) {
            this.origin.transformNormal(vertexData, activeTriangleBatchVertexCount, this.activeTriangleBatchVertexStart * vertexFormat.stride() + normalOffset, vertexFormat.stride());
        }

        vertexCount += activeTriangleBatchVertexCount;
        indexCount += activeTriangleBatchIndexCount;

        this.vertexPositionLookup.clear();
        this.activeTriangleBatchVertexStart = 0;
        this.activeTriangleBatchVertexCount = 0;
        this.activeTriangleBatchIndexStart = 0;
        this.activeTriangleBatchIndexCount = 0;

        this.activeColor = null;
        this.activeNormal = null;
        this.activeTexCoord = null;
    }

    @Override
    public MeshGen addTriangle(float width, float height) {
        final float left = -width / 2.0f;
        final float right = width / 2.0f;
        final float top = height / 2.0f;
        beginTriangles();

        color(this.currentColor.x, this.currentColor.y, this.currentColor.z);
        normal(0.0f, 0.0f, 1.0f);

        texCoord(0.0f, 0.0f);
        position(left, -top, 0.0f);

        texCoord(1.0f, 0.0f);
        position(right, -top, 0.0f);

        texCoord(0.5f, 1.0f);
        position(0.0f, top, 0.0f);

        endTriangles();
        return this;
    }

    @Override
    public MeshGen addRect(float width, float height) {
        final float left = -width/2.0f;
        final float right = width/2.0f;
        final float top = height/2.0f;
        final float bottom = -height/2.0f;

        beginTriangles();

        color(this.currentColor.x, this.currentColor.y, this.currentColor.z);
        normal(0.0f, 0.0f, 1.0f);

        // first triangle
        texCoord(0.0f, 0.0f);
        position(left, bottom, 0.0f);

        texCoord(1.0f, 0.0f);
        position(right, bottom, 0.0f);

        texCoord(1.0f, 1.0f);
        position(right, top, 0.0f);

        // second triangle
        texCoord(0.0f, 0.0f);
        position(left, bottom, 0.0f);

        texCoord(1.0f, 1.0f);
        position(right, top, 0.0f);

        texCoord(0.0f, 1.0f);
        position(left, top, 0.0f);

        endTriangles();
        return this;
    }

    @Override
    public MeshGen addGrid(float width, float height, int widthDivisions, int heightDivisions) {
        if (widthDivisions <= 0) {
            throw new IllegalArgumentException("widthDivisions must be greater than 0");
        }
        if (heightDivisions <= 0) {
            throw new IllegalArgumentException("heightDivisions must be greater than 0");
        }

        final float left = -width/2.0f;
        final float bottom = -height/2.0f;
        final float dW = width / widthDivisions;
        final float dH = height / heightDivisions;
        final float dS = 1.0f / widthDivisions;
        final float dT = 1.0f / heightDivisions;

        beginTriangles();
        color(this.currentColor.x, this.currentColor.y, this.currentColor.z);
        normal(0.0f, 0.0f, 1.0f);

        for (int row = 0; row < heightDivisions; row++) {
            for (int col = 0; col < widthDivisions; col++) {
                float x0 = left + col * dW;
                float x1 = left + (col + 1) * dW;
                float y0 = bottom + row * dH;
                float y1 = bottom + (row + 1) * dH;
                float s0 = col * dS;
                float s1 = (col + 1) * dS;
                float t0 = row * dT;
                float t1 = (row + 1) * dT;



                // first triangle
                texCoord(s0, t0);
                position(x0, y0, 0.0f);

                texCoord(s1, t0);
                position(x1, y0, 0.0f);

                texCoord(s1, t1);
                position(x1, y1, 0.0f);

                // second triangle
                texCoord(s0, t0);
                position(x0, y0, 0.0f);

                texCoord(s1, t1);
                position(x1, y1, 0.0f);

                texCoord(s0, t1);
                position(x0, y1, 0.0f);

            }

        }
        endTriangles();

        return this;
    }

    @Override
    public MeshGen addCube(float width, float height, float depth) {
        float left = -width/2.0f;
        float right = -left;
        float bottom = -height/2.0f;
        float top = -bottom;
        float back = -depth/2.0f;
        float front = -back;

        beginTriangles();
        color(this.currentColor.x, this.currentColor.y, this.currentColor.z);

        // front face
        normal(0.0f, 0.0f, 1.0f);
        texCoord(0, 0);
        position(left, bottom, front);

        texCoord(1, 0);
        position(right, bottom, front);

        texCoord(1, 1);
        position(right, top, front);

        texCoord(0, 0);
        position(left, bottom, front);

        texCoord(1, 1);
        position(right, top, front);

        texCoord(0, 1);
        position(left, top, front);

        // left face
        normal(-1.0f, 0.0f, 0.0f);
        texCoord(0, 0);
        position(left, bottom, back);

        texCoord(1, 0);
        position(left, bottom, front);

        texCoord(1, 1);
        position(left, top, front);

        texCoord(0, 0);
        position(left, bottom, back);

        texCoord(1, 1);
        position(left, top, front);

        texCoord(0, 1);
        position(left, top, back);

        // back face
        normal(0.0f, 0.0f, -1.0f);
        texCoord(0, 0);
        position(right, bottom, back);

        texCoord(1, 0);
        position(left, bottom, back);

        texCoord(1, 1);
        position(left, top, back);

        texCoord(0, 0);
        position(right, bottom, back);

        texCoord(1, 1);
        position(left, top, back);

        texCoord(0, 1);
        position(right, top, back);

        // right face
        normal(1.0f, 0.0f, 0.0f);
        texCoord(0, 0);
        position(right, bottom, front);

        texCoord(1, 0);
        position(right, bottom, back);

        texCoord(1, 1);
        position(right, top, back);

        texCoord(0, 0);
        position(right, bottom, front);

        texCoord(1, 1);
        position(right, top, back);

        texCoord(0, 1);
        position(right, top, front);

        // top face
        normal(0.0f, 1.0f, 0.0f);
        texCoord(0, 0);
        position(left, top, front);

        texCoord(1, 0);
        position(right, top, front);

        texCoord(1, 1);
        position(right, top, back);

        texCoord(0, 0);
        position(left, top, front);

        texCoord(1, 1);
        position(right, top, back);

        texCoord(0, 1);
        position(left, top, back);

        // bottom face
        normal(0.0f, -1.0f, 0.0f);
        texCoord(0, 0);
        position(left, bottom, back);

        texCoord(1, 0);
        position(right, bottom, back);

        texCoord(1, 1);
        position(right, bottom, front);

        texCoord(0, 0);
        position(left, bottom, back);

        texCoord(1, 1);
        position(right, bottom, front);

        texCoord(0, 1);
        position(left, bottom, front);


        endTriangles();

        return this;
    }

    @Override
    public MeshGen addCylinder(float radius, float height, int numSlices, int numRings, boolean caps) {
        float dTheta = (float)(2.0f * Math.PI / numSlices);
        float dHeight = height / numRings;
        float bottom = -height / 2.0f;
        float top = -bottom;

        beginTriangles();
        color(this.currentColor.x, this.currentColor.y, this.currentColor.z);
        for (int row = 0; row < numRings; row++) {
            float theta = (float) Math.PI / 2.0f;
            for (int col = 0; col < numSlices; col++) {
                float x0 = radius * (float)Math.cos(theta);
                float z0 = radius * (float)Math.sin(theta);
                float x1 = radius * (float)Math.cos(theta + dTheta);
                float z1 = radius * (float)Math.sin(theta + dTheta);
                float y0 = bottom + row * dHeight;
                float y1 = bottom + (row + 1) * dHeight;

                float s0 = (float)col / (float)numSlices;
                float s1 = (float)(col + 1) / (float)numSlices;
                float t0 = (float)row / (float)numRings;
                float t1 = (float)(row + 1) / (float)numRings;

                // first triangle
                normal(x0/radius, 0.0f, z0/radius);
                texCoord(s0, t0);
                position(x0, y0, z0);

                normal(x1/radius, 0.0f, z1/radius);
                texCoord(s1, t1);
                position(x1, y1, z1);

                normal(x1/radius, 0.0f, z1/radius);
                texCoord(s1, t0);
                position(x1, y0, z1);

                // second triangle
                normal(x0/radius, 0.0f, z0/radius);
                texCoord(s0, t0);
                position(x0, y0, z0);

                normal(x0/radius, 0.0f, z0/radius);
                texCoord(s0, t1);
                position(x0, y1, z0);

                normal(x1/radius, 0.0f, z1/radius);
                texCoord(s1, t1);
                position(x1, y1, z1);


                theta += dTheta;
            }
        }

        if (caps) {
            // top cap
            normal(0.0f, 1.0f, 0.0f);
            float theta = (float) Math.PI / 2.0f;
            for (int col = 0; col < numSlices; col++) {
                float x0 = radius * (float)Math.cos(theta);
                float z0 = radius * (float)Math.sin(theta);
                float x1 = radius * (float)Math.cos(theta + dTheta);
                float z1 = radius * (float)Math.sin(theta + dTheta);

                float s0 = 0.5f + 0.5f * (x0 / radius);
                float t0 = 0.5f + 0.5f * (z0 / radius);
                float s1 = 0.5f + 0.5f * (x1 / radius);
                float t1 = 0.5f + 0.5f * (z1 / radius);

                texCoord(0.5f, 0.5f);
                position(0.0f, top, 0.0f);

                texCoord(s1, t1);
                position(x1, top, z1);

                texCoord(s0, t0);
                position(x0, top, z0);

                theta += dTheta;
            }

            // bottom cap
            normal(0.0f, -1.0f, 0.0f);
            theta = (float) Math.PI / 2.0f;
            for (int col = 0; col < numSlices; col++) {
                float x0 = radius * (float) Math.cos(theta);
                float z0 = radius * (float) Math.sin(theta);
                float x1 = radius * (float) Math.cos(theta + dTheta);
                float z1 = radius * (float) Math.sin(theta + dTheta);

                float s0 = 0.5f + 0.5f * (x0 / radius);
                float t0 = 0.5f + 0.5f * (z0 / radius);
                float s1 = 0.5f + 0.5f * (x1 / radius);
                float t1 = 0.5f + 0.5f * (z1 / radius);

                texCoord(0.5f, 0.5f);
                position(0.0f, bottom, 0.0f);

                texCoord(s0, t0);
                position(x0, bottom, z0);

                texCoord(s1, t1);
                position(x1, bottom, z1);
                theta += dTheta;
            }
        }

        endTriangles();

        return this;
    }

    @Override
    public MeshGen addSphere(float radius, int numSlices, int numRings) {
        return null;
    }

    @Override
    public MeshGen addCone(float radius, float height, int numSlices, int numRings) {
        return null;
    }

    @Override
    public MeshGen addPyramid(float width, float height, float depth) {
        return null;
    }

    @Override
    public IndexedMesh build() {
        final float[] finalVertexData = Arrays.copyOf(vertexData, vertexCount * vertexFormat.stride());
        final int[] finalIndices = Arrays.copyOf(indices, indexCount);
        return new IndexedMesh(this.vertexFormat, IndexedMesh.PrimitiveType.TRIANGLES, finalVertexData, finalIndices);
    }
}
