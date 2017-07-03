package com.kviation.sample.orientation;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements LifecycleRegistryOwner {

  private LifecycleRegistry mLifecycleRegistry = new LifecycleRegistry(this);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    final AttitudeIndicator aiView = (AttitudeIndicator) findViewById(R.id.attitude_indicator);

    Orientation orientation = new Orientation(this, new Orientation.Listener() {
      @Override
      public void onOrientationChanged(float pitch, float roll) {
        aiView.setAttitude(pitch, roll);
      }
    });
    getLifecycle().addObserver(orientation);
  }

  @Override
  public LifecycleRegistry getLifecycle() {
    return mLifecycleRegistry;
  }
}
