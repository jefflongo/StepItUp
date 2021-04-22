package com.ece150.stepitup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener  {

    private static final String TAG = "StepItUp";
    private static final int STEP_GOAL_INVALID = -1;

    private int mStepCounter = 0;
    private long mStartTime = 0;
    private int mTotalSteps = 0;
    private float mStepsHr = 0;
    private int mGoalsCompleted = 0;

    private int mStepGoal = STEP_GOAL_INVALID;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer = null;

    // ECE 251 ONLY
    private Sensor mMagnetometer = null;
    private float[] mLastAccelerometerReading = new float[3];
    private float[] mLastMagnetometerReading = new float[3];
    private float[] mOrientation = new float[3];
    private float[] mRotationMatrix = new float[9];
    // ------------

    private StepDetector mStepDetector = new StepDetector();

    private EditText mStepGoalEditText;
    private TextView mTotalStepsTextView;
    private TextView mStepsHrTextView;
    private TextView mGoalsCompletedTextView;
    // ECE 251 ONLY
    private ImageView mCompassImageView;
    // ------------


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStepGoalEditText = (EditText) findViewById(R.id.stepGoalEditText);
        mTotalStepsTextView = (TextView) findViewById(R.id.totalStepsValueTextView);
        mStepsHrTextView = (TextView) findViewById(R.id.stepsHrValueTextView);
        mGoalsCompletedTextView = (TextView) findViewById(R.id.goalsCompletedValueTextView);
        // ECE 251 ONLY
        mCompassImageView = (ImageView) findViewById(R.id.compassImageView);
        // ------------

        if (savedInstanceState != null) {
            mStepCounter = savedInstanceState.getInt("stepCounter");
            Log.d(TAG, "step counter restored: " + mStepCounter);
            mStartTime = savedInstanceState.getLong("startTime");
            Log.d(TAG, "start time restored: " + mStartTime);
            mStepGoal = savedInstanceState.getInt("stepGoal");
            mStepGoalEditText.setText(String.valueOf(mStepGoal));
            Log.d(TAG, "step goal restored: " + mStepGoal);
            mTotalSteps = savedInstanceState.getInt("totalSteps");
            mTotalStepsTextView.setText(String.valueOf(mTotalSteps));
            Log.d(TAG, "total steps restored: " + mTotalSteps);
            mStepsHr = savedInstanceState.getFloat("stepsHr");
            mTotalStepsTextView.setText(String.valueOf(mStepsHr));
            Log.d(TAG, "steps/hr restored: " + mStepsHr);
            mGoalsCompleted = savedInstanceState.getInt("goalsCompleted");
            mGoalsCompletedTextView.setText(String.valueOf(mGoalsCompleted));
            Log.d(TAG, "goals completed restored: " + mGoalsCompleted);
        } else {
            Log.d(TAG, "savedInstanceState was null");
            mStepCounter = 0;
            mStepGoal = STEP_GOAL_INVALID;
            mTotalSteps = 0;
            mStepsHr = 0;
            mGoalsCompleted = 0;
            mStartTime = System.currentTimeMillis();
        }

        Handler handler = new Handler();
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                long elapsedMillis = System.currentTimeMillis() - mStartTime;
                mStepsHr = 1000 * 60 * 60 * mTotalSteps / (float) elapsedMillis;
                mStepsHrTextView.setText(String.valueOf(mStepsHr));
                handler.postDelayed(this, 1000);
            }
        };

        handler.postDelayed(r, 1000);

        final Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                try {
                    mStepGoal = Integer.parseInt(mStepGoalEditText.getText().toString());
                    mStepCounter = mStepGoal;
                    Log.d(TAG, "save pressed - step goal set to " + mStepGoal);
                } catch (NumberFormatException e) {
                    mStepGoal = STEP_GOAL_INVALID;
                    mStepCounter = 0;
                    Log.e(TAG, "save pressed - invalid text in step goal text box");
                }
                mStepGoalEditText.clearFocus();
                imm.hideSoftInputFromWindow(mStepGoalEditText.getWindowToken(), 0);
            }
        });

        final Button restartButton = (Button) findViewById(R.id.restartButton);
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (mStepGoal != STEP_GOAL_INVALID) {
                    mStepCounter = mStepGoal;
                    mStepGoalEditText.setText(String.valueOf(mStepGoal));
                    Log.d(TAG, "restart pressed - step counter set to " + mStepGoal);
                } else {
                    Log.d(TAG, "restart pressed - step goal was invalid");
                }
                mStepGoalEditText.clearFocus();
                imm.hideSoftInputFromWindow(mStepGoalEditText.getWindowToken(), 0);
            }
        });

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (mAccelerometer != null) {
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
        // ECE 251 ONLY
        if (mMagnetometer != null) {
            mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
        }
        // ------------
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("stepCounter", mStepCounter);
        Log.d(TAG, "step counter saved: " + mStepCounter);
        outState.putLong("startTime", mStartTime);
        Log.d(TAG, "start time saved: " + mStartTime);
        outState.putInt("stepGoal", mStepGoal);
        Log.d(TAG, "step goal saved: " + mStepGoal);
        outState.putInt("totalSteps", mTotalSteps);
        Log.d(TAG, "total steps saved: " + mTotalSteps);
        outState.putFloat("stepsHr", mStepsHr);
        Log.d(TAG, "steps/hr saved: " + mStepsHr);
        outState.putInt("goalsCompleted", mGoalsCompleted);
        Log.d(TAG, "goals completed saved: " + mGoalsCompleted);
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // ECE 251 ONLY
            mLastAccelerometerReading = event.values;
            handleCompass();
            // ------------
            if (mStepDetector.detectStep(event.values[0], event.values[1], event.values[2])) {
                handleStep();
            }
        }
        // ECE 251 ONLY
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mLastMagnetometerReading = event.values;
            handleCompass();
        }
        // ------------
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void sendNotification(String text) {
        Intent intent = getIntent();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "notify")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("StepItUp")
                .setContentText(text)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "notify";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Notification request channel",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }

        notificationManager.notify(0, builder.build());
    }

    private void handleStep() {
        Log.d(TAG, "detected step");
        // update the total step count
        mTotalSteps++;
        mTotalStepsTextView.setText(String.valueOf(mTotalSteps));
        // decrement the step counter if a goal is active
        if (mStepCounter > 0) {
            mStepCounter--;
            if (!mStepGoalEditText.hasFocus()) {
                mStepGoalEditText.setText(String.valueOf(mStepCounter));
            }
            // check for goal complete
            if (mStepCounter == 0) {
                mGoalsCompleted++;
                mGoalsCompletedTextView.setText(String.valueOf(mGoalsCompleted));
                sendNotification("Goal completed!");
            }
        }
    }

    private void handleCompass() {
        SensorManager.getRotationMatrix(mRotationMatrix, null, mLastAccelerometerReading, mLastMagnetometerReading);
        SensorManager.getOrientation(mRotationMatrix, mOrientation);
        mCompassImageView.setRotation((float)(-mOrientation[0]*180/3.14159));
    }
}
