package dev.stjepano.math.geometry.mesh;

import java.util.Arrays;

/// Simplest possible representation of an indexed mesh (it could be triangle mesh or line mesh).
/// The mesh is in format which is suitable for use with graphics APIs.
/// @param vertexFormat the vertex format description
/// @param primitiveType tells us if this is line or triangle mesh
/// @param vertexData contains vertex data, take care, vertexData.length is not number of vertices
/// @param indices each pair or triplet indexes into vertexData and represents one line or triangle
public record IndexedMesh(VertexFormat vertexFormat, PrimitiveType primitiveType, float[] vertexData, int[] indices) {

    public IndexedMesh {
        if (vertexFormat == null) {
            throw new NullPointerException("vertexFormat");
        }
        if (primitiveType == null) {
            throw new NullPointerException("primitiveType");
        }
        if (vertexData == null || vertexData.length == 0) {
            throw new IllegalArgumentException("Null or empty vertexData.");
        }
        if (indices == null || indices.length == 0) {
            throw new IllegalArgumentException("Null or empty indices.");
        }
    }


    /// Return the number of vertices (calculated as vertexData.length / stride).
    public int vertexCount() {
        int stride = this.vertexFormat.stride();
        if (stride == 0) {
            throw new IllegalStateException("stride is 0");
        }
        return this.vertexData.length / stride;
    }

    public record VertexFormat(AttributeType[] attributeTypes) {

        public VertexFormat {
            if (attributeTypes == null) {
                throw new NullPointerException("attributeTypes");
            }

            // Check for duplicate attributes which would be illegal
            final int[] frequencies = new int[AttributeType.values().length];
            for (var attributeType : attributeTypes) {
                frequencies[attributeType.ordinal()] += 1;
                if (frequencies[attributeType.ordinal()] > 1) {
                    throw new IllegalArgumentException("Duplicate attribute " + attributeType);
                }
            }
            // Check if position is specified, no position we can't really have vertex
            if (frequencies[AttributeType.POSITION.ordinal()] == 0) {
                throw new IllegalArgumentException("No POSITION attribute!");
            }
        }

        /// Return true if `VertexFormat` has a normal.
        public boolean hasNormal() {
            for (AttributeType at : attributeTypes) {
                if (at == AttributeType.NORMAL) {
                    return true;
                }
            }
            return false;
        }

        /// Return true if `VertexFormat` has a color.
        public boolean hasColor() {
            for (AttributeType at : attributeTypes) {
                if (at == AttributeType.COLOR) {
                    return true;
                }
            }
            return false;
        }

        /// Return true if `VertexFormat` has a texture coordinate.
        public boolean hasTextureCoordinate() {
            for (AttributeType at : attributeTypes) {
                if (at == AttributeType.TEX_COORD) {
                    return true;
                }
            }
            return false;
        }

        /// An offset in vertex data to first component of normal. -1 if no normal.
        public int normalOffset() {
            if (!hasNormal()) {
                return -1;
            }
            return 3;
        }

        /// An offset in vertex data to the first component of color. -1 if no color.
        public int colorOffset() {
            if (!hasColor()) {
                return -1;
            }
            int result = 3;
            if (hasNormal()) {
                result += 3;
            }
            return result;
        }

        /// An offset in vertex data to the first component of texture coordinate. -1 if no texture coordinate.
        public int textureCoordinateOffset() {
            if (!hasTextureCoordinate()) {
                return -1;
            }
            int result = 3;
            if (hasNormal()) {
                result += 3;
            }
            if (hasColor()) {
                result += 3;
            }
            return result;
        }

        public enum AttributeType {

            POSITION(3),
            NORMAL(3),
            COLOR(3),
            TEX_COORD(2);

            private final int numComponents;

            AttributeType(int numComponents) {
                this.numComponents = numComponents;
            }

            public int numComponents() {
                return this.numComponents;
            }
        }

        /// Calculates vertex stride (in number of components) based on vertex format of triangle mesh.
        public int stride() {
            int result = 0;
            for (VertexFormat.AttributeType attributeType : attributeTypes) {
                result += attributeType.numComponents;
            }
            return result;
        }
    }

    public enum PrimitiveType {
        LINES,
        TRIANGLES
    }
}
