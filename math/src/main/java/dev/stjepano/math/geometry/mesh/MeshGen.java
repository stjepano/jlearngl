package dev.stjepano.math.geometry.mesh;

import dev.stjepano.math.Transform;

/// `MeshGen` allows you to generate meshes. All meshes can be composed of multiple "sub-meshes" where each
/// sub-mesh is generated at origin.
///
/// Example - a cube with sphere above it and touching the cube's top face:
/// ```java
/// MeshGen meshGen = new ...;
/// meshGen.origin().setPosition(0, 0, 0);
/// meshGen.addCube(1, 1, 1);
/// meshGen.origin().setPosition(0, 1, 0);
/// meshGen.addSphere(1, 8, 8);
/// IndexedMesh mesh = meshGen.build();
/// ```
///
/// #### About vertex data format
/// Vertex data format is always in this order
/// ```
/// position: 3 * float
/// [normal: 3 * float]
/// [color: 3 * float]
/// [texCoord: 2 * float]
/// ```
///
/// So if you created an instance of `MeshGen` that generates POSITION (always) and TEX_COORD attributes the vertex data
/// will look like this: `[x0, y0, z0, u0, v0, x1, y1, z1, u1, v1, ...]`
public interface MeshGen {

    /// Allows access to `origin`. All "sub-meshes" are build around origin. You can modify where meshes get
    /// placed using this.
    Transform origin();

    /// Construct an isosceles or, depending on width and height, equilateral triangle.
    ///
    /// Triangle is in local X-Y plane, facing local +Z.
    /// @param width the width of triangle base
    /// @param height distance between base and top of the triangle
    /// @return reference to this
    MeshGen addTriangle(float width, float height);

    /// Construct a rectangle in X-Y plane, facing local +Z.
    /// @param width the width of the rectangle
    /// @param height the height of the rectangle
    MeshGen addRect(float width, float height);

    /// Construct a grid in X-Y plane, facing local +Z.
    /// @param width the width of the grid
    /// @param height the height of the grid
    /// @param widthDivisions number of divisions along width
    /// @param heightDivisions number of divisions along height
    MeshGen addGrid(float width, float height, int widthDivisions, int heightDivisions);

    /// Construct a cube. Cube is aligned with current transform, front face in transforms +Z around transform point.
    /// @param width the width of cube (on X axis)
    /// @param height the height of cube (on Y axis)
    /// @param depth the depth of cube (on Z axis)
    /// @return reference to this
    MeshGen addCube(float width, float height, float depth);

    /// Construct a cylinder. Cylinder is constructed around Y axis.
    /// @param radius radius of cylinder shell
    /// @param height the height of cylinder
    /// @param numSlices number of slices of shell
    /// @param numRings number of rings along height
    /// @param caps should we generate caps
    /// @return reference to this
    MeshGen addCylinder(float radius, float height, int numSlices, int numRings, boolean caps);

    /// Construct a sphere around current transform.
    /// @param radius the radius of sphere
    /// @param numSlices number of slices around Y axis
    /// @param numRings number of rings along Y axis
    /// @return reference to this
    MeshGen addSphere(float radius, int numSlices, int numRings);

    /// Construct a cone around current transform.
    /// @param radius radius of cone base
    /// @param height height of cone
    /// @param numSlices number of slices around Y axis
    /// @param numRings number of rings along Y axis
    /// @return reference to this
    MeshGen addCone(float radius, float height, int numSlices, int numRings);

    /// Construct a pyramid around current transform.
    /// @param width the width of the pyramid base
    /// @param height the height of the pyramid
    /// @param depth the depth of the pyramid base
    /// @return reference to this
    MeshGen addPyramid(float width, float height, float depth);

    /// Build the indexed mesh.
    ///
    /// **NOTE:** also resets the `MeshGen`.
    IndexedMesh build();
}
