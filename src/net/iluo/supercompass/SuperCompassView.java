package net.iluo.supercompass;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

class SuperCompassView extends GLSurfaceView {
  private GLES20TriangleRenderer mRenderer;

  public SuperCompassView(Context context) {
    super(context);
    setEGLContextClientVersion(2);
    mRenderer = new GLES20TriangleRenderer(context);
    setRenderer(mRenderer);
  }

  public void SetStyle(Settings.STYLE style)
  {
    mRenderer.SetStyle(style);
  }

  public void SetCompassValues(float[] values)
  {
    mRenderer.SetCompassValues(values);
  }

  public boolean onTouchEvent(final MotionEvent event)
  {
    queueEvent(new Runnable() {
        public void run() {
          // No idea where the magic number comes from
          // With the magic number the rotation is correct on a Galaxy S II, no idea what it looks like with other devices
          final float fMagicNumber = 20.0f;
          final float angle = ((360.0f + fMagicNumber) * ((event.getX() - (0.5f * getWidth())) / getWidth()));
          mRenderer.SetDialAngle(angle);
        }
      }
    );

    return true;
  }
}
