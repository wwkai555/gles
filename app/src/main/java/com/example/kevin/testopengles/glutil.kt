package com.example.kevin.testopengles

import android.opengl.GLES20
import android.opengl.Matrix

/**
 * Created by kevin on 05/09/2017.
 */
private fun loadShader(shaderType: Int, source: String): Int {
    return GLES20.glCreateShader(shaderType).apply {
        "glCreateShader: $this".print("--->>>")
        if (this != 0) {
            GLES20.glShaderSource(this, source)
            GLES20.glCompileShader(this)
            val compiled = IntArray(1)
            GLES20.glGetShaderiv(this, GLES20.GL_COMPILE_STATUS, compiled, 0)
            "compile shader : ${compiled[0]}".print("--->>")
            if (compiled[0] == 0) {
                "Could not compile shader $shaderType:$source".print("--->>:")
                GLES20.glGetShaderInfoLog(this).print("--->>ShaderInfo:")
                GLES20.glDeleteShader(this)
                return 0
            }
        }
    }
}

fun createProgram(vertexSource: String, fragmentSource: String): GLProgramInfo? {
    val program = GLES20.glCreateProgram()
    check(-1 == program.print("--->> create program: ")) { return "createProgram failed: cannot allocate program".print("--->>").run { null } }
    val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
    check(vertexShader == 0) { return "createProgram failed: cannot create vertex shader".print("--->>").run { null } }
    val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
    check(fragmentShader == 0) {
        "createProgram failed : cannot create fragment shader".print("--->>")
        GLES20.glDeleteProgram(program)
        GLES20.glDeleteShader(vertexShader)
        return null
    }
    GLES20.glAttachShader(program, vertexShader)
    if (checkGlError("-->>glAttachVertexShader error: ")) {
        GLES20.glDeleteShader(vertexShader)
        GLES20.glDeleteShader(fragmentShader)
        GLES20.glDeleteProgram(program)
        return null
    }
    GLES20.glAttachShader(program, fragmentShader)
    if (checkGlError("--->>glAttachFragmentShader error：")) {
        GLES20.glDeleteShader(vertexShader)
        GLES20.glDeleteShader(fragmentShader)
        GLES20.glDeleteProgram(program)
        return null
    }
    GLES20.glLinkProgram(program)
    val linkStatus = IntArray(1)
    GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
    if (linkStatus[0] != GLES20.GL_TRUE) {
        "Could not link program: ${GLES20.glGetProgramInfoLog(program)} ".print("--->>")
        GLES20.glDeleteShader(vertexShader)
        GLES20.glDeleteShader(fragmentShader)
        GLES20.glDeleteProgram(program)
        return null
    }
    return GLProgramInfo(program, vertexShader, fragmentShader)
}

fun genTextures(count: Int, type: Int): IntArray? {
    val textures = IntArray(count)
    GLES20.glGenTextures(count, textures, 0)
    checkGlError("--->>glGenTextures error: ") { return null }
    for (texture in textures) {
        GLES20.glBindTexture(type, texture)
        checkGlError("--->>glBindTexture error:") {
            GLES20.glDeleteTextures(count, textures, 0)
            return null
        }
        GLES20.glTexParameteri(type, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(type, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(type, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(type, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        checkGlError("--->>glTexParameter error: ") {
            GLES20.glDeleteTextures(count, textures, 0)
            return null
        }
    }
    return textures
}

fun fullClipBounds(): FloatArray = floatArrayOf(0f, 0f, 1f, 1f)

fun checkGlError(msg: String): Boolean {
    GLES20.glGetError().run {
        return if (this != GLES20.GL_NO_ERROR) {
            this.print(msg)
            true
        } else false
    }
}

inline fun checkGlError(msg: String, action: () -> Unit) {
    GLES20.glGetError().run {
        if (this != GLES20.GL_NO_ERROR) {
            print(msg)
            action()
        } else return@run
    }
}

inline fun check(condition: Boolean, lazyMsg: () -> Unit): Unit {
    if (condition) lazyMsg()
}

data class GLProgramInfo(internal val program: Int, internal val vertexShader: Int, internal val fragmentShader: Int)