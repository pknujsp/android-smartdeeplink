//
// Created by jesp on 2023-07-04.
//

#include "glblurringview.h"

#define INDICES_SIZE 6

static const char *vertexShaderCode = R"(
    uniform mat4 uMVPMatrix;
    attribute vec4 vPosition;
    attribute vec2 a_texCoord;
    varying vec2 v_texCoord;
    void main() {
      gl_Position = uMVPMatrix * vPosition;
      v_texCoord = a_texCoord;
    }
)";

static const char *fragmentShaderCode = R"(
    precision mediump float;
    varying vec2 v_texCoord;
    uniform sampler2D s_texture;
    void main() {
      gl_FragColor = texture2D(s_texture, v_texCoord);
    }
)";

static const float vertices[] = {
        -1.0f, -1.0f, 0.0f,  // bottom left
        1.0f, -1.0f, 0.0f,  // bottom right
        1.0f, 1.0f, 0.0f,  // top right
        -1.0f, 1.0f, 0.0f,  // top left
};

static const char indices[INDICES_SIZE] = {
        0, 1, 2,
        2, 3, 0,
};

static void *vertexBuffer;

static void *indexBuffer;

static GLuint positionHandle = 0;
static GLuint uvHandle = 0;
static GLuint program = 0;
static GLint mvpMatrixHandle = 0;

static GLuint textures[1];
static GLfloat vpMatrix[16];
static GLfloat modelMatrix[16];
static GLfloat mvpMatrix[16];

static GLint bitmapWidth = 0;
static GLint bitmapHeight = 0;

GLuint loadShader(int type, const char *shaderCode);

void multiplyMM(GLfloat result[16], int resultOffset, GLfloat lhs[16], int lhsOffset, GLfloat rhs[16], int rhsOffset);

bool overlap(const GLfloat *a, int aStart, int aLength, const GLfloat *b, int bStart, int bLength);

void setIdentityM(GLfloat sm[16], int smOffset);

extern "C"
JNIEXPORT void JNICALL
Java_io_github_pknujsp_blur_natives_NativeGLBlurringImpl_onSurfaceCreated(JNIEnv *env, jobject thiz) {
    program = glCreateProgram();
    glAttachShader(program, loadShader(GL_VERTEX_SHADER, vertexShaderCode));
    glAttachShader(program, loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode));
    glLinkProgram(program);

    glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
}

GLuint loadShader(GLenum type, const char *shaderCode) {
    GLuint shader = glCreateShader(type);
    glShaderSource(shader, 1, &shaderCode, nullptr);
    glCompileShader(shader);
}


extern "C"
JNIEXPORT void JNICALL
Java_io_github_pknujsp_blur_natives_NativeGLBlurringImpl_onDrawFrame(JNIEnv *env, jobject thiz, jobject bitmap) {
    glClear(GL_COLOR_BUFFER_BIT bitor GL_DEPTH_BUFFER_BIT);

    if (bitmap != nullptr) {
        glBindTexture(GL_TEXTURE_2D, textures[0]);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        void *pixels = nullptr;
        AndroidBitmap_lockPixels(env, bitmap, (void **) &pixels);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, bitmapWidth, bitmapHeight, 0,
                     GL_RGBA, GL_UNSIGNED_BYTE, pixels);


        glDrawElements(GL_TRIANGLES, INDICES_SIZE, GL_UNSIGNED_BYTE, indexBuffer);
        glDeleteTextures(1, textures);
        AndroidBitmap_unlockPixels(env, bitmap);
    }
}

void multiplyMM(GLfloat *result, int resultOffset, GLfloat *lhs, int lhsOffset, GLfloat *rhs, int rhsOffset) {
    if (overlap(result, resultOffset, 16, lhs, lhsOffset, 16)
        || overlap(result, resultOffset, 16, rhs, rhsOffset, 16)) {
        float tmp[32];
        for (int i = 0; i < 4; i++) {
            float rhs_i0 = rhs[4 * i + 0 + rhsOffset];
            float ri0 = lhs[0 + lhsOffset] * rhs_i0;
            float ri1 = lhs[1 + lhsOffset] * rhs_i0;
            float ri2 = lhs[2 + lhsOffset] * rhs_i0;
            float ri3 = lhs[3 + lhsOffset] * rhs_i0;
            for (int j = 1; j < 4; j++) {
                float rhs_ij = rhs[4 * i + j + rhsOffset];
                ri0 += lhs[4 * j + 0 + lhsOffset] * rhs_ij;
                ri1 += lhs[4 * j + 1 + lhsOffset] * rhs_ij;
                ri2 += lhs[4 * j + 2 + lhsOffset] * rhs_ij;
                ri3 += lhs[4 * j + 3 + lhsOffset] * rhs_ij;
            }
            tmp[4 * i + 0] = ri0;
            tmp[4 * i + 1] = ri1;
            tmp[4 * i + 2] = ri2;
            tmp[4 * i + 3] = ri3;
        }

        // copy from tmp to result
        for (int i = 0; i < 16; i++) {
            result[i + resultOffset] = tmp[i];
        }

    } else {
        for (int i = 0; i < 4; i++) {
            float rhs_i0 = rhs[4 * i + 0 + rhsOffset];
            float ri0 = lhs[0 + lhsOffset] * rhs_i0;
            float ri1 = lhs[1 + lhsOffset] * rhs_i0;
            float ri2 = lhs[2 + lhsOffset] * rhs_i0;
            float ri3 = lhs[3 + lhsOffset] * rhs_i0;
            for (int j = 1; j < 4; j++) {
                float rhs_ij = rhs[4 * i + j + rhsOffset];
                ri0 += lhs[4 * j + 0 + lhsOffset] * rhs_ij;
                ri1 += lhs[4 * j + 1 + lhsOffset] * rhs_ij;
                ri2 += lhs[4 * j + 2 + lhsOffset] * rhs_ij;
                ri3 += lhs[4 * j + 3 + lhsOffset] * rhs_ij;
            }
            result[4 * i + 0 + resultOffset] = ri0;
            result[4 * i + 1 + resultOffset] = ri1;
            result[4 * i + 2 + resultOffset] = ri2;
            result[4 * i + 3 + resultOffset] = ri3;
        }
    }
}

