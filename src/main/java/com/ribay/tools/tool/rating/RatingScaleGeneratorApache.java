package com.ribay.tools.tool.rating;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by CD on 15.05.2016.
 */
public class RatingScaleGeneratorApache extends AbstractRatingScaleGenerator {

    public Number[] generateRatingScale(int nofRatings, double mediumScore) {

        // This does not solve integer problems - can only solve problems with real numbers as solution

        int mediumScoreRound = (int) Math.round(mediumScore);

        double[] coefficients = generateTargetFunctionCoefficients(nofRatings, mediumScore);
        LinearObjectiveFunction maxFunction = new LinearObjectiveFunction(coefficients, 0);

        Collection<LinearConstraint> constraints = new ArrayList<>();
        constraints.add(new LinearConstraint(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, Relationship.EQ, nofRatings * mediumScore)); // constraint for getting mediumScore
        constraints.add(new LinearConstraint(new double[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, Relationship.EQ, nofRatings)); // constraint for having no more ratings than the specified number of ratings
        constraints.add(new LinearConstraint(new double[]{1, 1, 1, 0, 0, 0, 0, 0, 0, 0}, Relationship.GEQ, 0.10 * nofRatings)); // number of bad ratings (1, 2 or 3) must be higher than 10%
        constraints.add(new LinearConstraint(new double[]{1, 1, 0, 0, 0, 0, 0, 0, 0, 0}, Relationship.GEQ, 0.05 * nofRatings)); // number of hate ratings (1 or 2) must be higher than 5%
        constraints.add(new LinearConstraint(new double[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0}, Relationship.GEQ, 0.03 * nofRatings)); // number of absolute hate ratings (1) must be higher than 3%
        constraints.add(new LinearConstraint(new double[]{0, 0, 0, 0, 0, 0, 0, 1, 1, 1}, Relationship.GEQ, 0.10 * nofRatings)); // number of good ratings (8, 9 or 10) must be higher than 10%
        constraints.add(new LinearConstraint(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 1, 1}, Relationship.GEQ, 0.05 * nofRatings)); // number of fan-boy ratings (9 or 10) must be higher than 5%
        constraints.add(new LinearConstraint(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, Relationship.GEQ, 0.03 * nofRatings)); // number of absolute fan-boy ratings (10) must be higher than 3%

        for (int i = 0; i < minValues.length; i++) {
            double minValue = minValues[i];
            int coefficientToSet1 = mediumScoreRound + i;
            int coefficientToSet2 = mediumScoreRound - i;

            if (coefficientToSet1 <= 10) {
                ArrayRealVector v = new ArrayRealVector(10);
                v.setEntry(coefficientToSet1 - 1, 1);
                constraints.add(new LinearConstraint(v, Relationship.GEQ, minValue * nofRatings)); // min value for rating near medium value must be set
            }
            if (coefficientToSet2 >= 1) {
                ArrayRealVector v = new ArrayRealVector(10);
                v.setEntry(coefficientToSet2 - 1, 1);
                constraints.add(new LinearConstraint(v, Relationship.GEQ, minValue * nofRatings)); // min value for rating near medium value must be set
            }
        }

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(new MaxIter(100), maxFunction, new LinearConstraintSet(constraints), GoalType.MAXIMIZE, new NonNegativeConstraint(true));
        double[] calcResult = solution.getKey();

        Number[] result = new Number[10];
        for (int i = 0; i < 10; i++) {
            result[i] = calcResult[i];
        }

        return result;
    }

}
