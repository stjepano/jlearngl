#include "stbwrap.h"

#define STB_IMAGE_IMPLEMENTATION
#include <stb_image.h>

void * jstbiLoadFromFile(const char *filePathPtr, jboolean flipVertically, ImageInfo *outImageInfoPtr,
    char *outErrorBuf, jint outErrorBufSize) {
    assert(filePathPtr);
    assert(outImageInfoPtr);
    assert(outErrorBuf);
    assert(outErrorBufSize > 0);

    stbi_set_flip_vertically_on_load_thread(flipVertically);
    int width, height, channels;
    stbi_uc *pixels = stbi_load(filePathPtr, &width, &height, &channels, 0);
    if (pixels == NULL) {
        const char* failure_reason = stbi_failure_reason();
        snprintf(outErrorBuf, outErrorBufSize, "%s", failure_reason == NULL ? "unknown" : failure_reason);
        return NULL;
    }

    outImageInfoPtr->width = width;
    outImageInfoPtr->height = height;
    outImageInfoPtr->channels = channels;

    return pixels;
}

void * jstbiLoadFromMemory(const void *memory, jlong memorySize, jboolean flipVertically, ImageInfo *outImageInfoPtr,
    char *outErrorBuf, jint outErrorBufSize) {
    assert(memory);
    assert(outImageInfoPtr);
    assert(outErrorBuf);
    assert(outErrorBufSize > 0);

    if (memorySize <= 0 || memorySize > INT32_MAX) {
        snprintf(outErrorBuf, outErrorBufSize, "Invalid memory size: %ld", memorySize);
        return NULL;
    }

    stbi_set_flip_vertically_on_load_thread(flipVertically);
    int width, height, channels;
    stbi_uc *pixels = stbi_load_from_memory(memory, (int) memorySize, &width, &height, &channels, 0);
    if (pixels == NULL) {
        const char* failure_reason = stbi_failure_reason();
        snprintf(outErrorBuf, outErrorBufSize, "%s", failure_reason == NULL ? "unknown" : failure_reason);
        return NULL;
    }

    outImageInfoPtr->width = width;
    outImageInfoPtr->height = height;
    outImageInfoPtr->channels = channels;

    return pixels;
}

void jstbiFree(void *memory) {
    if (memory) {
        stbi_image_free(memory);
    }
}
