package dev.stjepano.math;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Objects;

public final class Projection {
    private final Mat4 matrix = new Mat4();


    /// Call this if you do not want any projection transformation applied.
    public void identity() {
        this.matrix.setIdentity();
    }

    /// Set projection to orthographic projection.
    /// @param left coordinate of left clipping plane
    /// @param right coordinate of right clipping plane
    /// @param bottom coordinate of bottom clipping plane
    /// @param top coordinate of top clipping plane
    /// @param dNear distance to near clipping plane
    /// @param dFar distance to far clipping plane
    public void ortho(float left, float right, float bottom, float top, float dNear, float dFar) {
        float tx = -(right + left) / (right - left);
        float ty = -(top + bottom) / (top - bottom);
        float tz = -(dFar + dNear) / (dFar - dNear);

        this.matrix.setIdentity()
                .setColumn(0, 2.0f/(right - left), 0, 0, 0)
                .setColumn(1, 0, 2.0f/(top - bottom), 0, 0)
                .setColumn(2, 0, 0, -2.0f/(dFar - dNear), 0)
                .setColumn(3, tx, ty, tz, 1);
    }

    /// Orthographic projection
    /// @param width the width of clipping space
    /// @param aspect the aspect ration of window as width/height
    /// @param dNear distance to near clipping plane
    /// @param dFar distance to far clipping plane
    public void ortho(float width, float aspect, float dNear, float dFar) {
        float left = -width/2.0f;
        float right = -left;
        float bottom = left/aspect;
        float top = -bottom;
        ortho(left, right, bottom, top, dNear, dFar);
    }

    /// Set perspective projection.
    /// @param left coordinate of left clipping plane
    /// @param right coordinate of right clipping plane
    /// @param bottom coordinate of bottom clipping plane
    /// @param top coordinate of top clipping plane
    /// @param dNear distance to near clipping plane
    /// @param dFar distance to far clipping plane
    public void frustum(float left, float right, float bottom, float top, float dNear, float dFar) {
        float a = (right + left) / (right - left);
        float b = (top + bottom) / (top - bottom);
        float c = -(dFar + dNear) / (dFar - dNear);
        float d = - (2.0f * dFar * dNear) / (dFar - dNear);

        this.matrix.setIdentity()
                .setColumn(0, (2.0f * dNear) / (right - left), 0, 0, 0)
                .setColumn(1, 0, (2.0f * dNear) / (top - bottom), 0, 0)
                .setColumn(2, a, b, c, -1)
                .setColumn(3, 0, 0, d, 0);
    }

    /// Set the perspective matrix.
    /// @param fovYRad vertical field of view
    /// @param aspect aspect ratio (width/height)
    /// @param dNear distance to near clipping plane
    /// @param dFar distance to far clipping plane
    public void perspective(float fovYRad, float aspect, float dNear, float dFar) {
        float tanHalfFovY = (float)Math.tan(fovYRad / 2.0f);
        float top = dNear * tanHalfFovY;
        float bottom = -top;
        float right = top * aspect;
        float left = -right;
        frustum(left, right, bottom, top, dNear, dFar);
    }

    /// Access the matrix ...
    public Mat4 matrix() {
        return this.matrix;
    }

    /// Copy to float array. First component at dest + offset.
    ///
    /// **Row major order**
    public void toFloatArray(float[] dest, int offset) {
        this.matrix.toFloatArray(dest, offset);
    }

    /// Copy to float array.
    ///
    /// **Row major order**
    public void toFloatArray(float[] dest) {
        toFloatArray(dest, 0);
    }

    /// Initialize Projection from float array, first component at src + offset.
    ///
    /// **Row major order**
    public static void fromFloatArray(float[] src, int offset, Projection dest) {
        Mat4.fromFloatArray(src, offset, dest.matrix);
    }

    /// Initialize Projection from memory segment, first component at src + byteOffset.
    ///
    /// **Row major order**
    public static void fromMemorySegment(MemorySegment src, long byteOffset, Projection dest) {
        Mat4.fromMemorySegment(src, byteOffset, dest.matrix);
    }

    /// Copy to memory segment, first element at dest + byteOffset.
    ///
    /// **Row major order**
    public void toMemorySegment(MemorySegment dest, long byteOffset) {
        this.matrix.toMemorySegment(dest, byteOffset);
    }

    @Override
    public String toString() {
        return "Projection(" + this.matrix + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Projection that = (Projection) o;
        return Objects.equals(matrix, that.matrix);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(matrix);
    }
}
