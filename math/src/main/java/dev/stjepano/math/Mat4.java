package dev.stjepano.math;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Objects;

/// A 16 element 4x4 matrix.
///
/// **WARNING**: this matrix is row-major oriented when converted to float[] or written to MemorySegment. For
/// OpenGL compatibility you should transpose it first or use OpenGL functions that do transpose themselves.
public final class Mat4 {
    public float m00, m01, m02, m03;
    public float m10, m11, m12, m13;
    public float m20, m21, m22, m23;
    public float m30, m31, m32, m33;

    /// Constructor, identity matrix.
    public Mat4() {
        setIdentity();
    }

    /// Element constructor, use with care.
    public Mat4(
            float m00, float m01, float m02, float m03,
            float m10, float m11, float m12, float m13,
            float m20, float m21, float m22, float m23,
            float m30, float m31, float m32, float m33
    ) {
        this.m00 = m00; this.m01 = m01; this.m02 = m02; this.m03 = m03;
        this.m10 = m10; this.m11 = m11; this.m12 = m12; this.m13 = m13;
        this.m20 = m20; this.m21 = m21; this.m22 = m22; this.m23 = m23;
        this.m30 = m30; this.m31 = m31; this.m32 = m32; this.m33 = m33;
    }

    /// Construct matrix from columns.
    public Mat4(Vec4 col0, Vec4 col1, Vec4 col2, Vec4 col3) {
        setColumns(col0, col1, col2, col3);
    }

    /// Construct matrix from other matrix.
    public Mat4(Mat4 other) {
        set(other);
    }

    /// Copy. Allocates new object.
    public Mat4 copy() {
        return new Mat4(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33);
    }

    /// No-alloc copy.
    public void copy(Mat4 dest) {
        dest.set(this);
    }

    /// Set to identity.
    public Mat4 setIdentity() {
        m00 = 1.0f; m01 = 0.0f; m02 = 0.0f; m03 = 0.0f;
        m10 = 0.0f; m11 = 1.0f; m12 = 0.0f; m13 = 0.0f;
        m20 = 0.0f; m21 = 0.0f; m22 = 1.0f; m23 = 0.0f;
        m30 = 0.0f; m31 = 0.0f; m32 = 0.0f; m33 = 1.0f;
        return this;
    }

    /// Set to have same values as other matrix.
    public Mat4 set(Mat4 other) {
        this.m00 = other.m00; this.m01 = other.m01; this.m02 = other.m02; this.m03 = other.m03;
        this.m10 = other.m10; this.m11 = other.m11; this.m12 = other.m12; this.m13 = other.m13;
        this.m20 = other.m20; this.m21 = other.m21; this.m22 = other.m22; this.m23 = other.m23;
        this.m30 = other.m30; this.m31 = other.m31; this.m32 = other.m32; this.m33 = other.m33;
        return this;
    }

    /// Set to have the specified columns.
    public Mat4 setColumns(Vec4 col0, Vec4 col1, Vec4 col2, Vec4 col3) {
        m00 = col0.x;
        m10 = col0.y;
        m20 = col0.z;
        m30 = col0.w;

        m01 = col1.x;
        m11 = col1.y;
        m21 = col1.z;
        m31 = col1.w;

        m02 = col2.x;
        m12 = col2.y;
        m22 = col2.z;
        m32 = col2.w;

        m03 = col3.x;
        m13 = col3.y;
        m23 = col3.z;
        m33 = col3.w;
        return this;
    }

    /// Set column at index to have same value as vector.
    public Mat4 setColumn(int index, Vec4 col) {
        return setColumn(index, col.x, col.y, col.z, col.w);
    }

    /// Set column at index to have specified values.
    public Mat4 setColumn(int index, float x, float y, float z, float w) {
        if (index == 0) {
            m00 = x;
            m10 = y;
            m20 = z;
            m30 = w;
        } else if (index == 1) {
            m01 = x;
            m11 = y;
            m21 = z;
            m31 = w;
        } else if (index == 2) {
            m02 = x;
            m12 = y;
            m22 = z;
            m32 = w;
        } else if (index == 3) {
            m03 = x;
            m13 = y;
            m23 = z;
            m33 = w;
        } else {
            throw new IndexOutOfBoundsException();
        }
        return this;
    }

    /// Set row at index to the values of given row vector.
    public Mat4 setRow(int index, Vec4 row) {
        return setRow(index, row.x, row.y, row.z, row.w);
    }

