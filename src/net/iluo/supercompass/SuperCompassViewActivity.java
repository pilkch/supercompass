package net.iluo.supercompass;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


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


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
   MenuInflater inflater = getMenuInflater();
   inflater.inflate(R.menu.menu_options, menu);
   return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.style_orienteering:
      case R.id.style_orienteering_with_map:
        // TODO: IS THIS NEEDED?  CAN WE JUST USE THE VALUE OF item.isChecked() and not worry about item.setChecked()?
        item.setChecked(!item.isChecked());
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  /*@Override
  public void onGroupItemClick(MenuItem item) {
    // One of the group items (using the onClick attribute) was clicked
    // The item parameter passed here indicates which item it is
    // All other menu item clicks are handled by onOptionsItemSelected()
  }*/
}
