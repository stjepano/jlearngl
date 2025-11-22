package dev.stjepano.math;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Objects;

/// A 9 element 3x3 matrix.
///
/// **WARNING**: this matrix is row-major oriented when converted to float[] or written to MemorySegment. For
/// OpenGL compatibility you should transpose it first or use OpenGL functions that do transpose themselves.
public final class Mat3 {
    public float m00, m01, m02;
    public float m10, m11, m12;
    public float m20, m21, m22;

    /// Constructor, identity matrix.
    public Mat3() {
        setIdentity();
    }

    /// Element constructor, use with care.
    public Mat3(
            float m00, float m01, float m02,
            float m10, float m11, float m12,
            float m20, float m21, float m22
    ) {
        this.m00 = m00; this.m01 = m01; this.m02 = m02;
        this.m10 = m10; this.m11 = m11; this.m12 = m12;
        this.m20 = m20; this.m21 = m21; this.m22 = m22;
    }

    /// Construct matrix from columns.
    public Mat3(Vec3 col0, Vec3 col1, Vec3 col2) {
        setColumns(col0, col1, col2);
    }

    /// Construct matrix from other matrix.
    public Mat3(Mat3 other) {
        set(other);
    }

    /// Construct matrix from quaternion rotation.
    public Mat3(Quaternion q) {
        setRotation(q);
    }

    /// Copy. Allocates new object.
    public Mat3 copy() {
        return new Mat3(m00, m01, m02, m10, m11, m12, m20, m21, m22);
    }

    /// No-alloc copy.
    public void copy(Mat3 dest) {
        dest.set(this);
    }

    /// Set to identity.
    public Mat3 setIdentity() {
        m00 = 1.0f; m01 = 0.0f; m02 = 0.0f;
        m10 = 0.0f; m11 = 1.0f; m12 = 0.0f;
        m20 = 0.0f; m21 = 0.0f; m22 = 1.0f;
        return this;
    }

    /// Set to have same values as other matrix.
    public Mat3 set(Mat3 other) {
        this.m00 = other.m00; this.m01 = other.m01; this.m02 = other.m02;
        this.m10 = other.m10; this.m11 = other.m11; this.m12 = other.m12;
        this.m20 = other.m20; this.m21 = other.m21; this.m22 = other.m22;
        return this;
    }

    /// Set to have the specified columns.
    public Mat3 setColumns(Vec3 col0, Vec3 col1, Vec3 col2) {
        m00 = col0.x;
        m10 = col0.y;
        m20 = col0.z;

        m01 = col1.x;
        m11 = col1.y;
        m21 = col1.z;

        m02 = col2.x;
        m12 = col2.y;
        m22 = col2.z;

        return this;
    }

    /// Set column at index to have same value as vector.
    public Mat3 setColumn(int index, Vec3 col) {
        return setColumn(index, col.x, col.y, col.z);
    }

    /// Set column at index to have specified values.
    public Mat3 setColumn(int index, float x, float y, float z) {
        if (index == 0) {
            m00 = x;
            m10 = y;
            m20 = z;
        } else if (index == 1) {
            m01 = x;
            m11 = y;
            m21 = z;
        } else if (index == 2) {
            m02 = x;
            m12 = y;
            m22 = z;
        } else {
            throw new IndexOutOfBoundsException();
        }
        return this;
    }

    /// Set row at index to the values of given row vector.
    public Mat3 setRow(int index, Vec3 row) {
        return setRow(index, row.x, row.y, row.z);
    }

    /// Set row at index to the specified values.
    public Mat3 setRow(int index, float x, float y, float z) {
        if (index == 0) {
            m00 = x;
            m01 = y;
            m02 = z;
        } else if (index == 1) {
            m10 = x;
            m11 = y;
            m12 = z;
        } else if (index == 2) {
            m20 = x;
            m21 = y;
            m22 = z;
        } else {
            throw new IndexOutOfBoundsException();
        }
        return this;
    }