    /// Set row at index to the specified values.
    public Mat4 setRow(int index, float x, float y, float z, float w) {
        if (index == 0) {
            m00 = x;
            m01 = y;
            m02 = z;
            m03 = w;
        } else if (index == 1) {
            m10 = x;
            m11 = y;
            m12 = z;
            m13 = w;
        } else if (index == 2) {
            m20 = x;
            m21 = y;
            m22 = z;
            m23 = w;
        } else if (index == 3) {
            m30 = x;
            m31 = y;
            m32 = z;
            m33 = w;
        } else {
            throw new IndexOutOfBoundsException();
        }
        return this;
    }

    /// Multiply all elements of matrix with given scalar.
    public Mat4 scale(float s) {
        m00 *= s; m01 *= s; m02 *= s; m03 *= s;
        m10 *= s; m11 *= s; m12 *= s; m13 *= s;
        m20 *= s; m21 *= s; m22 *= s; m23 *= s;
        m30 *= s; m31 *= s; m32 *= s; m33 *= s;
        return this;
    }

    /// Add: this += other
    public Mat4 add(Mat4 other) {
        m00 += other.m00; m01 += other.m01; m02 += other.m02; m03 += other.m03;
        m10 += other.m10; m11 += other.m11; m12 += other.m12; m13 += other.m13;
        m20 += other.m20; m21 += other.m21; m22 += other.m22; m23 += other.m23;
        m30 += other.m30; m31 += other.m31; m32 += other.m32; m33 += other.m33;
        return this;
    }

    /// Sub: this -= other
    public Mat4 sub(Mat4 other) {
        m00 -= other.m00; m01 -= other.m01; m02 -= other.m02; m03 -= other.m03;
        m10 -= other.m10; m11 -= other.m11; m12 -= other.m12; m13 -= other.m13;
        m20 -= other.m20; m21 -= other.m21; m22 -= other.m22; m23 -= other.m23;
        m30 -= other.m30; m31 -= other.m31; m32 -= other.m32; m33 -= other.m33;
        return this;
    }

    /// Mul: this *= other
    public Mat4 mul(Mat4 other) {
        float t00 = m00, t01 = m01, t02 = m02, t03 = m03;
        float t10 = m10, t11 = m11, t12 = m12, t13 = m13;
        float t20 = m20, t21 = m21, t22 = m22, t23 = m23;
        float t30 = m30, t31 = m31, t32 = m32, t33 = m33;

        m00 = t00 * other.m00 + t01 * other.m10 + t02 * other.m20 + t03 * other.m30;
        m01 = t00 * other.m01 + t01 * other.m11 + t02 * other.m21 + t03 * other.m31;
        m02 = t00 * other.m02 + t01 * other.m12 + t02 * other.m22 + t03 * other.m32;
        m03 = t00 * other.m03 + t01 * other.m13 + t02 * other.m23 + t03 * other.m33;

        m10 = t10 * other.m00 + t11 * other.m10 + t12 * other.m20 + t13 * other.m30;
        m11 = t10 * other.m01 + t11 * other.m11 + t12 * other.m21 + t13 * other.m31;
        m12 = t10 * other.m02 + t11 * other.m12 + t12 * other.m22 + t13 * other.m32;
        m13 = t10 * other.m03 + t11 * other.m13 + t12 * other.m23 + t13 * other.m33;

        m20 = t20 * other.m00 + t21 * other.m10 + t22 * other.m20 + t23 * other.m30;
        m21 = t20 * other.m01 + t21 * other.m11 + t22 * other.m21 + t23 * other.m31;
        m22 = t20 * other.m02 + t21 * other.m12 + t22 * other.m22 + t23 * other.m32;
        m23 = t20 * other.m03 + t21 * other.m13 + t22 * other.m23 + t23 * other.m33;

        m30 = t30 * other.m00 + t31 * other.m10 + t32 * other.m20 + t33 * other.m30;
        m31 = t30 * other.m01 + t31 * other.m11 + t32 * other.m21 + t33 * other.m31;
        m32 = t30 * other.m02 + t31 * other.m12 + t32 * other.m22 + t33 * other.m32;
        m33 = t30 * other.m03 + t31 * other.m13 + t32 * other.m23 + t33 * other.m33;

        return this;
    }

    /// Transpose the matrix.
    public Mat4 transpose() {
        float t00 = m00, t01 = m01, t02 = m02, t03 = m03;
        float t10 = m10, t11 = m11, t12 = m12, t13 = m13;
        float t20 = m20, t21 = m21, t22 = m22, t23 = m23;
        float t30 = m30, t31 = m31, t32 = m32, t33 = m33;
        setColumn(0, t00, t01, t02, t03);
        setColumn(1, t10, t11, t12, t13);
        setColumn(2, t20, t21, t22, t23);
        setColumn(3, t30, t31, t32, t33);
        return this;
    }

