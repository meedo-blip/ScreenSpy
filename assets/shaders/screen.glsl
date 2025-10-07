#type vertex
#version 330 core

layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aTexCoord;

out vec2 TexCoord;

void main() {
    gl_Position = vec4(aPos, 0.0, 1.0);
    TexCoord = aTexCoord;
}

#type fragment
#version 330 core

in vec2 TexCoord;
out vec4 FragColor;

uniform sampler2D screenTexture;

void main() {
    FragColor = vec4(texture(screenTexture, TexCoord).xyz, 1.0);
}
