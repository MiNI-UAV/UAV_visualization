#version 420 core
layout (location = 0) in vec2 aPos;

out vec2 fragPos;

uniform mat3x2 transform;

void main() {
        vec2 transformed = transform * vec3(aPos, 1.f);
        fragPos = transformed;
        gl_Position = vec4(transformed, 1.0f, 1.0f);
}
