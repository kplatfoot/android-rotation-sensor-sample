package com.kviation.sample.orientation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    AttitudeIndicator aiView = findViewById(R.id.attitude_indicator);

    getLifecycle().addObserver(new Orientation(this,
            (pitch, roll) -> aiView.setAttitude(pitch, roll)));
  }
}
