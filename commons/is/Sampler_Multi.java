/*
 * Copyright 2010-2014 Susanta Tewari. <statsusant@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package commons.is;

import bd.org.apache.commons.math.stat.descriptive.SummaryStatistics;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import commons.core.Singletons;
import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;

import static commons.core.Singletons.MATHCONTEXT_128_HALF_UP;

/**
 * Sampler for <a href="http://en.wikipedia.org/wiki/Importance_sampling">Importance Sampling</a>.
 * It implements the formula:
 * <pre>
 *   E[h(X)] = Average [ h(X)&times;P(X)/Q(X) ]
 *   P(X) = target distribution (X->R)
 *   Q(X) = proposal distribution (X->R)
 *   h(X) = target function (X->R); e.g., for computing probability it can be the conditional
 *          probability of data (D) given X under the target distribution i.e., P(D|X).
 * </pre>
 *
 * @param <X> domain of sampling
 * @author Susanta Tewari
 * @version 1.4.2
 * @history First created on 03/07/2012 (derived from ImportanceSampling, 06/02/2011).
 * @since 1.4.2
 */
public class Sampler_Multi<X> {

    private final String name;
    private final Proposal_Multi<X> proposal;
    private final Factor_Multi<X> factor;
    private final SummaryStatistics[] stat;

    /* optimization */
    private final int size;

    /* mean function; function whose mean is being estimated using this sampler */
    private final Function<X, BigDecimal> meanFunction;

    /**
     * Sets the default {@code Factor} (ratio of target to proposal). If an efficient {@code
     * Factor} is available, it should be set using {@link #setISFactor(Factor)}.
     *
     * @param name name for this sampler; used for ID purposes, need not be unique
     * @param proposal proposal distribution
     * @param factor IS Factor
     * @param meanFunction function whose mean is being estimated using this sampler
     */
    public Sampler_Multi(final String name, final Proposal_Multi<X> proposal,
                         final Factor_Multi<X> factor, final Function<X, BigDecimal> meanFunction) {


        // null
        Objects.requireNonNull(name);
        Objects.requireNonNull(proposal);
        Objects.requireNonNull(factor);
        Objects.requireNonNull(meanFunction);

        this.name         = name;
        this.factor       = factor;
        this.proposal     = proposal;
        this.meanFunction = meanFunction;
        size              = factor.size();
        stat              = new SummaryStatistics[size];

        for (int i = 0; i < size; i++) {
            stat[i] = new SummaryStatistics();
        }
    }

    /**
     * @return the name of this sampler
     */
    public String getName() {
        return name;
    }

    /**
     * Total number of times {@link #next()} invoked.
     *
     * @return total number of samples drawn so far.
     */
    public long nowSampleSize() {
        return stat[0].getN();
    }

    /**
     * Current estimate (average) based on the samples collected so far. Uses 128 digits of
     * precision and rounding mode {@link java.math.RoundingMode#HALF_UP}. It uses fixed digits of precision
     * to avoid non-terminating decimal expansion for inexact computations; e.g., when dividing 1 by
     * 3.
     *
     * @return the current estimate
     */
    public BigDecimal[] nowMean() {

        final BigDecimal[] result = new BigDecimal[size];

        if (isWaitingOnSample(result)) return result;

        for (int i = 0; i < size; i++) {
            result[i] = stat[i].getMean();
        }

        return result;
    }

    /**
     * @return current standard error for {@link #nowMean()}.
     */
    public BigDecimal[] nowStdError() {

        final BigDecimal[] result = new BigDecimal[size];

        if (isWaitingOnSample(result)) return result;

        final BigDecimal[] variance = nowVariance();

        for (int i = 0; i < size; i++) {

            final BigDecimal meanVar = variance[i].divide(new BigDecimal(nowSampleSize()),
                                           MATHCONTEXT_128_HALF_UP);
            final Apfloat val = ApfloatMath.sqrt(new Apfloat(meanVar));

            result[i] = new BigDecimal(val.toString());
        }

        return result;
    }

