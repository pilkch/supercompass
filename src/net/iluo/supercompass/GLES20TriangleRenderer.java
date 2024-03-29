package net.iluo.supercompass;

import net.iluo.supercompass.R;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

class GLES20TriangleRenderer implements GLSurfaceView.Renderer {

  class Texture
  {
    public Texture()
    {
      id = -1;
    }

    public void CreateFromResource(int idResource)
    {
      int[] textures = new int[1];
      GLES20.glGenTextures(1, textures, 0);

      id = textures[0];
      GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id);

      GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
      GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

      InputStream is = mContext.getResources().openRawResource(idResource);
      Bitmap bitmap;
      try {
        bitmap = BitmapFactory.decodeStream(is);
      } finally {
        try {
          is.close();
        } catch(IOException e) {
          // Ignore.
        }
      }

      GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
      bitmap.recycle();
    }

    public void Destroy()
    {
    }

    public void Bind()
    {
      GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id);
    }

    public void UnBind()
    {
      GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    private int id;
  };

  class VertexBuffer
  {
    public void CreateFromMemory(FloatBuffer buffer)
    {
      mTriangleVertices = ByteBuffer.allocateDirect(buffer.capacity() * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
      mTriangleVertices.put(buffer).position(0);
    }

    public void Destroy()
    {
    }

    public void Bind()
    {
      mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
      GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
      checkGlError("glVertexAttribPointer maPosition");
      mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
      GLES20.glEnableVertexAttribArray(maPositionHandle);
      checkGlError("glEnableVertexAttribArray maPositionHandle");
      GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
      checkGlError("glVertexAttribPointer maTextureHandle");
      GLES20.glEnableVertexAttribArray(maTextureHandle);
      checkGlError("glEnableVertexAttribArray maTextureHandle");
    }

    public void Render()
    {
      final int nVertices = 6;
      GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, nVertices);
      checkGlError("glDrawArrays");
    }

    public void UnBind()
    {
    }

    private FloatBuffer mTriangleVertices;
  };

  class GeometryBuilder
  {
    GeometryBuilder()
    {
      buffer = FloatBuffer.allocate(100);
    }

    public void PushBackRectangle(float x, float y, float width, float height)
    {
      PushBackRectangle(x, y, width, height, 1.0f, 0.0f, 0.0f, 1.0f);
    }

    public void PushBackRectangle(float x, float y, float width, float height, float fU, float fV, float fU2, float fV2)
    {
      final float fHalfWidth = width / 2.0f;
      final float fHalfHeight = height / 2.0f;
      final float[] vertices = {
        // X, Y, Z, U, V
        x - fHalfWidth, y + fHalfHeight, 0.0f, fU, fV,
        x - fHalfWidth, y - fHalfHeight, 0.0f, fU, fV2,
        x + fHalfWidth, y + fHalfHeight, 0.0f, fU2, fV,
        x - fHalfWidth, y - fHalfHeight, 0.0f, fU, fV2,
        x + fHalfWidth, y + fHalfHeight, 0.0f, fU2, fV,
        x + fHalfWidth, y - fHalfHeight, 0.0f, fU2, fV2
      };
      buffer.put(vertices);
    }

    public FloatBuffer GetBuffer()
    {
      buffer.position(0);
      return buffer;
    }

    private FloatBuffer buffer;
  }


  public GLES20TriangleRenderer(Context context)
  {
    mStyle = Settings.STYLE.ORIENTEERING;
    mDialAngle = 0.0f;
    mCompassValues = new float[3];
    mCompassValues[0] = 0.0f;
    mCompassValues[1] = 0.0f;
    mCompassValues[2] = 0.0f;
    mContext = context;
    textureOrienteeringMap = new Texture();
    textureOrienteering = new Texture();

    {
      GeometryBuilder builder = new GeometryBuilder();
      final float x = 0.0f;
      final float y = 0.0f;
      final float width = 2.0f;
      final float height = 3.4f;
      builder.PushBackRectangle(x, y, width, height, 1.0f, 0.0f, 0.0f, 1.0f);
      vertexBufferOrienteeringMap = new VertexBuffer();
      vertexBufferOrienteeringMap.CreateFromMemory(builder.GetBuffer());
    }
    {
      GeometryBuilder builder = new GeometryBuilder();
      final float x = 0.0f;
      final float y = -0.7f;
      final float width = 1.4f;
      final float height = 3.2f;
      builder.PushBackRectangle(x, y, width, height, 0.5f, 0.0f, 1.0f, 1.0f);
      vertexBufferOrienteeringBody = new VertexBuffer();
      vertexBufferOrienteeringBody.CreateFromMemory(builder.GetBuffer());
    }
    {
      GeometryBuilder builder = new GeometryBuilder();
      final float x = 0.0f;
      final float y = 0.0f;
      final float width = 1.4f;
      final float height = 1.4f;
      builder.PushBackRectangle(x, y, width, height, 0.0f, 0.0f, 0.5f, 0.5f);
      vertexBufferOrienteeringDial = new VertexBuffer();
      vertexBufferOrienteeringDial.CreateFromMemory(builder.GetBuffer());
    }
    {
      GeometryBuilder builder = new GeometryBuilder();
      final float x = 0.0f;
      final float y = 0.0f;
      final float width = 1.4f;
      final float height = 1.4f;
      builder.PushBackRectangle(x, y, width, height, 0.0f, 0.5f, 0.5f, 1.0f);
      vertexBufferOrienteeringNeedle = new VertexBuffer();
      vertexBufferOrienteeringNeedle.CreateFromMemory(builder.GetBuffer());
    }
  }

  public void SetStyle(Settings.STYLE style)
  {
    mStyle = style;
  }

  public void SetDialAngle(float angle)
  {
    mDialAngle = angle;
  }

  public void SetCompassValues(float[] values)
  {
    mCompassValues = values;
  }

  public void onDrawFrame(GL10 glUnused) {
    // Ignore the passed-in GL10 interface, and use the GLES20
    // class's static methods instead.

    GLES20.glClearColor(100.0f / 255.0f, 149.0f / 255.0f, 237.0f / 255.0f, 1.0f);
    GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
    GLES20.glUseProgram(mProgram);
    checkGlError("glUseProgram");

    if (mStyle == Settings.STYLE.ORIENTEERING_WITH_MAP) {
      GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
      textureOrienteeringMap.Bind();

      vertexBufferOrienteeringMap.Bind();

      float[] mMMatrix = new float[16];
      Matrix.setIdentityM(mMMatrix, 0);

      Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mMMatrix, 0);
      Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);

      GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);

      vertexBufferOrienteeringMap.Render();

      vertexBufferOrienteeringMap.UnBind();

      GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
      textureOrienteeringMap.UnBind();
    }

    {
      GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
      textureOrienteering.Bind();

      {
        vertexBufferOrienteeringBody.Bind();

        float[] mMMatrix = new float[16];
        Matrix.setIdentityM(mMMatrix, 0);

        Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mMMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        vertexBufferOrienteeringBody.Render();

        vertexBufferOrienteeringBody.UnBind();
      }

      {
        vertexBufferOrienteeringDial.Bind();

        float[] mRotationMatrix = new float[16];
        Matrix.setIdentityM(mRotationMatrix, 0);
        final float fAngle = mDialAngle;
        Matrix.rotateM(mRotationMatrix, 0, fAngle, 0, 0, 1.0f);

        float[] mTranslationMatrix = new float[16];
        Matrix.setIdentityM(mTranslationMatrix, 0);
        final float y = -0.5f;
        Matrix.translateM(mTranslationMatrix, 0, 0.0f, y, 0.0f);

        float[] mMMatrix = new float[16];
        Matrix.multiplyMM(mMMatrix, 0, mTranslationMatrix, 0, mRotationMatrix, 0);

        Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mMMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        vertexBufferOrienteeringDial.Render();

        vertexBufferOrienteeringDial.UnBind();
      }

      {
        vertexBufferOrienteeringNeedle.Bind();

        float[] mRotationMatrix = new float[16];
        Matrix.setIdentityM(mRotationMatrix, 0);
        final float fAngle = -mCompassValues[0];
        Matrix.rotateM(mRotationMatrix, 0, fAngle, 0, 0, 1.0f);

        float[] mTranslationMatrix = new float[16];
        Matrix.setIdentityM(mTranslationMatrix, 0);
        final float y = -0.5f;
        Matrix.translateM(mTranslationMatrix, 0, 0.0f, y, 0.0f);

        float[] mMMatrix = new float[16];
        Matrix.multiplyMM(mMMatrix, 0, mTranslationMatrix, 0, mRotationMatrix, 0);

        Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mMMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        vertexBufferOrienteeringNeedle.Render();

        vertexBufferOrienteeringNeedle.UnBind();
      }

      GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
      textureOrienteering.UnBind();
    }
  }

  public void onSurfaceChanged(GL10 glUnused, int width, int height) {
    // Ignore the passed-in GL10 interface, and use the GLES20
    // class's static methods instead.
    GLES20.glViewport(0, 0, width, height);
    float ratio = (float) width / height;
    Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
  }

  public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
    // Ignore the passed-in GL10 interface, and use the GLES20
    // class's static methods instead.
    mProgram = createProgram(mVertexShader, mFragmentShader);
    if (mProgram == 0) return;

    maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
    checkGlError("glGetAttribLocation aPosition");
    if (maPositionHandle == -1) {
      throw new RuntimeException("Could not get attrib location for aPosition");
    }
    maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
    checkGlError("glGetAttribLocation aTextureCoord");
    if (maTextureHandle == -1) {
      throw new RuntimeException("Could not get attrib location for aTextureCoord");
    }

    muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
    checkGlError("glGetUniformLocation uMVPMatrix");
    if (muMVPMatrixHandle == -1) {
      throw new RuntimeException("Could not get attrib location for uMVPMatrix");
    }

    textureOrienteeringMap.CreateFromResource(R.raw.orienteering_map);
    textureOrienteering.CreateFromResource(R.raw.orienteering);

    Matrix.setLookAtM(mVMatrix, 0, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
  }

  private int loadShader(int shaderType, String source) {
    int shader = GLES20.glCreateShader(shaderType);
    if (shader != 0) {
      GLES20.glShaderSource(shader, source);
      GLES20.glCompileShader(shader);
      int[] compiled = new int[1];
      GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
      if (compiled[0] == 0) {
        Log.e(TAG, "Could not compile shader " + shaderType + ":");
        Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
        GLES20.glDeleteShader(shader);
        shader = 0;
      }
    }

    return shader;
  }

  private int createProgram(String vertexSource, String fragmentSource) {
    int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
    if (vertexShader == 0) return 0;

    int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
    if (pixelShader == 0) return 0;

    int program = GLES20.glCreateProgram();
    if (program != 0) {
      GLES20.glAttachShader(program, vertexShader);
      checkGlError("glAttachShader");
      GLES20.glAttachShader(program, pixelShader);
      checkGlError("glAttachShader");
      GLES20.glLinkProgram(program);
      int[] linkStatus = new int[1];
      GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
      if (linkStatus[0] != GLES20.GL_TRUE) {
        Log.e(TAG, "Could not link program: ");
        Log.e(TAG, GLES20.glGetProgramInfoLog(program));
        GLES20.glDeleteProgram(program);
        program = 0;
      }
    }

    return program;
  }

  private void checkGlError(String op) {
    int error;
    while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
      Log.e(TAG, op + ": glError " + error);
      throw new RuntimeException(op + ": glError " + error);
    }
  }

  Settings.STYLE mStyle;
  private float mDialAngle;
  private float[] mCompassValues;

  private static final int FLOAT_SIZE_BYTES = 4;
  private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
  private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
  private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;

  private final String mVertexShader =
    "uniform mat4 uMVPMatrix;\n" +
    "attribute vec4 aPosition;\n" +
    "attribute vec2 aTextureCoord;\n" +
    "varying vec2 vTextureCoord;\n" +
    "void main() {\n" +
    "  gl_Position = uMVPMatrix * aPosition;\n" +
    "  vTextureCoord = aTextureCoord;\n" +
    "}\n";

  private final String mFragmentShader =
    "precision mediump float;\n" +
    "varying vec2 vTextureCoord;\n" +
    "uniform sampler2D sTexture;\n" +
    "void main() {\n" +
    "  vec4 colour = texture2D(sTexture, vTextureCoord);" +
    "  if (colour.a < 0.1) discard;" +
    "  gl_FragColor = colour;\n" +
    "}\n";

  private float[] mMVPMatrix = new float[16];
  private float[] mProjMatrix = new float[16];
  private float[] mVMatrix = new float[16];

  private int mProgram;
  private int muMVPMatrixHandle;
  private Texture textureOrienteeringMap;
  private Texture textureOrienteering;
  private VertexBuffer vertexBufferOrienteeringMap;
  private VertexBuffer vertexBufferOrienteeringBody;
  private VertexBuffer vertexBufferOrienteeringDial;
  private VertexBuffer vertexBufferOrienteeringNeedle;
  private int maPositionHandle;
  private int maTextureHandle;

  private Context mContext;
  private static String TAG = "GLES20TriangleRenderer";
}
