#include "glwrap.h"

#include <stdio.h>
#include <glad/glad.h>

void jglViewport(jGLint x, jGLint y, jGLsizei w, jGLsizei h) {
    glViewport((GLint) x, (GLint) y, (GLsizei) w, (GLsizei) h);
}

jboolean jglPolygonMode(jGLenum mode) {
    glPolygonMode(GL_FRONT_AND_BACK, (GLenum) mode);
    return glGetError() == GL_NO_ERROR;
}

void jglClearNamedFramebufferiv(jGLuint framebuffer, jGLenum buffer, jGLint drawbuffer, const jGLint *value) {
    glClearNamedFramebufferiv((GLuint) framebuffer, (GLenum) buffer, (GLint) drawbuffer, (const GLint*) value);
}

void jglClearNamedFramebufferuiv(jGLuint framebuffer, jGLenum buffer, jGLint drawbuffer, const jGLuint *value) {
    glClearNamedFramebufferuiv((GLuint) framebuffer, (GLenum) buffer, (GLint) drawbuffer, (const GLuint*) value);
}

void jglClearNamedFramebufferfv(jGLuint framebuffer, jGLenum buffer, jGLint drawbuffer, const jGLfloat *value) {
    glClearNamedFramebufferfv((GLuint) framebuffer, (GLenum) buffer, (GLint) drawbuffer, (const GLfloat*) value);
}

void jglClearNamedFramebufferfi(jGLuint framebuffer, jGLenum buffer, jGLint drawbuffer, jGLfloat depth, jGLint stencil) {
    glClearNamedFramebufferfi((GLuint) framebuffer, (GLenum) buffer, (GLint) drawbuffer, (GLfloat) depth, (GLint) stencil);
}

jboolean jglNamedBufferSubData(jGLuint buffer, jGLintptr offset, jGLsizeiptr size, const void *data) {
    glNamedBufferSubData((GLuint) buffer, (GLintptr) offset, (GLsizeiptr) size, data);
    return glGetError() == GL_NO_ERROR;
}

void * jglMapNamedBufferRange(jGLuint buffer, jGLintptr offset, jGLsizeiptr length, jGLbitfield accessflags) {
    return glMapNamedBufferRange((GLuint) buffer, (GLintptr) offset, (GLsizeiptr) length, (GLbitfield) accessflags);
}

jboolean jglUnmapNamedBuffer(jGLuint buffer) {
    glUnmapNamedBuffer((GLuint) buffer);
    return glGetError() == GL_NO_ERROR;
}

jboolean jglFlushMappedNamedBufferRange(jGLuint buffer, jGLintptr offset, jGLsizeiptr length) {
    glFlushMappedNamedBufferRange((GLuint) buffer, (GLintptr) offset, (GLsizeiptr) length);
    return glGetError() == GL_NO_ERROR;
}

void jglDeleteBuffers(jGLuint buffer) {
    GLuint buf = buffer;
    glDeleteBuffers(1, &buf);
}

jGLuint jglCreateBufferWithStorage(jGLsizeiptr byteSize, jGLbitfield storageflags, const void *data) {
    GLuint buffer = 0;
    glCreateBuffers(1, &buffer);
    if (buffer) {
        glNamedBufferStorage(buffer, (GLsizeiptr) byteSize, data, (GLbitfield) storageflags);
        if (glGetError() != GL_NO_ERROR) {
            glDeleteBuffers(1, &buffer);
            buffer = 0;
        }
    }
    return (jGLuint) buffer;
}

