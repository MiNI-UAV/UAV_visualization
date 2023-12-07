#version 420 core
layout(binding=2)uniform sampler2D droneMask;
out vec4 fragColor;

uniform vec4 color;

in vec4 fragPos;

void main()
{
    vec3 pos = fragPos.xyz / fragPos.w;
    pos = pos * 0.5 + 0.5;

    float dx = (1.0 / textureSize(droneMask, 0).x) * 1.0;
    float dy = (1.0 / textureSize(droneMask, 0).y) * 1.0;

    vec2 center   = pos.xy;
    float mCenter   = texture(droneMask, center).r;
    float delta = 0.0;

    for(int sx = -1; sx <= 1; ++sx)
    {
        for(int sy = -1; sy <= 1; ++sy)
        {
            if(sx == 0 && sy == 0) continue;
            vec2 p = vec2(center.x + dx * sx, center.y + dy * sy);
            float m = texture(droneMask, p).r;
            float d = abs(mCenter - m);
            delta = max(delta, d);
        }
    }

    if(delta < 0.2)
        discard;

    fragColor = color;
}
