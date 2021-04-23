package com.ece150.stepitup;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity implements SensorEventListener  {
    private StepDetector mStepDetector = new StepDetector();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // [TODO] Setup button behavior
        // [TODO] Create a thread to calculate steps/hr
        // [TODO] Initialize UI elements
        // [TODO] Request ACTIVITY_RECOGNITION permission
        // [TODO] Initialize accelerometer and step counter sensors
        // [TODO ECE 251 ONLY] Initialize magnetometer
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        // [TODO] Handle the raw data. Hint: Provide data to the step detector, call `handleStep` if step detected
        // [TODO] Update UI elements
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Unused
    }

    private void sendNotification(String text) {
        // [TODO] Implement notification
    }

    private void handleStep() {
        // [TODO] Update state / UI on step detected
    }
}
