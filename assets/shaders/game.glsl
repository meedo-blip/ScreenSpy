#type vertex
#version 330 core

layout (location=0) in vec3 aCenter;
layout (location=1) in vec2 aScale;
layout (location=2) in vec3 aColor;
layout (location=3) in float aRotate;

out vec3 fColor;
out vec2 fTexCoords;
out vec2 fCoords;
out float fTexId, fAttribs;

uniform mat4 uView;
uniform mat4 uProjection;
uniform float uPixel; // pixel width

void main() {
    // element vertex id: gl_VertexID

    //  1      0
    //
    //  2      3

    int vert = gl_VertexID & 3;
    int attribs = int(aCenter.z);

    vec2 center = aCenter.xy * uPixel;

    vec2 pos = (vert == 2 ? vec2(-aScale.x, -aScale.y)
    : vert == 3 ? vec2(aScale.x, -aScale.y)
    : vert == 1 ? vec2(-aScale.x, aScale.y)
    : vec2(aScale.x, aScale.y));

    fColor = aColor;
    fAttribs = abs(attribs >> 4);
    fTexCoords = vert == 0 ? vec2(0f,1f)
        : vert == 1 ? vec2(0f, 0f)
        : vert == 2 ? vec2(1f, 0f)
        : vec2(1f, 1f);

    fTexId = attribs & 15;

    fCoords = vec2(round(aCenter.x), round(aCenter.y));

    if((attribs & 112) == 0) {
        float c = cos(aRotate), s = sin(aRotate);
        gl_Position = uView * uProjection * vec4(((pos.x * c) - (pos.y * s)) + center.x,
        ((s * pos.x) + (c * pos.y)) + center.y, -1, 1.0);
    } else {
        gl_Position = uView * uProjection * vec4(pos + center, -1, 1.0f);
    }
}

#type fragment
#version 330 core

in vec3 fColor;
in vec2 fTexCoords;
in vec2 fCoords;
in float fTexId;
in float fAttribs;

uniform sampler2D uTextures[8];
                            //  y = mx + c
// xy coords, gradient, c(and isClockwise bit)
uniform vec2 uHead, uBack;
out vec4 color;

// -3 Hemicircle
// -2 circle
// -1 box

// 0 -> 3 snake conjunction
// 2    3

// 0    1

void main() {
    int shape = int(fAttribs) - 3;

    if (shape == -3) {
        if (pow(fTexCoords.x, 2) + pow(fTexCoords.y - 0.5f, 2) > 0.25)
        discard;
    }
    else if (shape == -2) {
        if (pow(fTexCoords.x - 0.5f, 2) + pow(fTexCoords.y - 0.5f, 2) > 0.25f) {
            discard;
        }
    }

    else if (shape >= 0) {
        float ox = (shape & 1)  ^ ((shape & 1) ^ ((shape & 2) >> 1)),
        oy = ((shape & 2) >> 1) ^ ((shape & 1) ^ ((shape & 2) >> 1));

        float dist = pow(fTexCoords.x - ox, 2) + pow(fTexCoords.y - oy, 2);

        if (dist < 0.0675f || dist > 0.5625f) {
            discard;
        }

        // float m = ((oy - (uHead.y - floor(uHead.y)) / (ox - (uHead.x - floor(uHead.x)))));
        // float c = (oy - (m * (ox)));

        // if(round(uHead.x) == fCoords.x && round(uHead.y) == fCoords.y) {
        //      if (fTexCoords.y > (m * fTexCoords.x) + c)
        //         discard;
        //  }
        // else if(uBack.x == fCoords.x && uBack.y == fCoords.y) {
        //if (fTexCoords.y * (ox - uBack.x) < (oy - uBack.y) * fTexCoords.x)
        //  discard;
    }


    if (fTexId > 0) {
        color = vec4(fColor.xyz, 1f) * texture(uTextures[int(fTexId)], fTexCoords);
    } else {
        color = vec4(fColor.xyz, 1f);
    }

   // if(fTexCoords.x <= 0.05f || fTexCoords.y <= 0.05f
    //|| fTexCoords.x >= 0.95f || fTexCoords.y >= 0.95f) {
    //    color = vec4(1,1,1,1);
    //}
}
