#type vertex
#version 330 core

layout (location=0) in vec3 aCenter;
layout (location=1) in vec2 aScale;
layout (location=2) in vec4 aColor;
layout (location=3) in vec2 aTexCoords;
layout (location=4) in float aTexId;

out vec4 fColor;
out vec2 fTexCoords;
out float fTexId;

uniform mat4 uView;
uniform mat4 uProjection;
uniform mat3 uRotation;

void main() {
    // element vertex id: gl_VertexID

    //  1      0
    //
    //  2      3

    int vert = gl_VertexID & 3;

    vec3 pos = aCenter + vec3(vert == 2 ? -aScale
                            : vert == 3 ? vec2(aScale.x, -aScale.y)
                            : vert == 1 ? vec2(-aScale.x, aScale.y)
                            : aScale , 0.0);

    fColor = aColor;
    fTexCoords = aTexCoords;
    fTexId = aTexId;

    gl_Position = uView * uProjection * vec4(pos, 1.0);

}

#type fragment
#version 330 core

in vec4 fColor;
in vec2 fTexCoords;
in float fTexId;

uniform sampler2D uTextures[8];

out vec4 color;

void main() {
    float c = texture(uTextures[int(fTexId)], fTexCoords).g;

    if (c>0.5f) {
        color =  vec4(fColor.xyz, 1);
    }
    else if(c <= 0.5f && c > 0.45f)
        color = vec4(fColor.xyz * 0.5f,1);
    else
        discard;
}
