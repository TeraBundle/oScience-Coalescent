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
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 * @version 1.2.3
 * @author Susanta Tewari
 * @history Created on 1/17/13
 * @since 1.2.3
 * @param <M>
 * @param <D>
 * @param <S>
 */
public abstract class MultiLHoodComputer<M extends Model, D extends Data<M, D>,
        S extends Statistic<D, S>> {

    /**
     * Computes log-likelihood (natural logarithm, base {@code e}) of the given statistic.
     *
     * @param s statistic
     * @param models models
     * @return log-likelihood (natural logarithm, base {@code e})
     * @throws InterruptedException
     */
    public abstract Map<M, BigDecimal> computeLogLHood(S s, Iterable<M> models)
            throws InterruptedException;

    /**
     * Provides a default implementation where each likelihood is computed serially.
     *
     * @param lhoodComputer
     * @param <M>
     * @param <D>
     * @param <S>
     * @return
     */
    public static <M extends Model, D extends Data<M, D>,
                   S extends Statistic<D, S>> MultiLHoodComputer<M, D,
                       S> ofDefault(final LikelihoodComputer<S> lhoodComputer) {

        return new MultiLHoodComputer<M, D, S>() {

            @Override
            public Map<M, BigDecimal> computeLogLHood(final S s, final Iterable<M> models)
                    throws InterruptedException {

                final Map<M, BigDecimal> result = new HashMap<>();

                for (final M m : models) {

                    final MutableData<M, D> mutableData = s.getData().getMutable();

                    mutableData.setModel(m);

                    final S s1 = s.factory(mutableData.getImmutable());

                    result.put(m, lhoodComputer.computeLogLHood(s1));
                }

                return result;
            }
        };
    }
}