bool overlap(const GLfloat *a, int aStart, int aLength, const GLfloat *b, int bStart, int bLength) {
    if (a != b)
        return false;
    if (aStart == bStart)
        return true;

    int aEnd = aStart + aLength;
    int bEnd = bStart + bLength;

    if (aEnd == bEnd)
        return true;
    if (aStart < bStart && bStart < aEnd)
        return true;
    if (aStart < bEnd && bEnd < aEnd)
        return true;
    if (bStart < aStart && aStart < bEnd)
        return true;
    if (bStart < aEnd && aEnd < bEnd)
        return true;

    return false;
}

extern "C"
JNIEXPORT void JNICALL
Java_io_github_pknujsp_blur_natives_NativeGLBlurringImpl_onSurfaceChanged(JNIEnv *env, jobject thiz, jint width, jint height,
                                                                          jintArray collecting_view_rect, jintArray window_rect) {
    glViewport(0, 0, width, height);
    bitmapWidth = width;
    bitmapHeight = height;

    positionHandle = glGetAttribLocation(program, "vPosition");
    mvpMatrixHandle = glGetUniformLocation(program, "uMVPMatrix");

    glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix);
    setIdentityM(modelMatrix, 0);
    setIdentityM(mvpMatrix, 0);

    glUseProgram(program);
    glEnableVertexAttribArray(positionHandle);
    glVertexAttribPointer(positionHandle, 3, GL_FLOAT, false, 12, vertexBuffer);

    int collecting_view_rect_size = env->GetArrayLength(collecting_view_rect);
    int window_rect_size = env->GetArrayLength(window_rect);

    jint *collecting_view_rect_array = env->GetIntArrayElements(collecting_view_rect, nullptr);
    jint *window_rect_array = env->GetIntArrayElements(window_rect, nullptr);

    GLfloat statusBarRatio = (GLfloat) collecting_view_rect_array[1] / (GLfloat) window_rect_array[3];
    GLfloat navigationBarRatio = (GLfloat) (window_rect_array[3] - collecting_view_rect_array[3]) / (GLfloat) window_rect_array[3];

    env->ReleaseIntArrayElements(collecting_view_rect, collecting_view_rect_array, 0);
    env->ReleaseIntArrayElements(window_rect, window_rect_array, 0);

    delete collecting_view_rect_array;
    delete window_rect_array;

    GLuint uvsVBO;
    glGenBuffers(1, &uvsVBO);
    glBindBuffer(GL_ARRAY_BUFFER, uvsVBO);

    const GLfloat uvs[] = {
            0.0f, 1.0f - navigationBarRatio,
            1.0f, 1.0f - navigationBarRatio,
            1.0f, statusBarRatio,
            0.0f, statusBarRatio,
    };
    glBufferData(GL_ARRAY_BUFFER, sizeof(uvs), uvs, GL_STATIC_DRAW);

    GLuint indexVBO;
    glGenBuffers(1, &indexVBO);
    glBindBuffer(GL_ARRAY_BUFFER, indexVBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(indices), indices, GL_STATIC_DRAW);

    uvHandle = glGetAttribLocation(program, "a_texCoord");
    glEnableVertexAttribArray(uvHandle);
    glVertexAttribPointer(uvHandle, 2, GL_FLOAT, GL_FALSE, 0, nullptr);

    multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0);

    glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix);

    glGenTextures(1, textures);
    glActiveTexture(GL_TEXTURE0);
}

void setIdentityM(GLfloat *sm, int smOffset) {
    for (int i = 0; i < 16; i++) {
        sm[smOffset + i] = 0;
    }
    for (int i = 0; i < 16; i += 5) {
        sm[smOffset + i] = 1.0f;
    }
}
