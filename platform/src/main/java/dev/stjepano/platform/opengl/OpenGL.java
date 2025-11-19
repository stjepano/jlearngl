package dev.stjepano.platform.opengl;

import java.lang.foreign.MemorySegment;

/// Interface to select OpenGL functions or combinations of functions.
///
/// Note that this interface does not provide access to all OpenGL functions rather higher level
/// abstractions required to render graphics.
@SuppressWarnings("unused")
public interface OpenGL {

    /// Facade for glViewport function which sets the viewport.
    /// @param x Lower left corner in pixels
    /// @param y Lower left corner in pixels
    /// @param width Width of viewport
    /// @param height Height of viewport
    void viewport(int x, int y, int width, int height);

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

}