jGLuint jglCreateProgramVF(const char *vertex_shader_src, const char *fragment_shader_src, char *error_buf, jlong error_buf_size) {
    if (vertex_shader_src == NULL || fragment_shader_src == NULL) {
        snprintf(error_buf, error_buf_size, "Illegal arguments.");
        return 0;
    }
    GLuint vertex_shader_id = 0;
    GLuint fragment_shader_id = 0;
    GLuint program_id = glCreateProgram();
    if (program_id == 0) {
        snprintf(error_buf, error_buf_size, "Could not create GL program.");
        goto LError;
    }
    vertex_shader_id = glCreateShader(GL_VERTEX_SHADER);
    fragment_shader_id = glCreateShader(GL_FRAGMENT_SHADER);
    if (vertex_shader_id == 0) {
        snprintf(error_buf, error_buf_size, "Could not create vertex shader.");
        goto LError;
    }
    if (fragment_shader_id == 0) {
        snprintf(error_buf, error_buf_size, "Could not create fragment shader.");
        goto LError;
    }

    int success;
    char info_log[512];

    glShaderSource(vertex_shader_id, 1, &vertex_shader_src, NULL);
    glCompileShader(vertex_shader_id);
    glGetShaderiv(vertex_shader_id, GL_COMPILE_STATUS, &success);
    if (!success) {
        glGetShaderInfoLog(vertex_shader_id, sizeof(info_log), NULL, info_log);
        snprintf(error_buf, error_buf_size, "vertex shader compile error: %s", info_log);
        goto LError;
    }

    glShaderSource(fragment_shader_id, 1, &fragment_shader_src, NULL);
    glCompileShader(fragment_shader_id);
    glGetShaderiv(fragment_shader_id, GL_COMPILE_STATUS, &success);
    if (!success) {
        glGetShaderInfoLog(fragment_shader_id, sizeof(info_log), NULL, info_log);
        snprintf(error_buf, error_buf_size, "fragment shader compile error: %s", info_log);
        goto LError;
    }

    glAttachShader(program_id, vertex_shader_id);
    glAttachShader(program_id, fragment_shader_id);
    glLinkProgram(program_id);

    glGetProgramiv(program_id, GL_LINK_STATUS, &success);
    if (!success) {
        glGetProgramInfoLog(program_id, sizeof(info_log), NULL, info_log);
        snprintf(error_buf, error_buf_size, "program link error: %s", info_log);
        goto LError;
    }

    goto LSuccess;
LError:
    if (program_id) {
        glDeleteProgram(program_id);
    }
    program_id = 0;
LSuccess:
    if (vertex_shader_id) {
        glDeleteShader(vertex_shader_id);
    }
    if (fragment_shader_id) {
        glDeleteShader(fragment_shader_id);
    }
    return (jGLuint) program_id;
}

void jglBindProgram(jGLuint program_id) {
    glUseProgram((GLuint) program_id);
}

void jglDeleteProgram(jGLuint program_id) {
    GLuint gl_program_id = (GLuint) program_id;
    glDeleteProgram(gl_program_id);
}

jGLuint jglCreateVertexArray() {
    GLuint vao_id = 0;
    glCreateVertexArrays(1, &vao_id);
    return (jGLuint) vao_id;
}

void jglBindVertexArray(jGLuint vao_id) {
    glBindVertexArray((GLuint) vao_id);
}

jboolean jglVertexArrayVertexBuffer(jGLuint vao_id, jGLuint binding_index, jGLuint buffer_id, jGLintptr offset, jGLsizei stride) {
    glVertexArrayVertexBuffer((GLuint) vao_id, (GLuint) binding_index, (GLuint) buffer_id, (GLintptr) offset, (GLsizei) stride);
    return glGetError() == GL_NO_ERROR;
}

jboolean jglVertexArrayIndexBuffer(jGLuint vao_id, jGLuint buffer_id) {
    glVertexArrayElementBuffer((GLuint) vao_id, (GLuint) buffer_id);
    return glGetError() == GL_NO_ERROR;
}

jboolean jglVertexArrayVertexAttrib(jGLuint vao_id, jGLuint attrib_index, jGLuint buffer_binding_index, jGLint num_component,
                                    jGLenum component_type, jboolean normalized, jGLuint offset) {

    glVertexArrayAttribFormat((GLuint)vao_id, (GLuint)attrib_index, (GLint)num_component,
        (GLenum)component_type, (GLboolean) (normalized ? GL_TRUE : GL_FALSE), (GLuint)offset);
    glVertexArrayAttribBinding((GLuint) vao_id, (GLuint)attrib_index, (GLuint) buffer_binding_index);
    glEnableVertexArrayAttrib((GLuint) vao_id, (GLuint) attrib_index);
    return glGetError() == GL_NO_ERROR;
}

jboolean jglVertexArrayToggleAttrib(jGLuint vao_id, jGLuint attrib_index, jboolean enabled) {
    if (enabled) {
        glEnableVertexArrayAttrib((GLuint) vao_id, (GLuint) attrib_index);
    } else {
        glDisableVertexArrayAttrib((GLuint) vao_id, (GLuint) attrib_index);
    }
    return glGetError() == GL_NO_ERROR;
}

void jglDeleteVertexArray(jGLuint vao_id) {
    GLuint gl_vao_id = (GLuint) vao_id;
    glDeleteVertexArrays(1, &gl_vao_id);
}

void jglDrawArrays(jGLenum mode, jGLint first, jGLsizei count) {
    glDrawArrays((GLenum) mode, (GLint)first, (GLsizei) count);
}

void jglDrawElements(jGLenum mode, jGLsizei count, jGLenum type, jlong offset) {
    glDrawElements((GLenum) mode, (GLsizei) count, (GLenum) type, (const GLvoid*)offset);
}
