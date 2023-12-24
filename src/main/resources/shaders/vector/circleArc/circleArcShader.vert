#version 420 core
layout (location = 0) in vec2 aPos;

out vec2 fragPos;
out vec2 transformedCenter;

uniform mat3x2 transform;
uniform vec2 center;

void main() {
    vec2 transformed = transform * vec3(aPos, 1.f);
    fragPos = transformed;
    gl_Position = vec4(transformed, 1.0f, 1.0f);
    transformedCenter = transform * vec3(center, 1.f);
}
