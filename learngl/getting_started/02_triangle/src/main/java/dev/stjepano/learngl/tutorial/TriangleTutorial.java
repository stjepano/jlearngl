package dev.stjepano.learngl.tutorial;

import dev.stjepano.platform.*;
import dev.stjepano.platform.opengl.Buffer;
import dev.stjepano.platform.opengl.BufferStorageFlags;
import dev.stjepano.platform.opengl.OpenGL;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class TriangleTutorial {

    static void main() {
        WindowSettings windowSettings = new WindowSettings(1920, 1080, "LearnOpenGL - Triangle", true);
        try (Platform platform = Platform.defaultInstance();
             Window window = platform.createWindow(windowSettings);
             Arena arena = Arena.ofConfined()) {

            final Keyboard keyboard = window.keyboard();
            final OpenGL gl = platform.openGl();

            gl.viewport(0, 0, window.framebufferWidth(), window.framebufferHeight());

            float[] vertices = new float[] {
                    -0.5f, -0.5f, 0.0f,
                    0.5f, -0.5f, 0.0f,
                    0.0f, 0.5f, 0.0f
            };

            MemorySegment verticesPtr = arena.allocateFrom(ValueLayout.JAVA_FLOAT, vertices);
            BufferStorageFlags storageFlags = BufferStorageFlags.builder()
                    .build();
            Buffer vertexBuffer = gl.createBuffer(verticesPtr.byteSize(), storageFlags, verticesPtr);

            while (!window.shouldClose()) {
                platform.pollEvents();
                gl.clearColorBuffer(0.2f, 0.3f, 0.4f, 1.0f);
                if (keyboard.isPressed(Key.ESCAPE)) {
                    IO.println("ESCAPE pressed");
                    window.setShouldClose(true);
                    continue;
                }

                window.swapBuffers();
            }

            vertexBuffer.delete();
        }
    }
}
