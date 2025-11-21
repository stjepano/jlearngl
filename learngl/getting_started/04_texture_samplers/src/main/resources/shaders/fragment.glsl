#version 460 core

in vec3 vColor;
in vec2 vTexCoord;

out vec4 oFragColor;

layout(location = 0) uniform sampler2D uContainerTexture;
layout(location = 1) uniform sampler2D uFaceTexture;

void main() {
    vec4 color = vec4(vColor.r, vColor.g, vColor.b, 1.0) * mix(texture(uContainerTexture, vTexCoord), texture(uFaceTexture, vTexCoord), 0.5);
    oFragColor = vec4(color.rgb, 1.0);
}