/*
 * Copyright 2003-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.math.stat.univariate.moment;

import java.io.Serializable;

import org.apache.commons.math.stat.univariate.AbstractStorelessUnivariateStatistic;

/**
 * Computes the (unbiased) sample variance.  Uses the definitional formula: 
 * <p>
 * variance = sum((x_i - mean)^2) / (n - 1)
 * <p>
 * where mean is the {@link Mean} and <code>n</code> is the number
 * of sample observations.  
 * <p>
 * The definitional formula does not have good numerical properties, so
 * this implementation uses updating formulas based on West's algorithm
 * as described in <a href="http://doi.acm.org/10.1145/359146.359152">
 * Chan, T. F. andJ. G. Lewis 1979, <i>Communications of the ACM</i>,
 * vol. 22 no. 9, pp. 526-531.</a>.
* <p>
 * <strong>Note that this implementation is not synchronized.</strong> If 
 * multiple threads access an instance of this class concurrently, and at least
 * one of the threads invokes the <code>increment()</code> or 
 * <code>clear()</code> method, it must be synchronized externally.
 * 
 * @version $Revision: 1.26 $ $Date: 2004/07/11 18:38:12 $
 */
public class Variance extends AbstractStorelessUnivariateStatistic implements Serializable {

    /** Serializable version identifier */
    static final long serialVersionUID = -9111962718267217978L;  
      
    /** SecondMoment is used in incremental calculation of Variance*/
    protected SecondMoment moment = null;

    /**
     * Boolean test to determine if this Variance should also increment
     * the second moment, this evaluates to false when this Variance is
     * constructed with an external SecondMoment as a parameter.
     */
    protected boolean incMoment = true;

    /**
     * Constructs a Variance.
     */
    public Variance() {
        moment = new SecondMoment();
    }

    /**
     * Constructs a Variance based on an external second moment.
     * @param m2 the SecondMoment (Thrid or Fourth moments work
     * here as well.)
     */
    public Variance(final SecondMoment m2) {
        incMoment = false;
        this.moment = m2;
    }
    /**
     * @see org.apache.commons.math.stat.univariate.StorelessUnivariateStatistic#increment(double)
     */
    public void increment(final double d) {
        if (incMoment) {
            moment.increment(d);
        }
    }

    /**
     * @see org.apache.commons.math.stat.univariate.StorelessUnivariateStatistic#getResult()
     */
    public double getResult() {
            if (moment.n == 0) {
                return Double.NaN;
            } else if (moment.n == 1) {
                return 0d;
            } else {
                return moment.m2 / ((double) moment.n - 1d);
            }
    }

    /**
     * @see org.apache.commons.math.stat.univariate.StorelessUnivariateStatistic#getN()
     */
    public long getN() {
        return moment.getN();
    }
    
    /**
     * @see org.apache.commons.math.stat.univariate.StorelessUnivariateStatistic#clear()
     */
    public void clear() {
        if (incMoment) {
            moment.clear();
        }
    }
    
    /**
     * Returns the variance of the entries in the input array, or 
     * <code>Double.NaN</code> if the array is empty.
     * <p>
     * See {@link Variance} for details on the computing algorithm.
     * <p>
     * Returns 0 for a single-value (i.e. length = 1) sample.
     * <p>
     * Throws <code>IllegalArgumentException</code> if the array is null.
     * <p>
     * Does not change the internal state of the statistic.
     * 
     * @param values the input array
     * @return the variance of the values or Double.NaN if length = 0
     * @throws IllegalArgumentException if the array is null
     */
    public double evaluate(final double[] values) {
        if (values == null) {
            throw new IllegalArgumentException("input values array is null");
        }
        return evaluate(values, 0, values.length);
    }

    /**
     * Returns the variance of the entries in the specified portion of
     * the input array, or <code>Double.NaN</code> if the designated subarray
     * is empty.
     * <p>
     * See {@link Variance} for details on the computing algorithm.
     * <p>
     * Returns 0 for a single-value (i.e. length = 1) sample.
     * <p>
     * Does not change the internal state of the statistic.
     * <p>
     * Throws <code>IllegalArgumentException</code> if the array is null.
     * 
     * @param values the input array
     * @param begin index of the first array element to include
     * @param length the number of elements to include
     * @return the variance of the values or Double.NaN if length = 0
     * @throws IllegalArgumentException if the array is null or the array index
     *  parameters are not valid
     */
    public double evaluate(final double[] values, final int begin, final int length) {

        double var = Double.NaN;

        if (test(values, begin, length)) {
            clear();
            if (length == 1) {
                var = 0.0;
            } else if (length > 1) {
                Mean mean = new Mean();
                double m = mean.evaluate(values, begin, length);
                var = evaluate(values, m, begin, length);
            }
        }
        return var;
    }
    
    /**
     * Returns the variance of the entries in the specified portion of
     * the input array, using the precomputed mean value.  Returns 
     * <code>Double.NaN</code> if the designated subarray is empty.
     * <p>
     * See {@link Variance} for details on the computing algorithm.
     * <p>
     * Returns 0 for a single-value (i.e. length = 1) sample.
     * <p>
     * Throws <code>IllegalArgumentException</code> if the array is null.
     * <p>
     * Does not change the internal state of the statistic.
     * 
     * @param values the input array
     * @param mean the precomputed mean value
     * @param begin index of the first array element to include
     * @param length the number of elements to include
     * @return the variance of the values or Double.NaN if length = 0
     * @throws IllegalArgumentException if the array is null or the array index
     *  parameters are not valid
     */
    public double evaluate(final double[] values, final double mean, 
            final int begin, final int length) {
        
        double var = Double.NaN;

        if (test(values, begin, length)) {
            if (length == 1) {
                var = 0.0;
            } else if (length > 1) {
                double accum = 0.0;
                double accum2 = 0.0;
                for (int i = begin; i < begin + length; i++) {
                    accum += Math.pow((values[i] - mean), 2.0);
                    accum2 += (values[i] - mean);
                }
                var = (accum - (Math.pow(accum2, 2) / ((double) length))) /
                (double) (length - 1);
            }
        }
        return var;
    }
    
    /**
     * Returns the variance of the entries in the input array, using the
     * precomputed mean value.  Returns <code>Double.NaN</code> if the array
     * is empty.
     * <p>
     * See {@link Variance} for details on the computing algorithm.
     * <p>
     * Returns 0 for a single-value (i.e. length = 1) sample.
     * <p>
     * Throws <code>IllegalArgumentException</code> if the array is null.
     * <p>
     * Does not change the internal state of the statistic.
     * 
     * @param values the input array
     * @param mean the precomputed mean value
     * @return the variance of the values or Double.NaN if the array is empty
     * @throws IllegalArgumentException if the array is null
     */
    public double evaluate(final double[] values, final double mean) {
        if (values == null) {
            throw new IllegalArgumentException("input values array is null");
        }
        return evaluate(values, mean, 0, values.length);
    }

}
