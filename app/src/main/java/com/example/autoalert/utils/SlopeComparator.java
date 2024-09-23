package com.example.autoalert.utils;

public class SlopeComparator {


        private static final double THRESHOLD = 0.001;
    private static final double THRESHOLD_SPEED = 0.0001;
    private static final double THRESHOLD_GYR = 0.0001;



    public static boolean isAccidentDetectedAccelerometer(double previousSlope, double newSlope) {
            double difference = Math.abs(newSlope - previousSlope);
            return difference > THRESHOLD;
        }

    public static boolean isAccidentDetectedGyroscope(double previousSlope, double newSlope) {
        double difference = Math.abs(newSlope - previousSlope);
        return difference > THRESHOLD_GYR;
    }

    public static boolean isAccidentDetectedSpeed(double previousSlope, double newSlope) {
        double difference = Math.abs(newSlope - previousSlope);
        return difference > THRESHOLD_SPEED;
    }
    }


