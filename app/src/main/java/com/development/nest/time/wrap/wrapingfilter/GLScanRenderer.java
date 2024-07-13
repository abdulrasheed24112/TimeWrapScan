package com.development.nest.time.wrap.wrapingfilter;

import static com.development.nest.time.wrap.utils.Constants.HORIZONTAL;
import android.opengl.GLES20;
import com.development.nest.time.wrap.utils.SharedPreferenceHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GLScanRenderer {
    private int programId = -1;
    private int aPositionHandle;
    private int sTextureSamplerHandle;
    private int uTextureSamplerHandle;
    private int aTextureCoordHandle;

    private int scanHeightHandle;
    private int scanLinePositionHandle;
    private int scanLineThicknessHandle;

    private int[] bos = new int[2];
    private int[] textures = new int[2];
    private int[] frameBuffers = new int[2];
    private float thickness = 0.004f;

    public int getProgram() {
        String fragmentShader;
        String orientation = SharedPreferenceHelper.INSTANCE.getPreferences().getString(SharedPreferenceHelper.ORIENTATION, HORIZONTAL);
        if (orientation.equals(HORIZONTAL)) {
            thickness = 0.008f;
            fragmentShader = "varying highp vec2 vTexCoord;\n" + "uniform sampler2D sTexture;\n" + "uniform sampler2D uTexture;\n" + "uniform highp float scanHeight;\n" + "uniform highp float scanLinePosition;\n" + "uniform highp float scanLineThickness;\n" + "void main() {\n" + "   highp float fy = vTexCoord.x;\n" + "   highp vec4 sTexColor = texture2D(sTexture, vTexCoord);\n" + "   highp vec4 uTexColor = texture2D(uTexture, vTexCoord);\n" + "   if (fy >= scanLinePosition - scanLineThickness/2.0 && fy <= scanLinePosition + scanLineThickness/2.0) {\n" + "       gl_FragColor = vec4(0.215, 0.956, 0.956, 1.0); // color for scanner line\n" + "   } else if (fy > scanHeight) {\n" + "       gl_FragColor = sTexColor;\n" + "   } else {\n" + "       gl_FragColor = uTexColor;\n" + "   }\n" + "}";
        } else {
            fragmentShader = "varying highp vec2 vTexCoord;\n" + "uniform sampler2D sTexture;\n" + "uniform sampler2D uTexture;\n" + "uniform highp float scanHeight;\n" + "uniform highp float scanLinePosition;\n" + "uniform highp float scanLineThickness;\n" + "void main() {\n" + "   highp float fy = 1.0 - vTexCoord.y;\n" + "   highp vec4 sTexColor = texture2D(sTexture, vTexCoord);\n" + "   highp vec4 uTexColor = texture2D(uTexture, vTexCoord);\n" + "   if (fy >= scanLinePosition - scanLineThickness/2.0 && fy <= scanLinePosition + scanLineThickness/2.0) {\n" + "       gl_FragColor = vec4(0.215, 0.956, 0.956, 1.0); // color for scanner line\n" + "   } else if (fy > scanHeight) {\n" + "       gl_FragColor = sTexColor;\n" + "   } else {\n" + "       gl_FragColor = uTexColor;\n" + "   }\n" + "}";
        }
        String vertexShader = "attribute vec4 aPosition;\n" + "attribute vec2 aTexCoord;\n" + "varying vec2 vTexCoord;\n" + "void main() {\n" + "  vTexCoord = aTexCoord;\n" + "  gl_Position = aPosition;\n" + "}";
        programId = ShaderUtils.createProgram(vertexShader, fragmentShader);
        aPositionHandle = GLES20.glGetAttribLocation(programId, "aPosition");
        sTextureSamplerHandle = GLES20.glGetUniformLocation(programId, "sTexture");
        uTextureSamplerHandle = GLES20.glGetUniformLocation(programId, "uTexture");
        aTextureCoordHandle = GLES20.glGetAttribLocation(programId, "aTexCoord");
        scanHeightHandle = GLES20.glGetUniformLocation(programId, "scanHeight");
        scanLinePositionHandle = GLES20.glGetUniformLocation(programId, "scanLinePosition");
        scanLineThicknessHandle = GLES20.glGetUniformLocation(programId, "scanLineThickness");

        return programId;
    }

    public void initShader() {
        float[] vertexData = {1f, -1f, 0f, -1f, -1f, 0f, 1f, 1f, 0f, -1f, 1f, 0f};

        float[] textureVertexData = {1f, 0f, 0f, 0f, 1f, 1f, 0f, 1f};
        FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertexData);
        vertexBuffer.position(0);

        FloatBuffer textureVertexBuffer = ByteBuffer.allocateDirect(textureVertexData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(textureVertexData);
        textureVertexBuffer.position(0);

        GLES20.glGenBuffers(2, bos, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bos[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4, vertexBuffer, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bos[1]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, textureVertexData.length * 4, textureVertexBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        GLES20.glGenTextures(textures.length, textures, 0);
        for (int i = 0; i < textures.length; i++) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[i]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        GLES20.glGenFramebuffers(frameBuffers.length, frameBuffers, 0);
    }

    public void setSize(int width, int height) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[0]);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textures[0], 0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[1]);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[1]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textures[1], 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    private int textureIndex = 0;


    public void drawFrame(int texture, float scanHeight, int speed) {
        int index = textureIndex;
        textureIndex = (index + 1) % 2;

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[textureIndex]);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(getProgram());

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bos[0]);
        GLES20.glEnableVertexAttribArray(aPositionHandle);
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bos[1]);
        GLES20.glEnableVertexAttribArray(aTextureCoordHandle);
        GLES20.glVertexAttribPointer(aTextureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        GLES20.glUniform1i(sTextureSamplerHandle, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[index]);
        GLES20.glUniform1i(uTextureSamplerHandle, 1);

        GLES20.glUniform1f(scanHeightHandle, scanHeight);
        float newHeight = scanHeight + 0.015f;
        if (speed > 10) {
            newHeight = scanHeight + 0.02f;
        }
        GLES20.glUniform1f(scanLinePositionHandle, newHeight);
        GLES20.glUniform1f(scanLineThicknessHandle, thickness);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(aPositionHandle);
        GLES20.glDisableVertexAttribArray(aTextureCoordHandle);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

    }

    public int getTexture() {
        return textures[textureIndex];
    }

    public void release() {
        GLES20.glDeleteProgram(programId);
        GLES20.glDeleteTextures(textures.length, textures, 0);
        GLES20.glDeleteFramebuffers(frameBuffers.length, frameBuffers, 0);
        GLES20.glDeleteBuffers(bos.length, bos, 0);
    }
}
