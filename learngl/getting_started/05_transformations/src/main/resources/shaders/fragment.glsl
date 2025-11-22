#version 460 core

in vec3 vColor;
in vec2 vTexCoord;

out vec4 oFragColor;

layout(location = 1) uniform float uBrightness;
layout(location = 2) uniform sampler2D uContainerTexture;
layout(location = 3) uniform sampler2D uFaceTexture;

void main() {
    vec4 color = vec4(vColor.r, vColor.g, vColor.b, 1.0) * mix(texture(uContainerTexture, vTexCoord), texture(uFaceTexture, vTexCoord), 0.5);
    oFragColor = vec4(color.rgb * uBrightness, 1.0);
}