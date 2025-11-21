#include "glwrap.h"

#include <assert.h>
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
    glClearNamedFramebufferiv((GLuint) framebuffer, (GLenum) buffer, (GLint) drawbuffer, (const GLint *) value);
}

void jglClearNamedFramebufferuiv(jGLuint framebuffer, jGLenum buffer, jGLint drawbuffer, const jGLuint *value) {
    glClearNamedFramebufferuiv((GLuint) framebuffer, (GLenum) buffer, (GLint) drawbuffer, (const GLuint *) value);
}

void jglClearNamedFramebufferfv(jGLuint framebuffer, jGLenum buffer, jGLint drawbuffer, const jGLfloat *value) {
    glClearNamedFramebufferfv((GLuint) framebuffer, (GLenum) buffer, (GLint) drawbuffer, (const GLfloat *) value);
}

void jglClearNamedFramebufferfi(jGLuint framebuffer, jGLenum buffer, jGLint drawbuffer, jGLfloat depth,
                                jGLint stencil) {
    glClearNamedFramebufferfi((GLuint) framebuffer, (GLenum) buffer, (GLint) drawbuffer, (GLfloat) depth,
                              (GLint) stencil);
}

jboolean jglNamedBufferSubData(jGLuint buffer, jGLintptr offset, jGLsizeiptr size, const void *data) {
    glNamedBufferSubData((GLuint) buffer, (GLintptr) offset, (GLsizeiptr) size, data);
    return glGetError() == GL_NO_ERROR;
}

