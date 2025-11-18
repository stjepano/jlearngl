package dev.stjepano.platform;

import dev.stjepano.platform.impl.PlatformImpl;

@SuppressWarnings("unused")
public interface Platform extends AutoCloseable {

    /// On first call creates a instance and returns it. On subsequent calls returns the instance.
    static Platform defaultInstance() {
        return PlatformImpl.getInstance();
    }

    /// Create a new window.
    ///
    /// This also creates completely new OpenGL context which is associated with the window.
    Window createWindow(WindowSettings windowSettings);

    /// Poll all the events related to window(s) created by this platform and update internal states and timers.
    ///
    /// You should call this only once per frame.
    void pollEvents();

    /// Sets the OpenGL context associated with the `window` as the "current" GL context on the calling thread.
    ///
    /// After this all OpenGL functions called from current thread operate on that context.
    /// If you specify `null` then no context is active on this thread anymore.
    ///
    /// _When you `createWindow` its context becomes current automatically so you don't need to call this explicitly._
    void setGLContextForCurrentThread(Window window);

    /// Gives access to platform time.
    Time time();

    /// Access to OpenGL functionality.
    OpenGL openGl();

    /// Close the platform (and all windows). Release all resources.
    @Override
    void close();


}
