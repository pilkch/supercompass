package com.iluo.supercompass;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;


public class SuperCompassViewActivity extends Activity {

  private static final String TAG = "Compass";

  private SensorManager mSensorManager;
  private Sensor mSensor;

  private SuperCompassView mView;

  private final SensorEventListener mListener = new SensorEventListener() {
    public void onSensorChanged(SensorEvent event) {
      if (Build.debug) Log.d(TAG, "sensorChanged (" + event.values[0] + ", " + event.values[1] + ", " + event.values[2] + ")");
      if (mView != null) mView.SetCompassValues(event.values);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
  };

  @Override
  protected void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    mView = new SuperCompassView(getApplication());
    setContentView(mView);
    mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
    mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
  }

  @Override
  protected void onPause() {
    super.onPause();
    mSensorManager.unregisterListener(mListener);
    mView.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mView.onResume();
    mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_GAME);
  }

  @Override
  protected void onStop()
  {
    //mView.onStop();
    mSensorManager.unregisterListener(mListener);
    super.onStop();
  }
}
