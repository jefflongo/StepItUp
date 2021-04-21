package com.ece150.stepitup;

import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

import static java.lang.Math.abs;

public class StepDetector {
    private static final String TAG = "StepItUp";

    private static final int MIN_SAMPLES = 10;
    private static final int MAX_SAMPLES = 1000;

    // with a sample every ~20ms, ignore at least 250ms worth of samples = ~13 samples
    private static final int IGNORE_SAMPLES = 13;

    // empirically tested value
    private static final double THRESHOLD = 5;

    private static int mSamplesToIgnore = 0;

    private final Queue<Double> mSamples = new LinkedList<>();

    public StepDetector() { }

    public boolean detectStep(float x, float y, float z) {
        double sample = Math.sqrt(x*x + y*y + z*z);

        // ensure there's enough samples to compare against a sane average
        if (mSamples.size() < MIN_SAMPLES) {
            mSamples.add(sample);
            return false;
        }

        double mean = getMean();
        Log.v(TAG, "sample: " + sample + " mean: " + mean + " diff: " + abs(sample - mean));

        boolean ret = false;
        // only allow a "true" return if enough time has elapsed to where there's definitely been another step
        if (mSamplesToIgnore == 0) {
            if (abs(sample - mean) > THRESHOLD) {
                mSamplesToIgnore = IGNORE_SAMPLES;
                ret = true;
            }
        } else {
            mSamplesToIgnore--;
        }

        // remove stale data
        if (mSamples.size() > MAX_SAMPLES) {
            mSamples.remove();
        }
        mSamples.add(sample);

        return ret;
    }

    private double getMean() {
        if (mSamples.size() == 0) {
            return 0;
        }

        double mean = 0;
        for (double sample : mSamples) {
            mean += sample;
        }
        mean /= mSamples.size();

        return mean;
    }
}
