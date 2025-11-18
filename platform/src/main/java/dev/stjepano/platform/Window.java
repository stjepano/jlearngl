package dev.stjepano.platform;

public interface Window extends AutoCloseable {

    // TODO: content scale (DPI support)
    // TODO: minimization, maximization ...

    /// Get access to keyboard for the window.
    Keyboard keyboard();

    /// Get access to mouse for the window.
    Mouse mouse();

    /// Return true if user requested window to be closed or, if programmatically `setShouldClose(true)` was called.
    boolean shouldClose();

    /// If set to true will cause `shouldClose()` to return true. If set to false will reset the request to
    /// close the window.
    void setShouldClose(boolean value);

    /// Swap OpenGL buffers effectively showing the rendered image on window.
    void swapBuffers();

    /// Get the window width
    int width();

    /// Get the window height
    int height();

    /// Get the framebuffer width (this does not need to be same as window width)
    int framebufferWidth();

    /// Get the framebuffer height (this does not need to be same as window height)
    int framebufferHeight();

    @Override
    void close();
}