    /// Calculate determinant of the matrix.
    public float determinant() {
        return m03*m12*m21*m30 - m02*m13*m21*m30 - m03*m11*m22*m30 + m01*m13*m22*m30 +
                m02*m11*m23*m30 - m01*m12*m23*m30 - m03*m12*m20*m31 +
                m02*m13*m20*m31 + m03*m10*m22*m31 - m00*m13*m22*m31 -
                m02*m10*m23*m31 + m00*m12*m23*m31 + m03*m11*m20*m32 -
                m01*m13*m20*m32 - m03*m10*m21*m32 + m00*m13*m21*m32 +
                m01*m10*m23*m32 - m00*m11*m23*m32 - m02*m11*m20*m33 +
                m01*m12*m20*m33 + m02*m10*m21*m33 - m00*m12*m21*m33 -
                m01*m10*m22*m33 + m00*m11*m22*m33;
    }

    /// Invert the matrix.
    public Mat4 invert() {
        float det = determinant();
        if (det < 1e-8f) {
            return this;
        }
        float oneOverDet = 1.0f/det;

        float t00 = m00, t01 = m01, t02 = m02, t03 = m03;
        float t10 = m10, t11 = m11, t12 = m12, t13 = m13;
        float t20 = m20, t21 = m21, t22 = m22, t23 = m23;
        float t30 = m30, t31 = m31, t32 = m32, t33 = m33;

        m00 = -t13*t22*t31 + t12*t23*t31 + t13*t21*t32 - t11*t23*t32 - t12*t21*t33 + t11*t22*t33;
        m01 = t03*t22*t31 - t02*t23*t31 - t03*t21*t32 + t01*t23*t32 + t02*t21*t33 - t01*t22*t33;
        m02 = -t03*t12*t31 + t02*t13*t31 + t03*t11*t32 - t01*t13*t32 - t02*t11*t33 + t01*t12*t33;
        m03 = t03*t12*t21 - t02*t13*t21 - t03*t11*t22 + t01*t13*t22 + t02*t11*t23 - t01*t12*t23;

        m10 = t13*t22*t30 - t12*t23*t30 - t13*t20*t32 + t10*t23*t32 + t12*t20*t33 - t10*t22*t33;
        m11 = -t03*t22*t30 + t02*t23*t30 + t03*t20*t32 - t00*t23*t32 - t02*t20*t33 + t00*t22*t33;
        m12 = t03*t12*t30 - t02*t13*t30 - t03*t10*t32 + t00*t13*t32 + t02*t10*t33 - t00*t12*t33;
        m13 = -t03*t12*t20 + t02*t13*t20 + t03*t10*t22 - t00*t13*t22 - t02*t10*t23 + t00*t12*t23;

        m20 = -t13*t21*t30 + t11*t23*t30 + t13*t20*t31 - t10*t23*t31 - t11*t20*t33 + t10*t21*t33;
        m21 = t03*t21*t30 - t01*t23*t30 - t03*t20*t31 + t00*t23*t31 + t01*t20*t33 - t00*t21*t33;
        m22 = -t03*t11*t30 + t01*t13*t30 + t03*t10*t31 - t00*t13*t31 - t01*t10*t33 + t00*t11*t33;
        m23 = t03*t11*t20 - t01*t13*t20 - t03*t10*t21 + t00*t13*t21 + t01*t10*t23 - t00*t11*t23;

        m30 = t12*t21*t30 - t11*t22*t30 - t12*t20*t31 + t10*t22*t31 + t11*t20*t32 - t10*t21*t32;
        m31 = -t02*t21*t30 + t01*t22*t30 + t02*t20*t31 - t00*t22*t31 - t01*t20*t32 + t00*t21*t32;
        m32 = t02*t11*t30 - t01*t12*t30 - t02*t10*t31 + t00*t12*t31 + t01*t10*t32 - t00*t11*t32;
        m33 = -t02*t11*t20 + t01*t12*t20 + t02*t10*t21 - t00*t12*t21 - t01*t10*t22 + t00*t11*t22;

        m00 *= oneOverDet; m01 *= oneOverDet; m02 *= oneOverDet; m03 *= oneOverDet;
        m10 *= oneOverDet; m11 *= oneOverDet; m12 *= oneOverDet; m13 *= oneOverDet;
        m20 *= oneOverDet; m21 *= oneOverDet; m22 *= oneOverDet; m23 *= oneOverDet;
        m30 *= oneOverDet; m31 *= oneOverDet; m32 *= oneOverDet; m33 *= oneOverDet;

        return this;
    }

