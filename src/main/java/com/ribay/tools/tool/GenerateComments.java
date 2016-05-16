package com.ribay.tools.tool;

import com.ribay.tools.tool.rating.IRatingScaleGenerator;
import com.ribay.tools.tool.rating.RatingScaleGeneratorLpSolve;

import java.util.Arrays;

/**
 * Created by CD on 15.05.2016.
 */
public class GenerateComments {

    public static void main(String[] args) throws Exception {
        IRatingScaleGenerator generator;
        Number[] result;

        /*
        generator = new RatingScaleGeneratorApache();
        result = generator.generateRatingScale(238, 7.1);
        System.out.println(Arrays.toString(result));
        */

        generator = new RatingScaleGeneratorLpSolve();
        result = generator.generateRatingScale(238, 7.1);
        System.out.println(Arrays.toString(result));

    }

}
