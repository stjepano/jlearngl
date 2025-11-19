package dev.stjepano.platform.opengl;

public interface VertexArray extends GpuResource {
    /// Binds the buffer to the vertex buffer binding point.
    /// @param bindingIndex the index of vertex buffer binding point
    /// @param buffer the buffer to bind
    /// @param offset the offset of the first element in buffer (in bytes)
    /// @param stride the distance between elements in the buffer (in bytes)
    /// @throws OpenGLException in case of any OpenGL error
    void vertexBuffer(int bindingIndex, Buffer buffer, long offset, int stride);

    /// Specify the organization of data in vertex array. The vertex attribute is enabled by default.
    /// @param attribIndex the vertex attribute index
    /// @param bufferBindingIndex the buffer binding index in the vertex array with which to associate the vertex attribute
    /// @param numComponents the number of values per vertex (number of components)
    /// @param componentType the type of data (component)
    /// @param normalized true if parameter represents normalized integer
    /// @param offset The offset in bytes of the first element relative to the start of vertex buffer binding
    void vertexAttrib(int attribIndex, int bufferBindingIndex, int numComponents, GLDataType componentType, boolean normalized, int offset);

    /// Enable or disable vertex attribute at index.
    /// @param attribIndex vertex attribute index
    /// @param enabled specifies if attribute is enabled or disabled
    void toggleAttrib(int attribIndex, boolean enabled);

    /// Configure element array buffer binding for vertex array
    void indexBuffer(Buffer indexBuffer);
}
