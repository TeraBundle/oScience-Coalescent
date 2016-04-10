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

package commons.core;

import java.math.BigDecimal;

/**
 * @param <M>
 * @param <D>
 * @param <S>
 * @param <R>
 * @author Susanta Tewari
 */
public abstract class ModelEstimator<M extends Model, D extends Data<M, D>,
        S extends Statistic<D, S>, R extends ModelEstimator<M, D, S, R>>
        implements Statistic<D, R> {

    /**
     * the statistic defines the sample configuration of the recursion
     */
    private final S statistic;

    /**
     * Creates an instance from a given statistic.
     *
     * @param statistic the underlying statistic of this recursion
     */
    protected ModelEstimator(final S statistic) {
        this.statistic = statistic;
    }

    /**
     * @return estimated model
     */
    public abstract M getEstimate();

    /**
     * By default, it returns false unless overridden.
     *
     * @return {@code true} if variance is known; {@code false} otherwise
     */
    public boolean isVarianceKnown() {
        return false;
    }

    /**
     * By default, throws UnsupportedOperationException unless overridden.
     *
     * @return variance
     * @throws UnsupportedOperationException if {@link #isVarianceKnown()} returns {@code false}
     */
    public BigDecimal getVariance() {
        throw new UnsupportedOperationException("Unknown variance");
    }

    /**
     * Does not support editing the model estimator.
     *
     * @return {@code null}
     */
    @Override
    public final Mutable<R> getMutable() {
        return null;
    }

    @Override
    public final D getData() {
        return statistic.getData();
    }
}
