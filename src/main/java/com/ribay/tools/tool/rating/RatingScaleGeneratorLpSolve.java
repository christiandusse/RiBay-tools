package com.ribay.tools.tool.rating;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by CD on 16.05.2016.
 */
public class RatingScaleGeneratorLpSolve extends AbstractRatingScaleGenerator {

    @Override
    public Number[] generateRatingScale(int nofRatings, double mediumScore) throws NotSolvableException {
        try {
            // See http://lpsolve.sourceforge.net/5.5/Java/README.html
            LpSolve solver = LpSolve.makeLp(8, 10);

            double[] targetFunctionCoefficients = generateTargetFunctionCoefficients(nofRatings, mediumScore);
            solver.strSetObjFn(toString(targetFunctionCoefficients));
            solver.setMaxim();

            solver.strAddConstraint(toString(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}), LpSolve.EQ, Math.round(nofRatings * mediumScore)); // constraint for getting mediumScore
            solver.strAddConstraint(toString(new double[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1}), LpSolve.EQ, nofRatings); // constraint for having no more ratings than the specified number of ratings
            solver.strAddConstraint(toString(new double[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0}), LpSolve.GE, Math.round(0.03 * nofRatings)); // number of absolute hate ratings (1) must be higher than 3%
            solver.strAddConstraint(toString(new double[]{1, 1, 0, 0, 0, 0, 0, 0, 0, 0}), LpSolve.GE, Math.round(0.05 * nofRatings)); // number of hate ratings (1 or 2) must be higher than 5%
            solver.strAddConstraint(toString(new double[]{1, 1, 1, 0, 0, 0, 0, 0, 0, 0}), LpSolve.GE, Math.round(0.10 * nofRatings)); // number of bad ratings (1, 2 or 3) must be higher than 10%
            solver.strAddConstraint(toString(new double[]{0, 0, 0, 0, 0, 0, 0, 1, 1, 1}), LpSolve.GE, Math.round(0.10 * nofRatings)); // number of good ratings (8, 9 or 10) must be higher than 10%
            solver.strAddConstraint(toString(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 1, 1}), LpSolve.GE, Math.round(0.05 * nofRatings)); // number of fan-boy ratings (9 or 10) must be higher than 5%
            solver.strAddConstraint(toString(new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 1}), LpSolve.GE, Math.round(0.03 * nofRatings)); // number of absolute fan-boy ratings (10) must be higher than 3%
            for (int i = 0; i < 10; i++) {
                solver.setInt(i + 1, true);
            }

            solver.setVerbose(LpSolve.FALSE); // do not print full solution with details
            solver.solve();

            double[] calcResult = solver.getPtrVariables();

            Number[] result = new Number[calcResult.length];
            for (int i = 0; i < calcResult.length; i++) {
                result[i] = calcResult[i];
            }
            return result;
        } catch (LpSolveException e) {
            throw new NotSolvableException(e);
        }
    }

    private String toString(double[] coefficients) {
        return StringUtils.join(coefficients, ' ');
    }

}