void *jglMapNamedBufferRange(jGLuint buffer, jGLintptr offset, jGLsizeiptr length, jGLbitfield accessflags) {
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

jGLuint jglCreateProgramVF(const char *vertex_shader_src, const char *fragment_shader_src, char *error_buf,
                           jlong error_buf_size) {
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

void jglProgramUniform1i(jGLuint program_id, jGLint location, jGLint i) {
    glProgramUniform1i((GLuint) program_id, (GLint) location, (GLint) i);
}

void jglProgramUniform1iv(jGLuint program_id, jGLint location, jGLsizei count, const jGLint *value_ptr) {
    glProgramUniform1iv((GLuint) program_id, (GLint) location, (GLsizei) count, (const GLint *) value_ptr);
}

void jglProgramUniform1f(jGLuint program_id, jGLint location, jGLfloat x) {
    glProgramUniform1f((GLuint) program_id, (GLint) location, (GLfloat) x);
}

void jglProgramUniform1fv(jGLuint program_id, jGLint location, jGLsizei count, const jGLfloat *value_ptr) {
    glProgramUniform1fv((GLuint) program_id, (GLint) location, (GLsizei) count, (const GLfloat *) value_ptr);
}

void jglProgramUniform2f(jGLuint program_id, jGLint location, jGLfloat x, jGLfloat y) {
    glProgramUniform2f((GLuint) program_id, (GLint) location, (GLfloat) x, (GLfloat) y);
}

void jglProgramUniform2fv(jGLuint program_id, jGLint location, jGLsizei count, const jGLfloat *value_ptr) {
    glProgramUniform2fv((GLuint) program_id, (GLint) location, (GLsizei) count, (const GLfloat *) value_ptr);
}

void jglProgramUniform3f(jGLuint program_id, jGLint location, jGLfloat x, jGLfloat y, jGLfloat z) {
    glProgramUniform3f((GLuint) program_id, (GLint) location, (GLfloat) x, (GLfloat) y, (GLfloat) z);
}

void jglProgramUniform3fv(jGLuint program_id, jGLint location, jGLsizei count, const jGLfloat *value_ptr) {
    glProgramUniform3fv((GLuint) program_id, (GLint) location, (GLsizei) count, (const GLfloat *) value_ptr);
}

void jglProgramUniform4f(jGLuint program_id, jGLint location, jGLfloat x, jGLfloat y, jGLfloat z, jGLfloat w) {
    glProgramUniform4f((GLuint) program_id, (GLint) location, (GLfloat) x, (GLfloat) y, (GLfloat) z, (GLfloat) w);
}

void jglProgramUniform4fv(jGLuint program_id, jGLint location, jGLsizei count, const jGLfloat *value_ptr) {
    glProgramUniform4fv((GLuint) program_id, (GLint) location, (GLsizei) count, (const GLfloat *) value_ptr);
}

void jglProgramUniformMatrix2fv(jGLuint program_id, jGLint location, jGLsizei count, jboolean transpose,
                                const jGLfloat *value_ptr) {
    glProgramUniformMatrix2fv((GLuint) program_id, (GLint) location, (GLsizei) count, (GLboolean) transpose,
                              (const GLfloat *) value_ptr);
}

void jglProgramUniformMatrix3fv(jGLuint program_id, jGLint location, jGLsizei count, jboolean transpose,
                                const jGLfloat *value_ptr) {
    glProgramUniformMatrix3fv((GLuint) program_id, (GLint) location, (GLsizei) count, (GLboolean) transpose,
                              (const GLfloat *) value_ptr);
}

void jglProgramUniformMatrix4fv(jGLuint program_id, jGLint location, jGLsizei count, jboolean transpose,
                                const jGLfloat *value_ptr) {
    glProgramUniformMatrix4fv((GLuint) program_id, (GLint) location, (GLsizei) count, (GLboolean) transpose,
                              (const GLfloat *) value_ptr);
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

jboolean jglVertexArrayVertexBuffer(jGLuint vao_id, jGLuint binding_index, jGLuint buffer_id, jGLintptr offset,
                                    jGLsizei stride) {
    glVertexArrayVertexBuffer((GLuint) vao_id, (GLuint) binding_index, (GLuint) buffer_id, (GLintptr) offset,
                              (GLsizei) stride);
    return glGetError() == GL_NO_ERROR;
}

jboolean jglVertexArrayIndexBuffer(jGLuint vao_id, jGLuint buffer_id) {
    glVertexArrayElementBuffer((GLuint) vao_id, (GLuint) buffer_id);
    return glGetError() == GL_NO_ERROR;
}

jboolean jglVertexArrayVertexAttrib(jGLuint vao_id, jGLuint attrib_index, jGLuint buffer_binding_index,
                                    jGLint num_component,
                                    jGLenum component_type, jboolean normalized, jGLuint offset) {
    glVertexArrayAttribFormat((GLuint) vao_id, (GLuint) attrib_index, (GLint) num_component,
                              (GLenum) component_type, (GLboolean) (normalized ? GL_TRUE : GL_FALSE), (GLuint) offset);
    glVertexArrayAttribBinding((GLuint) vao_id, (GLuint) attrib_index, (GLuint) buffer_binding_index);
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

/// Parameter stream format: [paramCode:int][data:varies][paramCode:int][data:varies]...[0:int]
/// Return 1 on success 0 on fail.
static int configure_texture(GLuint texture_id, const void *params, GLsizeiptr paramsSize) {
    (void) paramsSize;

    if (params) {
        const uint8_t *it = params;
        GLint paramCode;
        while ((paramCode = *(GLint *) it) != 0) {
            GLenum parameter = (GLenum) paramCode;
            it += sizeof(GLint);
            switch (parameter) {
                case GL_TEXTURE_MIN_FILTER: {
                    GLint value = *(GLint *) it;
                    it += sizeof(GLint);
                    glTextureParameteri(texture_id, GL_TEXTURE_MIN_FILTER, value);
                }
                break;
                case GL_TEXTURE_MAG_FILTER: {
                    GLint value = *(GLint *) it;
                    it += sizeof(GLint);
                    glTextureParameteri(texture_id, GL_TEXTURE_MAG_FILTER, value);
                }
                break;
                case GL_TEXTURE_WRAP_S: {
                    GLint value = *(GLint *) it;
                    it += sizeof(GLint);
                    glTextureParameteri(texture_id, GL_TEXTURE_WRAP_S, value);
                }
                break;
                case GL_TEXTURE_WRAP_T: {
                    GLint value = *(GLint *) it;
                    it += sizeof(GLint);
                    glTextureParameteri(texture_id, GL_TEXTURE_WRAP_T, value);
                }
                break;
                case GL_TEXTURE_BORDER_COLOR: {
                    glTextureParameterfv(texture_id, GL_TEXTURE_BORDER_COLOR, (const GLfloat *) it);
                    it += 4 * sizeof(GLfloat);
                }
                break;
                case GL_TEXTURE_MIN_LOD: {
                    GLfloat val = *(GLfloat *) it;
                    it += sizeof(GLfloat);
                    glTextureParameterf(texture_id, GL_TEXTURE_MIN_LOD, val);
                }
                break;
                case GL_TEXTURE_MAX_LOD: {
                    GLfloat val = *(GLfloat *) it;
                    it += sizeof(GLfloat);
                    glTextureParameterf(texture_id, GL_TEXTURE_MAX_LOD, val);
                }
                break;
                case GL_TEXTURE_LOD_BIAS: {
                    GLfloat val = *(GLfloat *) it;
                    it += sizeof(GLfloat);
                    glTextureParameterf(texture_id, GL_TEXTURE_LOD_BIAS, val);
                }
                break;
                case GL_TEXTURE_BASE_LEVEL: {
                    GLint value = *(GLint *) it;
                    it += sizeof(GLint);
                    glTextureParameteri(texture_id, GL_TEXTURE_BASE_LEVEL, value);
                }
                break;
                case GL_TEXTURE_MAX_LEVEL: {
                    GLint value = *(GLint *) it;
                    it += sizeof(GLint);
                    glTextureParameteri(texture_id, GL_TEXTURE_MAX_LEVEL, value);
                }
                break;
                case GL_TEXTURE_MAX_ANISOTROPY: {
                    GLfloat val = *(GLfloat *) it;
                    it += sizeof(GLfloat);
                    glTextureParameterf(texture_id, GL_TEXTURE_MAX_ANISOTROPY, val);
                }
                break;
                case GL_TEXTURE_COMPARE_MODE: {
                    GLint value = *(GLint *) it;
                    it += sizeof(GLint);
                    glTextureParameteri(texture_id, GL_TEXTURE_COMPARE_MODE, value);
                }
                break;
                case GL_TEXTURE_COMPARE_FUNC: {
                    GLint value = *(GLint *) it;
                    it += sizeof(GLint);
                    glTextureParameteri(texture_id, GL_TEXTURE_COMPARE_FUNC, value);
                }
                break;
                default:
                    return 0;
            }
        }
        if (glGetError() != GL_NO_ERROR) {
            return 0;
        }
    }

    return 1;
}

/// Parameter stream format: [paramCode:int][data:varies][paramCode:int][data:varies]...[0:int]
/// Return 1 on success 0 on fail.
static int configure_sampler(GLuint sampler_id, const void *params, GLsizeiptr paramsSize) {
    (void) paramsSize;

    if (params) {
        const uint8_t *it = params;
        GLint paramCode;
        while ((paramCode = *(GLint *) it) != 0) {
            GLenum parameter = (GLenum) paramCode;
            it += sizeof(GLint);
            switch (parameter) {
                case GL_TEXTURE_MIN_FILTER: {
                    GLint value = *(GLint *) it;
                    it += sizeof(GLint);
                    glSamplerParameteri(sampler_id, GL_TEXTURE_MIN_FILTER, value);
                }
                break;
                case GL_TEXTURE_MAG_FILTER: {
                    GLint value = *(GLint *) it;
                    it += sizeof(GLint);
                    glSamplerParameteri(sampler_id, GL_TEXTURE_MAG_FILTER, value);
                }
                break;
                case GL_TEXTURE_WRAP_S: {
                    GLint value = *(GLint *) it;
                    it += sizeof(GLint);
                    glSamplerParameteri(sampler_id, GL_TEXTURE_WRAP_S, value);
                }
                break;
                case GL_TEXTURE_WRAP_T: {
                    GLint value = *(GLint *) it;
                    it += sizeof(GLint);
                    glSamplerParameteri(sampler_id, GL_TEXTURE_WRAP_T, value);
                }
                break;
                case GL_TEXTURE_WRAP_R: {
                    GLint value = *(GLint *) it;
                    it += sizeof(GLint);
                    glSamplerParameteri(sampler_id, GL_TEXTURE_WRAP_R, value);
                }
                break;
                case GL_TEXTURE_BORDER_COLOR: {
                    glSamplerParameterfv(sampler_id, GL_TEXTURE_BORDER_COLOR, (const GLfloat *) it);
                    it += 4 * sizeof(GLfloat);
                }
                break;
                case GL_TEXTURE_MIN_LOD: {
                    GLfloat val = *(GLfloat *) it;
                    it += sizeof(GLfloat);
                    glSamplerParameterf(sampler_id, GL_TEXTURE_MIN_LOD, val);
                }
                break;
                case GL_TEXTURE_MAX_LOD: {
                    GLfloat val = *(GLfloat *) it;
                    it += sizeof(GLfloat);
                    glSamplerParameterf(sampler_id, GL_TEXTURE_MAX_LOD, val);
                }
                break;
                case GL_TEXTURE_LOD_BIAS: {
                    GLfloat val = *(GLfloat *) it;
                    it += sizeof(GLfloat);
                    glSamplerParameterf(sampler_id, GL_TEXTURE_LOD_BIAS, val);
                }
                break;
                case GL_TEXTURE_MAX_ANISOTROPY: {
                    GLfloat val = *(GLfloat *) it;
                    it += sizeof(GLfloat);
                    glSamplerParameterf(sampler_id, GL_TEXTURE_MAX_ANISOTROPY, val);
                }
                break;
                case GL_TEXTURE_COMPARE_MODE: {
                    GLint value = *(GLint *) it;
                    it += sizeof(GLint);
                    glSamplerParameteri(sampler_id, GL_TEXTURE_COMPARE_MODE, value);
                }
                break;
                case GL_TEXTURE_COMPARE_FUNC: {
                    GLint value = *(GLint *) it;
                    it += sizeof(GLint);
                    glSamplerParameteri(sampler_id, GL_TEXTURE_COMPARE_FUNC, value);
                }
                break;
                default:
                    return 0;
            }
        }
        if (glGetError() != GL_NO_ERROR) {
            return 0;
        }
    }

    return 1;
}

jGLuint jglCreateTexture2D(jGLint mipLevels, jGLenum internalFormat, jGLsizei width, jGLsizei height,
                           const void *parameterStream, jlong parameterStreamSize) {
    GLuint texture_id;
    glCreateTextures(GL_TEXTURE_2D, 1, &texture_id);
    if (texture_id == 0) {
        goto LError;
    }

    glTextureStorage2D(texture_id, (GLint) mipLevels, (GLenum) internalFormat, (GLsizei) width, (GLsizei) height);

    if (!configure_texture(texture_id, parameterStream, parameterStreamSize)) {
        goto LError;
    }

    goto LSuccess;
LError:
    if (texture_id) {
        glDeleteTextures(1, &texture_id);
    }
    texture_id = 0;
LSuccess:
    return (jGLuint) texture_id;
}

jboolean jglTextureUpdateSubImage2D(jGLuint textureId, jGLint mipLevel, jGLint x, jGLint y, jGLsizei width,
                                    jGLsizei height, jGLenum format, jGLenum type, jGLint rowAlignment,
                                    jGLint rowLength, const void *pixels) {
    GLint old_alignment, old_row_length;
    glGetIntegerv(GL_UNPACK_ALIGNMENT, &old_alignment);
    glGetIntegerv(GL_UNPACK_ROW_LENGTH, &old_row_length);

    glPixelStorei(GL_UNPACK_ALIGNMENT, (GLint) rowAlignment);
    glPixelStorei(GL_UNPACK_ROW_LENGTH, (GLint) rowLength);

    glTextureSubImage2D((GLuint) textureId, (GLint) mipLevel,
                        (GLint) x, (GLint) y, (GLsizei) width, (GLsizei) height,
                        (GLenum) format, (GLenum) type, pixels);

    glPixelStorei(GL_UNPACK_ALIGNMENT, old_alignment);
    glPixelStorei(GL_UNPACK_ROW_LENGTH, old_row_length);

    return glGetError() == GL_NO_ERROR;
}

jboolean jglTextureUpdateSubImage2DBuffer(jGLuint textureId, jGLint mipLevel, jGLint x, jGLint y, jGLsizei width,
                                          jGLsizei height, jGLenum format, jGLenum type, jGLint rowAlignment,
                                          jGLint rowLength, jGLuint bufferId,
                                          jGLsizeiptr offset) {
    GLint old_alignment, old_row_length;
    glGetIntegerv(GL_UNPACK_ALIGNMENT, &old_alignment);
    glGetIntegerv(GL_UNPACK_ROW_LENGTH, &old_row_length);

    glPixelStorei(GL_UNPACK_ALIGNMENT, (GLint) rowAlignment);
    glPixelStorei(GL_UNPACK_ROW_LENGTH, (GLint) rowLength);

    glBindBuffer(GL_PIXEL_UNPACK_BUFFER, (GLuint) bufferId);

    glTextureSubImage2D((GLuint) textureId, (GLint) mipLevel,
                        (GLint) x, (GLint) y, (GLsizei) width, (GLsizei) height,
                        (GLenum) format, (GLenum) type, (const void *) offset);

    glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);

    glPixelStorei(GL_UNPACK_ALIGNMENT, old_alignment);
    glPixelStorei(GL_UNPACK_ROW_LENGTH, old_row_length);

    return glGetError() == GL_NO_ERROR;
}

void jglTextureGenerateMipmaps(jGLuint textureId) {
    glGenerateTextureMipmap((GLuint) textureId);
}

jboolean jglTextureConfigure(jGLuint textureId, const void *parameterStream, jlong parameterStreamSize) {
    return (jboolean) configure_texture((GLuint) textureId, parameterStream, parameterStreamSize);
}

void jglDeleteTexture(jGLuint textureId) {
    GLuint texture_id = (GLuint) textureId;
    glDeleteTextures(1, &texture_id);
}

void jglBindTextureUnit(jGLuint unit, jGLuint textureId) {
    glBindTextureUnit((GLuint) unit, (GLuint) textureId);
}

jGLuint jglCreateSampler(const void *parameterStream, jlong parameterStreamSize) {
    GLuint sampler_id;
    glCreateSamplers(1, &sampler_id);
    if (sampler_id == 0) {
        goto LError;
    }

    if (!configure_sampler(sampler_id, parameterStream, parameterStreamSize)) {
        goto LError;
    }

    goto LSuccess;
LError:
    if (sampler_id) {
        glDeleteSamplers(1, &sampler_id);
    }
    sampler_id = 0;
LSuccess:
    return (jGLuint) sampler_id;
}

jboolean jglSamplerConfigure(jGLuint samplerId, const void *parameterStream, jlong parameterStreamSize) {
    return (jboolean) (configure_sampler((GLuint) samplerId, parameterStream, parameterStreamSize) != 0);
}

void jglDeleteSampler(jGLuint samplerId) {
    GLuint sampler_id = (GLuint) samplerId;
    glDeleteSamplers(1, &sampler_id);
}

void jglBindSamplerUnit(jGLuint unit, jGLuint samplerId) {
    glBindSampler((GLuint) unit, (GLuint) samplerId);
}

void jglDrawArrays(jGLenum mode, jGLint first, jGLsizei count) {
    glDrawArrays((GLenum) mode, (GLint) first, (GLsizei) count);
}

void jglDrawElements(jGLenum mode, jGLsizei count, jGLenum type, jlong offset) {
    glDrawElements((GLenum) mode, (GLsizei) count, (GLenum) type, (const GLvoid *) offset);
}
