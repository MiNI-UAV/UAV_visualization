#version 420 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoords;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec4 fragPos;

void main()
{
    fragPos = projection * view * model * vec4(aPos, 1.0f);
    gl_Position = fragPos;
}