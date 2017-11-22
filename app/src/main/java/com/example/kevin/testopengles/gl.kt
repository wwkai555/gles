package com.example.kevin.testopengles

import android.opengl.GLES20
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Created by kevin on 21/11/2017.
 */
val TRIANGLE_VERTEX_DATA = floatArrayOf(
        -1.0f, -1.0f, 0f, 0f, 1f,
        1.0f, -1.0f, 0f, 1f, 1f,
        -1.0f, 1.0f, 0f, 0f, 0f,
        1.0f, 1.0f, 0f, 1f, 0f)
val FLOAT_SIZE_BYTES = 4
val TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES
val TRIANGLE_VERTICES_DATA_POS_OFFSET = 0
val TRIANGLE_VERTICES_DATA_UV_OFFSET = 3

class IdentifyProgram private constructor(private val glProgramInfo: GLProgramInfo) {
    companion object {
        private val vertexShader: String = "uniform mat4 uMVPMatrix;\n" +
                "uniform mat4 uSTMatrix;\n" +
                "attribute vec4 aPosition;\n" +
                "attribute vec4 aTexturePosition;\n" +
                "varying vec2 vTextureCoord;\n" +
                "void main(){\n" +
                "  gl_Position = uMVPMatrix * aPosition;\n" +
                "  vTextureCoord = (uSTMatrix * aTexturePosition).xy;\n" +
                "}"

        private val fragmentShader: String = "precision mediump float;\n" +
                "varying vec2 vTextureCoord;\n" +
                "uniform sampler2D texture;\n" +
                "void main() {\n" +
                "  gl_FragColor = texture2D(texture,vTextureCoord);\n" +
                "}"

        private var identifyProgram: IdentifyProgram? = null
        val vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(TRIANGLE_VERTEX_DATA.size * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(TRIANGLE_VERTEX_DATA).position(0) }
        private var mvpLocation: Int = -1
        private var stLocation: Int = -1
        private var textureLocation: Int = -1

        fun create(): IdentifyProgram {
            synchronized(this) {
                createProgram(vertexShader, fragmentShader)?.apply {
                    GLES20.glUseProgram(program)
                    GLES20.glGetAttribLocation(program, "aPosition").apply {
                        if (-1 == this) "failed to get attribute location for  + aPosition".print("--->>")
                        checkGlError("glGetAttribLocation aPosition")
                        vertexBuffer.position(TRIANGLE_VERTICES_DATA_POS_OFFSET)
                        GLES20.glEnableVertexAttribArray(this)
                        GLES20.glVertexAttribPointer(this, 3, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, vertexBuffer)
                        checkGlError("--->>setPositionVertexAttribute error: ")
                    }

                    GLES20.glGetAttribLocation(program, "aTexturePosition").apply {
                        if (-1 == this) "failed to get attribute location for  + aPosition".print("--->>")
                        checkGlError("glGetAttribLocation aPosition")
                        vertexBuffer.position(TRIANGLE_VERTICES_DATA_UV_OFFSET)
                        GLES20.glEnableVertexAttribArray(this)
                        GLES20.glVertexAttribPointer(this, 2, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, vertexBuffer)
                        checkGlError("--->>setUVVertexAttribute error: ")
                    }
                    GLES20.glGetUniformLocation(program, "uMVPMatrix").run {
                        if (this == -1) "--->>failed to get uniform location for uMVPMatrix".print()
                        checkGlError("--->>glGetUniformLocation uMVPMatrix error: ")
                        mvpLocation = this
                    }
                    GLES20.glGetUniformLocation(program, "uSTMatrix").run {
                        if (this == -1) "--->>failed to get uniform location for mSTMatrix".print()
                        checkGlError("--->>glGetUniformLocation mSTMatrix error: ")
                        stLocation = this
                    }

                    GLES20.glGetUniformLocation(program, "texture").run {
                        if (this == -1) "--->>failed to get uniform location for texture".print()
                        checkGlError("--->>glGetUniformLocation texture error: ")
                        textureLocation = this
                    }
                    return identifyProgram ?: IdentifyProgram(this)
                }
            }
            throw RuntimeException("----create program failed----")
        }
    }


    fun draw(mMVPMatrix: FloatArray, mSTMatrix: FloatArray, texture: Texture) {
        glProgramInfo.apply {
            GLES20.glUniformMatrix4fv(mvpLocation.print("---mvpLocation: "), 1, false, mMVPMatrix, 0)
            checkGlError("--->>setMatrix4fv error: ")

            GLES20.glUniformMatrix4fv(stLocation.print("--->>stLocation: "), 1, false, mSTMatrix, 0)
            checkGlError("--->>setMatrix4fv error: ")

            GLES20.glUniform1i(textureLocation.print("--->>textureLocation: "), 0)
            checkGlError("--->>set1i error: ")

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        }
    }
}

fun Texture.bind() {
    GLES20.glBindTexture(type, id)
    GLES20.glTexParameteri(type, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
    GLES20.glTexParameteri(type, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
    GLES20.glTexParameteri(type, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
    GLES20.glTexParameteri(type, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
}

data class Texture(val id: Int, val type: Int)

val identityMatrix: FloatArray = FloatArray(16).apply {
    Matrix.setIdentityM(this, 0)
}