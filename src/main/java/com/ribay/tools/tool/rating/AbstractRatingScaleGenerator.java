package com.ribay.tools.tool.rating;

/**
 * Created by CD on 15.05.2016.
 */
public abstract class AbstractRatingScaleGenerator implements IRatingScaleGenerator {

    protected double[] generateTargetFunctionCoefficients(int nofRatings, double mediumScore) {
        int mediumScoreRound = (int) Math.round(mediumScore);

        double[] coefficients = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        for (int i = 0; i < factors.length; i++) {
            int factor = factors[i];
            int coefficientToSet1 = mediumScoreRound + i;
            int coefficientToSet2 = mediumScoreRound - i;

            if (coefficientToSet1 <= 10) {
                coefficients[coefficientToSet1 - 1] = factor;
            }
            if (coefficientToSet2 >= 1) {
                coefficients[coefficientToSet2 - 1] = factor;
            }
        }
        return coefficients;
    }

}
