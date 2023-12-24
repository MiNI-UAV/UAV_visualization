#version 420 core
out vec4 fragColor;

in vec2 fragPos;
in vec2 texCoord;

uniform sampler2D glyph;
uniform vec4 color;
uniform vec4 cropRectangle;
uniform float gammaCorrection;


void main()
{
    if(fragPos.x < cropRectangle.x || fragPos.y < cropRectangle.y || fragPos.x > cropRectangle.x + cropRectangle.z || fragPos.y > cropRectangle.y + cropRectangle.w)
        discard;
    float alpha = texture(glyph, texCoord).a;
    vec3 result = color.rgb;
    result = pow(result, vec3(1.0/gammaCorrection));
    fragColor = vec4(result, alpha * color.a);
}