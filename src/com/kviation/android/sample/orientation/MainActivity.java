
package com.kviation.android.sample.orientation;

import android.app.Activity;
import android.hardware.SensorManager;
import android.os.Bundle;

public class MainActivity extends Activity implements Orientation.Listener {

  private Orientation mOrientation;
  private AttitudeIndicator mAttitudeIndicator;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    mOrientation = new Orientation((SensorManager) getSystemService(Activity.SENSOR_SERVICE),
        getWindow().getWindowManager());
    mAttitudeIndicator = (AttitudeIndicator) findViewById(R.id.attitude_indicator);
  }

  @Override
  protected void onResume() {
    super.onResume();
    mOrientation.startListening(this);
  }

  @Override
  protected void onPause() {
    super.onPause();
    mOrientation.stopListening();
  }

  @Override
  public void onOrientationChanged(float pitch, float roll) {
    mAttitudeIndicator.setAttitude(pitch, roll);
  }
}
