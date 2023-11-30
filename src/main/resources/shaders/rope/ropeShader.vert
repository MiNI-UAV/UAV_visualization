#version 330 core
layout (location = 0) in float aT;
layout (location = 1) in float aTNext;

out VS_OUT {
    float t;
    float tNext;
} vs_out;

void main()
{
    vs_out.t = aT;
    vs_out.tNext = aTNext;
}