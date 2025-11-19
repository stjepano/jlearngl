package dev.stjepano.learngl.tutorial;

import dev.stjepano.platform.*;
import dev.stjepano.platform.opengl.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;

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

            String vertexShaderSrc = loadText("/shaders/vertex.glsl");
            String fragmentShaderSrc = loadText("/shaders/fragment.glsl");
            Program program = gl.createProgram(vertexShaderSrc, fragmentShaderSrc);

            VertexArray vao = gl.createVertexArray();
            int attribLocation = 0;
            vao.vertexBuffer(0, vertexBuffer, 0,  3 * (int)GLDataType.FLOAT.byteSize());
            vao.vertexAttrib(attribLocation, 0, 3, GLDataType.FLOAT, false,  0);
            vao.toggleAttrib(attribLocation, true);

            while (!window.shouldClose()) {
                platform.pollEvents();
                gl.clearColorBuffer(0.2f, 0.3f, 0.4f, 1.0f);
                if (keyboard.isPressed(Key.ESCAPE)) {
                    IO.println("ESCAPE pressed");
                    window.setShouldClose(true);
                    continue;
                }

                gl.bindVertexArray(vao);
                gl.bindProgram(program);
                gl.drawArrays(GLPrimitive.TRIANGLES, 0, 3);

                window.swapBuffers();
            }

            vao.delete();
            program.delete();
            vertexBuffer.delete();
        }
    }

    private static String loadText(String resourcePath) throws RuntimeException {
        try (InputStream inputStream = TriangleTutorial.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new RuntimeException("Resource " + resourcePath + " not found!");
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
