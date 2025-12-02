package dev.stjepano.math.geometry.mesh;

import dev.stjepano.math.MathUtil;
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
        generateCylinderShellTriangles(radius, height, numSlices, numRings, 0, 1);

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

    /// Generate cylinder shell triangles without colors. You must wrap in beginTriangles() and endTriangles().
    private void generateCylinderShellTriangles(float radius, float height, int numSlices, int numRings, float minT, float maxT) {
        float dTheta = (float)(2.0f * Math.PI / numSlices);
        float dHeight = height / numRings;
        float bottom = -height / 2.0f;
        float top = -bottom;

        float tRange = maxT - minT;

        for (int row = 0; row < numRings; row++) {
            float theta = (float) Math.PI / 2.0f;
            for (int col = 0; col < numSlices; col++) {
                final float nx0 = (float)Math.cos(theta);
                final float nz0 = (float)Math.sin(theta);
                final float nx1 = (float)Math.cos(theta + dTheta);
                final float nz1 = (float)Math.sin(theta + dTheta);
                final float x0 = radius * nx0;
                final float z0 = radius * nz0;
                final float x1 = radius * nx1;
                final float z1 = radius * nz1;
                final float y0 = bottom + row * dHeight;
                final float y1 = bottom + (row + 1) * dHeight;

                final float s0 = (float)col / (float)numSlices;
                final float s1 = (float)(col + 1) / (float)numSlices;
                final float t0 = (row / (float)numRings) * tRange + minT;
                final float t1 = ((row + 1) / (float)numRings) * tRange + minT;

                // first triangle
                normal(nx0, 0.0f, nz0);
                texCoord(s0, t0);
                position(x0, y0, z0);

                normal(nx1, 0.0f, nz1);
                texCoord(s1, t1);
                position(x1, y1, z1);

                normal(nx1, 0.0f, nz1);
                texCoord(s1, t0);
                position(x1, y0, z1);

                // second triangle
                normal(nx0, 0.0f, nz0);
                texCoord(s0, t0);
                position(x0, y0, z0);

                normal(nx0, 0.0f, nz0);
                texCoord(s0, t1);
                position(x0, y1, z0);

                normal(nx1, 0.0f, nz1);
                texCoord(s1, t1);
                position(x1, y1, z1);


                theta += dTheta;
            }
        }
    }

    @Override
    public MeshGen addSphere(float radius, int numSlices, int numRings) {

        /*

        Coordinate system is as follows:

            y
            |
            |
            |____ x
           /
          / z

          Usually spherical coordinates are defined as:
            x = r sin(theta) cos(phi)
            y = r sin(theta) sin(phi)
            z = r cos(theta)

          However, to match the above coordinate system, we have z in place of x, x in place of y and y in place of z:
            x = r sin(phi) cos(theta)
            y = r cos(phi)
            z = r sin(phi) sin(theta)

          Theta varies from 0 to 2PI (around y axis)
          Phi varies from 0 to PI (from top to bottom)

         */
        float dTheta = (float)(2.0f * Math.PI / numSlices);
        float dPhi = (float)(Math.PI / numRings);
        float dS = 1.0f / numSlices;
        float dT = 1.0f / numRings;

        beginTriangles();
        color(this.currentColor.x, this.currentColor.y, this.currentColor.z);

        float phi = 0.0f;
        for (int row = 0; row < numRings; row++) {
            float theta = (float) Math.PI / 2.0f; // start at "back" of sphere (-z)
            for (int col = 0; col < numSlices; col++) {
                final float sinTheta0 = (float)Math.sin(theta);
                final float cosTheta0 = (float)Math.cos(theta);
                final float sinTheta1 = (float)Math.sin(theta + dTheta);
                final float cosTheta1 = (float)Math.cos(theta + dTheta);
                final float sinPhi0 = (float)Math.sin(phi);
                final float cosPhi0 = (float)Math.cos(phi);
                final float sinPhi1 = (float)Math.sin(phi + dPhi);
                final float cosPhi1 = (float)Math.cos(phi + dPhi);

                final float nx0 = sinPhi0 * cosTheta0;
                //noinspection UnnecessaryLocalVariable
                final float ny0 = cosPhi0;
                final float nz0 = sinPhi0 * sinTheta0;

                final float nx1 = sinPhi0 * cosTheta1;
                //noinspection UnnecessaryLocalVariable
                final float ny1 = cosPhi0;
                final float nz1 = sinPhi0 * sinTheta1;

                final float nx2 = sinPhi1 * cosTheta1;
                //noinspection UnnecessaryLocalVariable
                final float ny2 = cosPhi1;
                final float nz2 = sinPhi1 * sinTheta1;

                final float nx3 = sinPhi1 * cosTheta0;
                //noinspection UnnecessaryLocalVariable
                final float ny3 = cosPhi1;
                final float nz3 = sinPhi1 * sinTheta0;

                // first triangle
                normal(nx0, ny0, nz0);
                texCoord((float)col * dS, 1.0f - (float)row * dT);
                position(radius * nx0, radius * ny0, radius * nz0);

                normal(nx1, ny1, nz1);
                texCoord((float)(col + 1) * dS, 1.0f - (float)row * dT);
                position(radius * nx1, radius * ny1, radius * nz1);

                normal(nx2, ny2, nz2);
                texCoord((float)(col + 1) * dS, 1.0f - (float)(row + 1) * dT);
                position(radius * nx2, radius * ny2, radius * nz2);

                // second triangle
                normal(nx0, ny0, nz0);
                texCoord((float)col * dS, 1.0f - (float)row * dT);
                position(radius * nx0, radius * ny0, radius * nz0);

                normal(nx2, ny2, nz2);
                texCoord((float)(col + 1) * dS, 1.0f - (float)(row + 1) * dT);
                position(radius * nx2, radius * ny2, radius * nz2);

                normal(nx3, ny3, nz3);
                texCoord((float)col * dS, 1.0f - (float)(row + 1) * dT);
                position(radius * nx3, radius * ny3, radius * nz3);

                theta += dTheta;
            }
            phi += dPhi;
        }

        endTriangles();
        return this;
    }

    @Override
    public MeshGen addCone(float radius, float height, int numSlices, int numRings) {
        final float dTheta = (float)(2.0f * Math.PI / numSlices);
        final float dHeight = height / numRings;
        final float dRadius = radius / numRings;
        final float bottom = -height / 2.0f;
        final float top = -bottom;

        // For smooth normals on cone surface
        // Normal direction: (height * cos(theta), radius, height * sin(theta)) normalized
        final float slantHeight = (float)Math.sqrt(height * height + radius * radius);
        final float normalY = radius / slantHeight;
        final float normalXZScale = height / slantHeight;

        final float dS = 1.0f / numSlices;
        final float dT = 1.0f / numRings;

        beginTriangles();
        color(this.currentColor.x, this.currentColor.y, this.currentColor.z);

        // Cone shell (lateral surface)
        for (int ring = 0; ring < numRings; ring++) {
            float theta = (float) Math.PI / 2.0f;  // Reset theta each ring!

            float y0 = bottom + ring * dHeight;
            float y1 = bottom + (ring + 1) * dHeight;

            // Radius decreases linearly from base to apex
            float r0 = radius - ring * dRadius;
            float r1 = radius - (ring + 1) * dRadius;

            float t0 = (float) ring / numRings;
            float t1 = (float) (ring + 1) / numRings;

            boolean isApex = (r1 <= MathUtil.EPSILON4);

            for (int slice = 0; slice < numSlices; slice++) {
                float cosTheta0 = (float) Math.cos(theta);
                float sinTheta0 = (float) Math.sin(theta);
                float cosTheta1 = (float) Math.cos(theta + dTheta);
                float sinTheta1 = (float) Math.sin(theta + dTheta);

                float x0 = r0 * cosTheta0;
                float z0 = r0 * sinTheta0;
                float x1 = r0 * cosTheta1;
                float z1 = r0 * sinTheta1;

                // Normals (same along each slant line)
                float nx0 = normalXZScale * cosTheta0;
                float nz0 = normalXZScale * sinTheta0;
                float nx1 = normalXZScale * cosTheta1;
                float nz1 = normalXZScale * sinTheta1;

                float s0 = (float) slice / numSlices;
                float s1 = (float) (slice + 1) / numSlices;

                if (isApex) {
                    // At apex: generate ONE triangle from base edge to apex point
                    normal(nx0, normalY, nz0);
                    texCoord(s0, t0);
                    position(x0, y0, z0);

                    // Apex point (average normals for smooth shading)
                    float len = Vec3.length((nx0 + nx1) * 0.5f, normalY, (nz0 + nz1) * 0.5f);
                    normal(((nx0 + nx1) * 0.5f)/len, normalY/len, ((nz0 + nz1) * 0.5f)/len);
                    texCoord((s0 + s1) * 0.5f, t1);
                    position(0.0f, y1, 0.0f);

                    normal(nx1, normalY, nz1);
                    texCoord(s1, t0);
                    position(x1, y0, z1);

                } else {
                    // Regular ring: generate quad (2 triangles)
                    float x2 = r1 * cosTheta1;
                    float z2 = r1 * sinTheta1;
                    float x3 = r1 * cosTheta0;
                    float z3 = r1 * sinTheta0;

                    // First triangle
                    normal(nx0, normalY, nz0);
                    texCoord(s0, t0);
                    position(x0, y0, z0);

                    normal(nx0, normalY, nz0);
                    texCoord(s0, t1);
                    position(x3, y1, z3);

                    normal(nx1, normalY, nz1);
                    texCoord(s1, t1);
                    position(x2, y1, z2);

                    // Second triangle
                    normal(nx0, normalY, nz0);
                    texCoord(s0, t0);
                    position(x0, y0, z0);

                    normal(nx1, normalY, nz1);
                    texCoord(s1, t1);
                    position(x2, y1, z2);

                    normal(nx1, normalY, nz1);
                    texCoord(s1, t0);
                    position(x1, y0, z1);
                }

                theta += dTheta;
            }
        }

        // Base cap (optional, facing down)
        if (radius > 0) {
            float theta = (float) Math.PI / 2.0f;
            normal(0.0f, -1.0f, 0.0f);  // Point down

            for (int slice = 0; slice < numSlices; slice++) {
                float cosTheta0 = (float) Math.cos(theta);
                float sinTheta0 = (float) Math.sin(theta);
                float cosTheta1 = (float) Math.cos(theta + dTheta);
                float sinTheta1 = (float) Math.sin(theta + dTheta);

                float x0 = radius * cosTheta0;
                float z0 = radius * sinTheta0;
                float x1 = radius * cosTheta1;
                float z1 = radius * sinTheta1;

                // Radial UV mapping for base
                float u0 = 0.5f + 0.5f * cosTheta0;
                float v0 = 0.5f + 0.5f * sinTheta0;
                float u1 = 0.5f + 0.5f * cosTheta1;
                float v1 = 0.5f + 0.5f * sinTheta1;

                // Triangle fan from center
                texCoord(0.5f, 0.5f);
                position(0.0f, bottom, 0.0f);

                texCoord(u0, v0);
                position(x0, bottom, z0);

                texCoord(u1, v1);
                position(x1, bottom, z1);

                theta += dTheta;
            }
        }

        endTriangles();
        return this;
    }

    @Override
    public MeshGen addPyramid(float width, float height, float depth) {

        final float left = -width/2.0f;
        final float right = -left;
        final float front = depth/2.0f;
        final float back = -front;
        final float bottom = -height/2.0f;
        final float top = -bottom;

        beginTriangles();
        color(this.currentColor.x, this.currentColor.y, this.currentColor.z);

        // base
        normal(0.0f, -1.0f, 0.0f);
        texCoord(0.0f, 0.0f);
        position(left, bottom, back);

        texCoord(1.0f, 0.0f);
        position(right, bottom, back);

        texCoord(1.0f, 1.0f);
        position(right, bottom, front);

        texCoord(0.0f, 0.0f);
        position(left, bottom, back);

        texCoord(1.0f, 1.0f);
        position(right, bottom, front);

        texCoord(0.0f, 1.0f);
        position(left, bottom, front);

        // front face

        final Vec3 edge1 = new Vec3( right - left, 0.0f, 0.0f).normalize();
        final Vec3 edge2 = new Vec3(width/2.0f, height, -depth/2.0f).normalize();
        final Vec3 faceNormal = edge1.cross(edge2).normalize();


        normal(faceNormal.x, faceNormal.y, faceNormal.z);
        texCoord(0.0f, 0.0f);
        position(left, bottom, front);

        texCoord(1.0f, 0.0f);
        position(right, bottom, front);

        texCoord(0.5f, 1.0f);
        position(0.0f, top, 0.0f);

        // back face
        normal(faceNormal.x, faceNormal.y, -faceNormal.z);
        texCoord(0.0f, 0.0f);
        position(right, bottom, back);

        texCoord(1.0f, 0.0f);
        position(left, bottom, back);

        texCoord(0.5f, 1.0f);
        position(0.0f, top, 0.0f);

        // right face
        edge1.set(0.0f, 0.0f, back - front).normalize();
        edge2.set(-width/2.0f, height, -depth/2.0f).normalize();
        faceNormal.set(edge1).cross(edge2).normalize();

        normal(faceNormal.x, faceNormal.y, faceNormal.z);
        texCoord(0.0f, 0.0f);
        position(right, bottom, front);

        texCoord(1.0f, 0.0f);
        position(right, bottom, back);

        texCoord(0.5f, 1.0f);
        position(0.0f, top, 0.0f);

        // left face
        normal(-faceNormal.x, faceNormal.y, faceNormal.z);
        texCoord(0.0f, 0.0f);
        position(left, bottom, back);

        texCoord(1.0f, 0.0f);
        position(left, bottom, front);

        texCoord(0.5f, 1.0f);
        position(0.0f, top, 0.0f);

        endTriangles();
        return this;
    }

    @Override
    public MeshGen addCapsule(float radius, float cylinderHeight, int numSlices, int hemisphereRings) {
        float totalHeight = cylinderHeight + 2.0f * radius;
        float cylinderMinT = radius / totalHeight;
        float cylinderMaxT = 1.0f - cylinderMinT;

        beginTriangles();
        color(currentColor.x, currentColor.y, currentColor.z);

        // bottom hemisphere
        generateHemisphere(radius, numSlices, hemisphereRings, (float) Math.PI/2.0f, (float) Math.PI, -(cylinderHeight/2.0f), cylinderMaxT, 1.0f);
        // cylinder shell
        generateCylinderShellTriangles(radius, cylinderHeight, numSlices, 1, cylinderMinT, cylinderMaxT);
        // top hemisphere
        generateHemisphere(radius, numSlices, hemisphereRings, 0.0f, (float) Math.PI/2.0f, (cylinderHeight/2.0f), 0, cylinderMinT);

        endTriangles();
        return this;
    }


    /// Helper method for hemisphere generation
    private void generateHemisphere(float radius, int numSlices, int numRings,
                                    float phiStart, float phiEnd,
                                    float yOffset,
                                    float tStart, float tEnd) {
        final float dTheta = (float)(2.0f * Math.PI / numSlices);
        final float dPhi = (phiEnd - phiStart) / numRings;
        final float dS = 1.0f / numSlices;
        final float dT = (tEnd - tStart) / numRings;

        for (int ring = 0; ring < numRings; ring++) {
            float theta = (float) Math.PI / 2.0f;  // Reset theta each ring
            float phi0 = phiStart + ring * dPhi;
            float phi1 = phiStart + (ring + 1) * dPhi;

            float sinPhi0 = (float)Math.sin(phi0);
            float cosPhi0 = (float)Math.cos(phi0);
            float sinPhi1 = (float)Math.sin(phi1);
            float cosPhi1 = (float)Math.cos(phi1);

            float t0 = tStart + ring * dT;
            float t1 = tStart + (ring + 1) * dT;

            for (int slice = 0; slice < numSlices; slice++) {
                float sinTheta0 = (float)Math.sin(theta);
                float cosTheta0 = (float)Math.cos(theta);
                float sinTheta1 = (float)Math.sin(theta + dTheta);
                float cosTheta1 = (float)Math.cos(theta + dTheta);

                // Calculate normals (same as positions but not scaled by radius)
                float nx0 = sinPhi0 * cosTheta0;
                //noinspection UnnecessaryLocalVariable
                float ny0 = cosPhi0;
                float nz0 = sinPhi0 * sinTheta0;

                float nx1 = sinPhi0 * cosTheta1;
                //noinspection UnnecessaryLocalVariable
                float ny1 = cosPhi0;
                float nz1 = sinPhi0 * sinTheta1;

                float nx2 = sinPhi1 * cosTheta1;
                //noinspection UnnecessaryLocalVariable
                float ny2 = cosPhi1;
                float nz2 = sinPhi1 * sinTheta1;

                float nx3 = sinPhi1 * cosTheta0;
                //noinspection UnnecessaryLocalVariable
                float ny3 = cosPhi1;
                float nz3 = sinPhi1 * sinTheta0;

                float s0 = (float)slice / (float) numSlices;
                float s1 = (float)(slice + 1) / (float)numSlices;

                // First triangle
                normal(nx0, ny0, nz0);
                texCoord(s0, 1.0f - t0);  // Flip vertically
                position(radius * nx0, yOffset + radius * ny0, radius * nz0);

                normal(nx1, ny1, nz1);
                texCoord(s1, 1.0f - t0);
                position(radius * nx1, yOffset + radius * ny1, radius * nz1);

                normal(nx2, ny2, nz2);
                texCoord(s1, 1.0f - t1);
                position(radius * nx2, yOffset + radius * ny2, radius * nz2);

                // Second triangle
                normal(nx0, ny0, nz0);
                texCoord(s0, 1.0f - t0);
                position(radius * nx0, yOffset + radius * ny0, radius * nz0);

                normal(nx2, ny2, nz2);
                texCoord(s1, 1.0f - t1);
                position(radius * nx2, yOffset + radius * ny2, radius * nz2);

                normal(nx3, ny3, nz3);
                texCoord(s0, 1.0f - t1);
                position(radius * nx3, yOffset + radius * ny3, radius * nz3);

                theta += dTheta;
            }
        }
    }

    @Override
    public IndexedMesh build() {
        final float[] finalVertexData = Arrays.copyOf(vertexData, vertexCount * vertexFormat.stride());
        final int[] finalIndices = Arrays.copyOf(indices, indexCount);
        reset();
        return new IndexedMesh(this.vertexFormat, IndexedMesh.PrimitiveType.TRIANGLES, finalVertexData, finalIndices);
    }

    private void reset() {
        currentColor.set(1.0f, 1.0f, 1.0f);
        currentNormal.set(0.0f, 0.0f, 1.0f);
        currentTexCoord.set(0.0f, 0.0f);
        vertexCount = 0;
        indexCount = 0;

        activeTriangleBatchVertexStart = 0;
        activeTriangleBatchVertexCount = 0;
        activeTriangleBatchIndexStart = 0;
        activeTriangleBatchIndexCount = 0;

        activeColor = null;
        activeNormal = null;
        activeTexCoord = null;

        vertexPositionLookup.clear();
    }
}
