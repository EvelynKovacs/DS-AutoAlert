package com.example.autoalert;

import org.junit.Test;
import static org.junit.Assert.*;
import com.example.autoalert.utils.AccidentDetector;

public class AngleCalculatorTest {

    @Test
    public void testCalculateAngleBetweenLines_NormalCase() {
        // Hardcode some coordinates
        double cx1 = -42.789351;
        double cy1 = -65.019023;
        double cx2 = -42.789256;
        double cy2 = -65.018722;
        double cx3 = -42.789162;
        double cy3 = -65.018765;

//        double cx1 =  -42.790138;
//        double cy1 = -65.028550;
//        double cx2=  -42.790138;
//        double cy2=-65.027992;
//        double cx3 = -42.789508;
//        double cy3 =  -65.027992;

//


        // Calculate the slopes
        double slope1 = AccidentDetector.calculateSlope(cx1, cy1, cx2, cy2); //3,168421053
        double slope2 = AccidentDetector.calculateSlope(cx2, cy2, cx3, cy3);//−0,457446809

        // Call the method from AccidentDetector
        double angle = AccidentDetector.calculateAngleBetweenLines(slope1, slope2); //

        // Assert the result is reasonable
        System.out.println("Calculated angle: " + angle);
        assertTrue("Angle is too small or too large", angle >= 0 && angle <= 90);
    }
}
