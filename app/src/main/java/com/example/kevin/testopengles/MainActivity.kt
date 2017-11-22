package com.example.kevin.testopengles

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import org.jetbrains.anko.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : AppCompatActivity() {
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var glRenderer: GLRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glRenderer = GLRenderer(this@MainActivity)
        frameLayout {
            backgroundColor = Color.BLUE
            glSurfaceView = gLSurfaceView {
                setEGLContextClientVersion(2)
                setRenderer(glRenderer)
                renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
            }.lparams(matchParent, matchParent)
        }
    }
}

class GLRenderer(context: Context) : GLSurfaceView.Renderer {
    private var bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.u)
    private lateinit var program: IdentifyProgram
    private lateinit var texture: Texture
    private var mMVPMatrix: FloatArray = identityMatrix
    private var mSTMatrix: FloatArray = identityMatrix
    private lateinit var viewPort: Size
    private var textureSize: Size = Size(bitmap.width, bitmap.height)
    private val temp = FloatArray(16)

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        program.draw(temp, mSTMatrix, texture)
        GLES20.glFinish()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        viewPort = Size(width, height)
        GLES20.glViewport(0, 0, width, height)
        val xScale = (textureSize.width / viewPort.width.toFloat()).print("---y scale factor : ")
        val yScale = (textureSize.height / viewPort.height.toFloat()).print("---x scaleFactor: ")
        mMVPMatrix.apply {
            //            translateM(temp, 0, this, 0, 0.5f, 0.5f, 0f)
//            rotateM(temp1, 0, temp, 0, 90f, 0f, 0f, 1f)
//            scaleM(temp2, 0, temp1, 0, 1 / xScale, 1 / yScale, 1f)
//            translateM(temp3, 0, temp2, 0, -0.5f, -0.5f, 0f)
            scaleM(temp, 0, this, 0, xScale, yScale, 1f)

        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        program = IdentifyProgram.create()
        texture = genTextures(1, GLES20.GL_TEXTURE_2D)?.run {
            Texture(this[0], GLES20.GL_TEXTURE_2D).apply {
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
                bind()
                GLUtils.texImage2D(type, 0, bitmap, 0)
                checkGlError("---->> upload bitmap error") { GLES20.glDeleteTextures(1, intArrayOf(id), 0) }
                bitmap.recycle()
            }
        }!!
    }
}

fun <T> T.print(msg: String = "") = apply { Log.d("KLG", msg + this) }

data class Size(val width: Int, val height: Int)