package com.example.autoalert.utils;

public class SlopeComparator {


        private static final double THRESHOLD = 5;
        private static final double THRESHOLD_SPEED = 60;
        private static final double THRESHOLD_GYR = 50;



    public static boolean isAccidentDetectedAccelerometer(double previousSlope, double newSlope) {
        double difference = Math.abs(newSlope - previousSlope);
        double percentageDifference = difference  * 100;
        System.out.println("PORCENTAJE ACELEROMETRO: "+percentageDifference);
            return percentageDifference > THRESHOLD;
        }

    public static boolean isAccidentDetectedGyroscope(double previousSlope, double newSlope) {
        double difference = Math.abs(newSlope - previousSlope);
        double percentageDifference = (difference / Math.abs(previousSlope)) * 100;
        return percentageDifference > THRESHOLD_GYR;
    }

    public static  boolean isAccidentDetectedSpeed(double newSpeed, double previousSpeed){
        double speedChange = Math.abs(newSpeed - previousSpeed);
        return speedChange > THRESHOLD_SPEED;
    }















//    public static boolean isAccidentDetectedSpeed(double previousSlope, double newSlope) {
//        double difference = Math.abs(newSlope - previousSlope);
//        double percentageDifference = (difference / Math.abs(previousSlope)) * 100;
//        return percentageDifference > THRESHOLD_SPEED;
//    }
    }


