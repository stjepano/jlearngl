package dev.stjepano.platform.opengl;

import java.lang.foreign.MemorySegment;

/// Interface to select OpenGL functions or combinations of functions.
///
/// Note that this interface does not provide access to all OpenGL functions rather higher level
/// abstractions required to render graphics.
@SuppressWarnings("unused")
public interface OpenGL {

    int ALL_MIPS = 0;

    /// Facade for glViewport function which sets the viewport.
    /// @param x Lower left corner in pixels
    /// @param y Lower left corner in pixels
    /// @param width Width of viewport
    /// @param height Height of viewport
    void viewport(int x, int y, int width, int height);

    /// Select a polygon rasterization mode
    /// @param mode specifies how polygons will be rasterized (default FILL)
    void polygonMode(GLPolygonRasterMode mode);

    /// Clears the GL_COLOR buffer of default framebuffer first color attachment with specified color values.
    void clearColorBuffer(float r, float g, float b, float a);

    /// Clears depth and stencil of the default framebuffer.
    void clearDepthStencil(float depthValue, int stencilValue);

    /// Clears depth of the default framebuffer.
    void clearDepth(float depthValue);

    /// Clear depth and stencil of the default framebuffer.
    void clearStencil(int stencilValue);

    /// Create a buffer but do not copy any data to it. Same as calling `createBuffer(byteSize, flags, NULL)`.
    /// @param byteSize capacity of the buffer
    /// @param flags specifies intended usage of the buffer
    /// @throws OpenGLException on OpenGL error
    Buffer createBuffer(long byteSize, BufferStorageFlags flags);

    /// Create a buffer and copy data to it.
    /// @param byteSize capacity of the buffer
    /// @param flags specifies intended usage of the buffer
    /// @param data pointer to data that will be copied to the buffer
    /// @throws OpenGLException on OpenGL error
    Buffer createBuffer(long byteSize, BufferStorageFlags flags, MemorySegment data);

    /// Creates a program from given vertex and fragment shader source (GLSL).
    ///
    /// NOTE: this creates the program, creates shaders and compiles them, links shaders with program
    ///
    /// @param vertexShaderSource the GLSL source code of vertex shader
    /// @param fragmentShaderSource the GLSL source code of fragment shader
    /// @throws OpenGLException in case of any OpenGL error or compilation/linking errors
    Program createProgram(String vertexShaderSource, String fragmentShaderSource);

    /// Create a {@link VertexArray} object
    VertexArray createVertexArray();

    /// Create a texture.
    /// @param mipLevels number of mip levels, 1 for no mipmapping. O/ALL_MIPS for all mipmap levels (auto calculated based on width and height)
    /// @param internalFormat internal texture format
    /// @param width the texture width (of mip level 0)
    /// @param height the texture height (of mip level 0)
    /// @param parameters the texture parameters
    Texture2D createTexture2D(int mipLevels, GLTexture2DFormat internalFormat, int width, int height, Texture2DParameters parameters);

    /// Bind a vertex array for the draw calls.
    /// @param vao the {@link VertexArray} object
    void bindVertexArray(VertexArray vao);

    /// Bind a program for the draw calls.
    /// @param program the {@link Program} object
    void bindProgram(Program program);

    /// Bind a 2D texture to specified unit.
    /// @param unit texture unit (use 0-15 for max compatibility)
    /// @param texture the 2D texture that is bound
    void bindTextureUnit(int unit, Texture2D texture);


    /// Draw command which draws from vertex buffers in the currently bound vertex array.
    /// @param primitiveType the type of primitive to render
    /// @param first the starting index in the enabled buffers
    /// @param count the number of elements from enabled buffers
    void drawArrays(GLPrimitive primitiveType, int first, int count);

    /// Render primitives from bound index buffer.
    /// @param primitiveType the type of primitive to render
    /// @param count number of elements to be rendered (array count in index buffer)
    /// @param indexType the type of values in index buffer (Must be one of UNSIGNED_BYTE, UNSIGNED_SHORT, UNSIGNED_INT)
    /// @param offset a byte offset in index buffer to first index
    void drawElements(GLPrimitive primitiveType, int count, GLDataType indexType, long offset);
}
