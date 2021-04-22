package com.ece150.stepitup;

public class StepDetector {

    public StepDetector() { }

    public boolean detectStep(float x, float y, float z) {
        double sample = Math.sqrt(x*x + y*y + z*z);

        // [TODO] determine if a step occurred

        return false;
    }
}