    /// Set this matrix to be rotation matrix.
    public Mat3 setRotation(Quaternion q) {
        m00 = 1 - 2*(q.y*q.y + q.z*q.z);
        m01 = 2*(q.x*q.y - q.w*q.z);
        m02 = 2*(q.w*q.y + q.x*q.z);

        m10 = 2*(q.x*q.y + q.w*q.z);
        m11 = 1 - 2*(q.x*q.x + q.z*q.z);
        m12 = 2*(-q.w*q.x + q.y*q.z);

        m20 = 2*(-q.w*q.y + q.x*q.z);
        m21 = 2*(q.w*q.x + q.y*q.z);
        m22 = 1 - 2*(q.x*q.x + q.y*q.y);
        return this;
    }

    /// Multiply all elements of matrix with given scalar.
    public Mat3 scale(float s) {
        m00 *= s; m01 *= s; m02 *= s;
        m10 *= s; m11 *= s; m12 *= s;
        m20 *= s; m21 *= s; m22 *= s;
        return this;
    }

    /// Add: this += other
    public Mat3 add(Mat3 other) {
        m00 += other.m00; m01 += other.m01; m02 += other.m02;
        m10 += other.m10; m11 += other.m11; m12 += other.m12;
        m20 += other.m20; m21 += other.m21; m22 += other.m22;
        return this;
    }

    /// Sub: this -= other
    public Mat3 sub(Mat3 other) {
        m00 -= other.m00; m01 -= other.m01; m02 -= other.m02;
        m10 -= other.m10; m11 -= other.m11; m12 -= other.m12;
        m20 -= other.m20; m21 -= other.m21; m22 -= other.m22;
        return this;
    }

    /// Mul: this *= other
    public Mat3 mul(Mat3 other) {
        float t00 = m00, t01 = m01, t02 = m02;
        float t10 = m10, t11 = m11, t12 = m12;
        float t20 = m20, t21 = m21, t22 = m22;

        m00 = t00 * other.m00 + t01 * other.m10 + t02 * other.m20;
        m01 = t00 * other.m01 + t01 * other.m11 + t02 * other.m21;
        m02 = t00 * other.m02 + t01 * other.m12 + t02 * other.m22;

        m10 = t10 * other.m00 + t11 * other.m10 + t12 * other.m20;
        m11 = t10 * other.m01 + t11 * other.m11 + t12 * other.m21;
        m12 = t10 * other.m02 + t11 * other.m12 + t12 * other.m22;

        m20 = t20 * other.m00 + t21 * other.m10 + t22 * other.m20;
        m21 = t20 * other.m01 + t21 * other.m11 + t22 * other.m21;
        m22 = t20 * other.m02 + t21 * other.m12 + t22 * other.m22;

        return this;
    }

    /// Transpose the matrix.
    public Mat3 transpose() {
        float t00 = m00, t01 = m01, t02 = m02;
        float t10 = m10, t11 = m11, t12 = m12;
        float t20 = m20, t21 = m21, t22 = m22;
        setColumn(0, t00, t01, t02);
        setColumn(1, t10, t11, t12);
        setColumn(2, t20, t21, t22);
        return this;
    }

    /// Calculate determinant of the matrix.
    public float determinant() {
        return -m02*m11*m20 + m01*m12*m20 + m02*m10*m21 - m00*m12*m21 - m01*m10*m22 +
                m00*m11*m22;
    }

