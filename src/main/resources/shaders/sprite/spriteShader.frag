#version 420 core
out vec4 fragColor;

in vec2 texCoord;

uniform sampler2D sprite;
uniform mat3x2 transform;
uniform float opacity;
uniform float gammaCorrection;

void main()
{
    vec4 color = texture(sprite, texCoord);
    vec3 result = color.xyz;
    result = pow(result, vec3(1.0/gammaCorrection));
    fragColor = vec4(result, color.w * opacity);
}