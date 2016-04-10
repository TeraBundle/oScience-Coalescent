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

package coalescent.recursion;

import coalescent.statistic.AC;
import com.google.common.base.Function;
import commons.util.FormatUtil;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Computes the probability of a sample configuration by observing its recursion (via the post-order
 * implementation).
 *
 * @author Susanta Tewari
 * @version 1.0 Dec 3, 2010
 */
public class ProbComputer_Exact<C extends AC<C, ?>>
        extends RecursionComputer<C, BigDecimal, BigDecimal>
        implements ExactRecursionListeners.MT_ME<C, BigDecimal, BigDecimal, ProbComputer_Exact<C>> {

    /* adds probs till sample config is done */
    private final Map<C, BigDecimal> adder = new HashMap<>();

    @Override
    public ProbComputer_Exact<C> of(final Function<C, BigDecimal> cacheValFunc) {

        return new ProbComputer_Exact<C>() {

            @Override
            public BigDecimal getValue(final C s) {
                return cacheValFunc.apply(s);
            }
        };
    }

    @Override
    public void onBoundaryCondn(final RecursionEvent<C> event) {

        final C sampleConfig = event.getSampleConfig();

        if (getCache().get(sampleConfig) == null) {

            final BigDecimal mrca_prob = BigDecimal.valueOf(sampleConfig.probAtMRCA());

            getCache().put(sampleConfig, mrca_prob);
        }
    }

    @Override
    public void onStartingIteratingEvents(final RecursionEvent<C> event) {

        final C sampleConfig = event.getSampleConfig();

        adder.put(sampleConfig, BigDecimal.ZERO);    // init
    }

    @Override
    public void onPostVisitAnsConfig(final RecursionEvent<C> event) {

        final C sampleConfig           = event.getSampleConfig();
        final C ancestralConfig        = event.getAncestralConfig();
        final BigDecimal transition_prob = sampleConfig.transitionProb(event.getEventType(),
                                               ancestralConfig);
        final BigDecimal ancestralProb = getValue(ancestralConfig);
        final BigDecimal new_value     = transition_prob.multiply(ancestralProb);
        final BigDecimal previous      = adder.get(sampleConfig);

        adder.put(sampleConfig, previous.add(new_value));
    }

    @Override
    public void onFinishedIteratingEvents(final RecursionEvent<C> event) {

        final C sampleConfig = event.getSampleConfig();

        getCache().put(sampleConfig, adder.get(sampleConfig));
        adder.remove(sampleConfig);
    }

    @Override
    public void onFinishedRecursion(final RecursionEvent<C> event) {

        super.onFinishedRecursion(event);

        final C sampleConfig = event.getSampleConfig();

        if (event.getStatistic().equals(sampleConfig)) {

            result            = getCache().get(sampleConfig);
            this.sampleConfig = sampleConfig;
        }
    }

    @Override
    public String toString() {
        return "Exact Probability: " + FormatUtil.format(result);
    }
}