    /// Invert the matrix.
    public Mat3 invert() {
        float det = determinant();
        if (det < 1e-8f) {
            return this;
        }
        float oneOverDet = 1.0f/det;

        float t00 = m00, t01 = m01, t02 = m02;
        float t10 = m10, t11 = m11, t12 = m12;
        float t20 = m20, t21 = m21, t22 = m22;

        m00 = -t12*t21 + t11*t22;
        m01 = t02*t21 - t01*t22;
        m02 = -t02*t11 + t01*t12;

        m10 = t12*t20 - t10*t22;
        m11 = -t02*t20 + t00*t22;
        m12 = t02*t10 - t00*t12;

        m20 = -t11*t20 + t10*t21;
        m21 = t01*t20 - t00*t21;
        m22 = -t01*t10 + t00*t11;

        m00 *= oneOverDet; m01 *= oneOverDet; m02 *= oneOverDet;
        m10 *= oneOverDet; m11 *= oneOverDet; m12 *= oneOverDet;
        m20 *= oneOverDet; m21 *= oneOverDet; m22 *= oneOverDet;

        return this;
    }

    /// Copy to float array. First component at dest + offset.
    ///
    /// **Row major order**
    public void toFloatArray(float[] dest, int offset) {
        dest[offset] = m00;
        dest[offset + 1] = m01;
        dest[offset + 2] = m02;

        dest[offset + 3] = m10;
        dest[offset + 4] = m11;
        dest[offset + 5] = m12;

        dest[offset + 6] = m20;
        dest[offset + 7] = m21;
        dest[offset + 8] = m22;
    }

    /// Copy to float array.
    ///
    /// **Row major order**
    public void toFloatArray(float[] dest) {
        toFloatArray(dest, 0);
    }

    /// Initialize Mat4 from float array, first component at src + offset.
    ///
    /// **Row major order**
    public static void fromFloatArray(float[] src, int offset, Mat3 dest) {
        dest.m00 = src[offset];
        dest.m01 = src[offset + 1];
        dest.m02 = src[offset + 2];

        dest.m10 = src[offset + 3];
        dest.m11 = src[offset + 4];
        dest.m12 = src[offset + 5];

        dest.m20 = src[offset + 6];
        dest.m21 = src[offset + 7];
        dest.m22 = src[offset + 8];
    }

    /// Initialize Mat4 from memory segment, first component at src + byteOffset.
    ///
    /// **Row major order**
    public static void fromMemorySegment(MemorySegment src, long byteOffset, Mat3 dest) {
        long stride = ValueLayout.JAVA_FLOAT.byteSize();
        dest.m00 = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride * 0);
        dest.m01 = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride * 1);
        dest.m02 = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride * 2);

        dest.m10 = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride * 3);
        dest.m11 = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride * 4);
        dest.m12 = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride * 5);

        dest.m20 = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride * 6);
        dest.m21 = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride * 7);
        dest.m22 = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride * 8);
    }

    /// Copy to memory segment, first element at dest + byteOffset.
    ///
    /// **Row major order**
    public void toMemorySegment(MemorySegment dest, long byteOffset) {
        long stride = ValueLayout.JAVA_FLOAT.byteSize();
        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride * 0, m00);
        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride * 1, m01);
        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride * 2, m02);

        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride * 3, m10);
        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride * 4, m11);
        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride * 5, m12);

        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride * 6, m20);
        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride * 7, m21);
        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride * 8, m22);
    }

    @Override
    public String toString() {
        return String.format("Mat4({%f, %f, %f}, {%f, %f, %f}, {%f, %f, %f})",
                m00, m01, m02,
                m10, m11, m12,
                m20, m21, m22);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Mat3 mat3 = (Mat3) o;
        return Float.compare(m00, mat3.m00) == 0 && Float.compare(m01, mat3.m01) == 0
                && Float.compare(m02, mat3.m02) == 0 && Float.compare(m10, mat3.m10) == 0
                && Float.compare(m11, mat3.m11) == 0 && Float.compare(m12, mat3.m12) == 0
                && Float.compare(m20, mat3.m20) == 0 && Float.compare(m21, mat3.m21) == 0
                && Float.compare(m22, mat3.m22) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m00, m01, m02, m10, m11, m12, m20, m21, m22);
    }
}
