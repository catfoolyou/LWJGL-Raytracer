varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;

void main() {
    vec2 uv = v_texCoords;

    vec3 col = vec3(uv.xy, 0);

    gl_FragColor = vec4(col, 1.0);
}