    /// Copy to float array. First component at dest + offset.
    ///
    /// **Row major order**
    public void toFloatArray(float[] dest, int offset) {
        dest[offset] = m00;
        dest[offset + 1] = m01;
        dest[offset + 2] = m02;
        dest[offset + 3] = m03;

        dest[offset + 4] = m10;
        dest[offset + 5] = m11;
        dest[offset + 6] = m12;
        dest[offset + 7] = m13;

        dest[offset + 8] = m20;
        dest[offset + 9] = m21;
        dest[offset + 10] = m22;
        dest[offset + 11] = m23;

        dest[offset + 12] = m30;
        dest[offset + 13] = m31;
        dest[offset + 14] = m32;
        dest[offset + 15] = m33;
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
    public static void fromFloatArray(float[] src, int offset, Mat4 dest) {
        dest.m00 = src[offset];
        dest.m01 = src[offset + 1];
        dest.m02 = src[offset + 2];
        dest.m03 = src[offset + 3];

        dest.m10 = src[offset + 4];
        dest.m11 = src[offset + 5];
        dest.m12 = src[offset + 6];
        dest.m13 = src[offset + 7];

        dest.m20 = src[offset + 8];
        dest.m21 = src[offset + 9];
        dest.m22 = src[offset + 10];
        dest.m23 = src[offset + 11];

        dest.m30 = src[offset + 12];
        dest.m31 = src[offset + 13];
        dest.m32 = src[offset + 14];
        dest.m33 = src[offset + 15];
    }

    /// Initialize Mat4 from memory segment, first component at src + byteOffset.
    ///
    /// **Row major order**
    public static void fromMemorySegment(MemorySegment src, long byteOffset, Mat4 dest) {
        long stride = ValueLayout.JAVA_FLOAT.byteSize();
        dest.m00 = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride * 0);
        dest.m01 = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride * 1);
        dest.m02 = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride * 2);
        dest.m03 = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride * 3);

        dest.m10 = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride * 4);
        dest.m11 = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride * 5);
        dest.m12 = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride * 6);
        dest.m13 = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride * 7);

        dest.m20 = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride * 8);
        dest.m21 = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride * 9);
        dest.m22 = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride * 10);
        dest.m23 = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride * 11);

        dest.m30 = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride * 12);
        dest.m31 = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride * 13);
        dest.m32 = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride * 14);
        dest.m33 = src.get(ValueLayout.JAVA_FLOAT, byteOffset + stride * 15);
    }

    /// Copy to memory segment, first element at dest + byteOffset.
    ///
    /// **Row major order**
    public void toMemorySegment(MemorySegment dest, long byteOffset) {
        long stride = ValueLayout.JAVA_FLOAT.byteSize();
        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride * 0, m00);
        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride * 1, m01);
        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride * 2, m02);
        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride * 3, m03);

        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride * 4, m10);
        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride * 5, m11);
        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride * 6, m12);
        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride * 7, m13);

        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride * 8, m20);
        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride * 9, m21);
        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride * 10, m22);
        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride * 11, m23);

        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride * 12, m30);
        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride * 13, m31);
        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride * 14, m32);
        dest.set(ValueLayout.JAVA_FLOAT, byteOffset + stride * 15, m33);
    }

    @Override
    public String toString() {
        return String.format("Mat4({%f, %f, %f, %f}, {%f, %f, %f, %f}, {%f, %f, %f, %f}, {%f, %f, %f, %f})",
                m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23,
                m30, m31, m32, m33);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Mat4 mat4 = (Mat4) o;
        return Float.compare(m00, mat4.m00) == 0 && Float.compare(m01, mat4.m01) == 0
                && Float.compare(m02, mat4.m02) == 0 && Float.compare(m03, mat4.m03) == 0
                && Float.compare(m10, mat4.m10) == 0 && Float.compare(m11, mat4.m11) == 0
                && Float.compare(m12, mat4.m12) == 0 && Float.compare(m13, mat4.m13) == 0
                && Float.compare(m20, mat4.m20) == 0 && Float.compare(m21, mat4.m21) == 0
                && Float.compare(m22, mat4.m22) == 0 && Float.compare(m23, mat4.m23) == 0
                && Float.compare(m30, mat4.m30) == 0 && Float.compare(m31, mat4.m31) == 0
                && Float.compare(m32, mat4.m32) == 0 && Float.compare(m33, mat4.m33) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33);
    }
}
