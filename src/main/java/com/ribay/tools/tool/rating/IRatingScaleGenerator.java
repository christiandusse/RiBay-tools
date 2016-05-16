package com.ribay.tools.tool.rating;

/**
 * Created by CD on 15.05.2016.
 */
public interface IRatingScaleGenerator {

    static final int[] factors = {100, 85, 60, 40, 25, 15, 10, 5};
    static final double[] minValues = {0.20, 0.10, 0.05};

    /**
     * requires: nofRatings > 0
     * requires: 1 <= mediumScore <= 10
     *
     * @param nofRatings
     * @param mediumScore
     */
    public Number[] generateRatingScale(int nofRatings, double mediumScore) throws NotFeasibleException, Exception;

    public static class NotFeasibleException extends Exception {

        public NotFeasibleException() {
            super();
        }

        public NotFeasibleException(Throwable cause) {
            super(cause);
        }

    }

}