    /**
     * @return current value of effective sample size (ESS)
     */
    public BigDecimal[] nowESS() {

        final BigDecimal[] result = new BigDecimal[size];

        if (isWaitingOnSample(result)) return result;

        final BigDecimal[] covSquare = nowCOVSquare();

        for (int i = 0; i < size; i++) {

            final BigDecimal variancePlusOne = covSquare[i].add(BigDecimal.ONE);

            result[i] = new BigDecimal(nowSampleSize()).divide(variancePlusOne,
                                       Singletons.MATHCONTEXT_128_HALF_UP);
        }

        return result;
    }

    private boolean waitOnSampleOver = false;

    private boolean isWaitingOnSample(BigDecimal[] result) {

        if (waitOnSampleOver) return false;

        if (countMinSample() == 0) {

            Arrays.fill(result, BigDecimal.ZERO);

            return true;

        } else {

            waitOnSampleOver = true;

            return false;
        }
    }

    private long countMinSample() {

        long result = 1;

        for (int i = 0; i < size; i++) {
            result = Math.min(result, stat[i].getN());
        }

        return result;
    }

    /**
     * @return variance
     */
    private BigDecimal[] nowVariance() {

        final BigDecimal[] result = new BigDecimal[size];

        if (isWaitingOnSample(result)) return result;

        for (int i = 0; i < size; i++) {
            result[i] = new BigDecimal(stat[i].getVariance());
        }

        return result;
    }

    /**
     * Returns the square of COV (coefficient of variation). It is variance divided by mean square.
     *
     * @return the square of COV
     */
    private BigDecimal[] nowCOVSquare() {

        final BigDecimal[] result = new BigDecimal[size];

        if (isWaitingOnSample(result)) return result;

        final BigDecimal[] mean     = nowMean();
        final BigDecimal[] variance = nowVariance();

        for (int i = 0; i < size; i++) {
            result[i] = variance[i].divide(mean[i].pow(2), Singletons.MATHCONTEXT_128_HALF_UP);
        }

        return result;
    }

    private Iterator_IS itr;

    private void setIterator(Iterator_IS itr) {
        this.itr = itr;
    }

    public double getFractionCompleted() {
        return itr.getFractionCompleted();
    }

    /**
     * @param sampleSize number of importance samples to draw
     * @return itself by conforming to builder pattern
     */
    public Sampler_Multi setIteratorBySampleSize(final long sampleSize) {

        setIterator(new Iterator_IS() {

            @Override
            public boolean hasNextImpl() {
                return nowSampleSize() < sampleSize;
            }
            @Override
            double getFractionCompleted() {
                return (double) nowSampleSize() / sampleSize;
            }

        });

        return this;
    }

    /**
     * @param time duration (in milliseconds) of the sampler since {@link #start()} invoked.
     * @return itself by conforming to builder pattern
     */
    public Sampler_Multi setIteratorByTime(final int time) {

        setIterator(Iterator_IS.of_Time(time));

        return this;
    }

    /**
     *
     * @throws InterruptedException if the current thread is interrupted while the sampler is
     * running
     */
    public void run() throws InterruptedException {

        start();

        while (hasNext()) {

            next();

            if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
        }
    }

    private void start() {
        itr.start();
    }

    private boolean hasNext() {
        return itr.hasNext();
    }

    /**
     * Takes one more important sample and updates the calculation.
     */
    public void next() {

        if (!hasNext()) throw new IllegalStateException("hasNext false");

        final X[] x                      = proposal.sample();
        final BigDecimal[] iSFactorValue = factor.getValue(x);

        for (int i = 0; i < size; i++) {

            final BigDecimal augend = iSFactorValue[i].multiply(meanFunction.apply(x[i]));

            stat[i].addValue(augend);
        }
    }

    @VisibleForTesting
    Factor_Multi<X> getFactor() {
        return factor;
    }
}
