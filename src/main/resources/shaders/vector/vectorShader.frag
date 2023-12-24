#version 420 core
out vec4 fragColor;

in vec2 fragPos;

uniform vec4 color;
uniform float gammaCorrection;
uniform vec4 cropRectangle;


void main()
{
    if(fragPos.x < cropRectangle.x || fragPos.y < cropRectangle.y || fragPos.x > cropRectangle.x + cropRectangle.z || fragPos.y > cropRectangle.y + cropRectangle.w)
        discard;
    vec3 result = color.xyz;
    result = pow(result, vec3(1.0/gammaCorrection));
    fragColor = vec4(result, color.w);
}