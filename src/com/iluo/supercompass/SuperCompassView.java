package com.iluo.supercompass;

import android.content.Context;
import android.opengl.GLSurfaceView;

class SuperCompassView extends GLSurfaceView {
  private GLES20TriangleRenderer mRenderer;

  public SuperCompassView(Context context) {
    super(context);
    setEGLContextClientVersion(2);
    mRenderer = new GLES20TriangleRenderer(context);
    setRenderer(mRenderer);
  }

  public void SetCompassValues(float[] values)
  {
    mRenderer.SetCompassValues(values);
  }
}
