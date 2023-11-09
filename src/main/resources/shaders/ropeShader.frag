#version 330 core
out vec4 fragColor;

in vec3 out_color;
in vec3 out_normal;
in vec3 fragPos;

uniform vec3 viewPos;
uniform vec3 backgroundColor;

struct DirLight {
    vec3 direction;
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};
uniform DirLight dirLight;


vec3 calcDirLight(DirLight light, vec3 normal, vec3 viewDir, vec4 objectColor);

void main()
{
    vec3 viewDir = normalize(viewPos - fragPos);
    vec3 result = backgroundColor;
    result = calcDirLight(dirLight, out_normal, viewDir, vec4(out_color,1));
    result *= out_color;
    fragColor = vec4(result, 1);
}

vec3 calcDirLight(DirLight light, vec3 normal, vec3 viewDir, vec4 objectColor)
{
    vec3 lightDir = normalize(-light.direction);
    float diff = max(dot(normal, lightDir), 0.0);
    float kPi = 3.14159265;
    float kEnergyConservation = ( 8.0 + 1.2/*material.shininess*/ ) / ( 8.0 * kPi );
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float spec = kEnergyConservation * pow(max(dot(normal, halfwayDir), 0.0), 1.2);
    vec3 ambient  = light.ambient;
    vec3 diffuse  = light.diffuse  * diff;
    vec3 specular = light.specular * spec;

    return (ambient + (diffuse + specular));
}
