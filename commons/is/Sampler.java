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
import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import static commons.core.Singletons.MATHCONTEXT_128_HALF_UP;

/**
 * Sampler for <a href="http://en.wikipedia.org/wiki/Importance_sampling">Importance Sampling</a>.
 * It implements the formula:
 *
 * <pre>
 *
 *   E[h(X)] = Average [ h(X)&times;P(X)/Q(X) ]
 *   P(X) = target distribution (X->R)
 *   Q(X) = proposal distribution (X->R)
 *   h(X) = mean function (X->R); function whose mean is being estimated using this sampler,
 *          e.g., for computing probability it can be the conditional probability of data (D)
 *          given X under the target distribution i.e., P(D|X).
 * </pre>
 * </p>
 * Sampler has iteration strategies which can be set via methods like {@code setIteratorBySampleSize}
 * and {@code setIteratorByTime}.
 *
 * @param <X> domain of sampling
 * @author Susanta Tewari
 * @version 1.4.2
 * @history First created on 03/07/2012 (derived from ImportanceSampling, 06/02/2011).
 * @since 1.4.2
 */
public class Sampler<X> {

    private final String name;
    private final Proposal<X> proposal;
    private final Factor<X> factor;
    private final SummaryStatistics stat = new SummaryStatistics();

    /** mean function; function whose mean is being estimated using this sampler */
    private final Function<X, BigDecimal> meanFunction;

    /**
     * <p>
     *  Creates an importance sampler for a given name, proposal, factor and mean function. Calling
     *  {@link #next()} draws a sample from the underlying domain. Note that drawn samples always
     *  add up. Drawn samples cannot be cleared, the user should create a new instance of the sampler.
     *  A number of methods of the form {@code nowX()} can be called to compute IS-Measures (not too
     *  expensive, as method {@code next()} does cumulative computations) on the already drawn
     *  samples.
     * </p>
     *
     * <p>
     *  The {@code name} is used for identification purposes when multiple proposals are run.
     *  However, two samplers may have the same name.
     * </p>
     *
     *
     * @param name name for this sampler; used for ID purposes, need not be unique
     * @param proposal proposal distribution
     * @param factor IS Factor
     * @param meanFunction function whose mean is being estimated using this sampler
     */
    public Sampler(final String name, final Proposal<X> proposal, final Factor<X> factor,
                   final Function<X, BigDecimal> meanFunction) {


        // null
        Objects.requireNonNull(name);
        Objects.requireNonNull(proposal);
        Objects.requireNonNull(factor);
        Objects.requireNonNull(meanFunction);

        this.name         = name;
        this.factor       = factor;
        this.proposal     = proposal;
        this.meanFunction = meanFunction;
    }

    /**
     * It is used for identification purposes when multiple proposals are run. However, two samplers
     * may have the same name.
     *
     * @return the name of this sampler
     */
    public String getName() {
        return name;
    }

    public Proposal<X> getProposal() {
        return proposal;
    }


    // <editor-fold desc="Measures">

    /**
     * Total number of times {@link #next()} invoked.
     *
     * @return total number of samples drawn so far.
     */
    public long nowSampleSize() {
        return stat.getN();
    }

    /**
     * Current estimate (average) based on the samples collected so far. Uses 128 digits of
     * precision and rounding mode {@link RoundingMode#HALF_UP}. It uses fixed digits of precision
     * to avoid non-terminating decimal expansion for inexact computations; e.g., when dividing 1 by
     * 3.
     *
     * @return the current estimate
     */
    public BigDecimal nowMean() {

        if (nowSampleSize() == 0) return BigDecimal.ZERO;

        return stat.getMean();
    }

    /**
     * @return current standard error for {@link #nowMean()}.
     */
    public BigDecimal nowStdError() {

        if (nowSampleSize() == 0) return BigDecimal.ZERO;

        final BigDecimal meanVar = nowVariance().divide(new BigDecimal(nowSampleSize()),
                                       MATHCONTEXT_128_HALF_UP);
        final Apfloat meanSE = ApfloatMath.sqrt(new Apfloat(meanVar));

        return new BigDecimal(meanSE.toString());
    }

    /**
     * @return current value of effective sample size (ESS)
     */
    public BigDecimal nowESS() {

        final BigDecimal variancePlusOne = nowCOVSquare().add(BigDecimal.ONE);

        return new BigDecimal(nowSampleSize()).divide(variancePlusOne, MATHCONTEXT_128_HALF_UP);
    }

    private BigDecimal nowVariance() {

        if (nowSampleSize() == 0) return BigDecimal.ZERO;

        return new BigDecimal(stat.getVariance());
    }

    /**
     * Returns the square of COV (coefficient of variation). It is variance divided by mean square.
     *
     * @return current value of COV-Square
     */
    private BigDecimal nowCOVSquare() {

        if (nowSampleSize() == 0) return BigDecimal.ZERO;

        final BigDecimal mean     = nowMean();
        final BigDecimal variance = new BigDecimal(stat.getVariance());

        return variance.divide(mean.pow(2), MATHCONTEXT_128_HALF_UP);

    }    // </editor-fold>


    // <editor-fold desc="Run-via-Iteration">
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
    public Sampler setIteratorBySampleSize(final long sampleSize) {

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
    public Sampler setIteratorByTime(final int time) {

        setIterator(Iterator_IS.of_Time(time));

        return this;
    }

    /**
     *
     * @throws InterruptedException if the current thread is interrupted while the sampler is
     * running
     */
    public void run() throws InterruptedException {

        proposal.init();
        start();

        while (hasNext()) {

            next();

            if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
        }

        proposal.clear();
    }

    private void start() {
        itr.start();
    }

    private boolean hasNext() {
        return itr.hasNext();
    }

    /**
     * Takes one more important sample and updates the calculation.
     * @throws IllegalStateException if {@link #hasNext()} returns {@code false}
     */
    private void next() {

        if (!hasNext()) throw new IllegalStateException("hasNext false");

        final X x               = proposal.sample();
        final BigDecimal augend = factor.getValue(x).multiply(meanFunction.apply(x));

        stat.addValue(augend);

    }    // </editor-fold>

    @VisibleForTesting
    Factor<X> getFactor() {
        return factor;
    }
}
