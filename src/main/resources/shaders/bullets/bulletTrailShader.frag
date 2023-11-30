#version 330 core
out vec4 fragColor;

uniform vec3 color;

in float opacity;

void main()
{
    fragColor = vec4(color, opacity);
}
