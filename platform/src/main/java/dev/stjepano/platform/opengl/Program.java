package dev.stjepano.platform.opengl;

public interface Program extends GpuResource {

    /// Set uniform - single integer
    void setInt(int location, int i);

    /// Set uniform - integer array
    void setIntArray(int location, int[] array);

    /// Set uniform - single float
    void setFloat(int location, float x);

    /// Set uniform - array of floats
    void setFloatArray(int location, float[] array);

    /// Set uniform - vec2
    void setFloat2(int location, float x, float y);

    /// Set uniform - array of vec2
    void setFloat2Array(int location, float[] array);

    /// Set uniform - vec3
    void setFloat3(int location, float x, float y, float z);

    /// Set uniform - array of vec3
    void setFloat3Array(int location, float[] array);

    /// Set uniform - vec4
    void setFloat4(int location, float x, float y, float z, float w);

    /// Set uniform - array of vec4
    void setFloat4Array(int location, float[] array);

    /// Set uniform - array of mat2 (each mat2 is 4 elements)
    void setMat2Array(int location, boolean transpose, float[] array);

    /// Set uniform - array of mat3 (each mat3 is 9 elements)
    void setMat3Array(int location, boolean transpose, float[] array);

    /// Set uniform - array of mat4 (each mat4 is 16 elements)
    void setMat4Array(int location, boolean transpose, float[] array);



}
