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

import coalescent.model.PopGenModel;
import coalescent.statistic.AC;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import org.openide.util.NbBundle;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version 1.4.2
 * @user Susanta Tewari
 * @history First created on 10/31/12.
 * @since 1.4.0
 *
 * @param <M> description
 */
public class ProbComputer_Exact_Multi<C extends AC<C, M>, M extends PopGenModel>
        extends RecursionComputer<C, Map<M, BigDecimal>, Map<M, BigDecimal>>
        implements ExactRecursionListeners.MT<C, Map<M, BigDecimal>, Map<M, BigDecimal>,
            ProbComputer_Exact_Multi<C, M>> {

    /** for adding probs of AC`s for a given event type */
    private final Map<C, Map<M, BigDecimal>> adder = new HashMap<>();
    private final ImmutableList<M> models;

    /**
     * @param models description
     */
    public ProbComputer_Exact_Multi(final List<M> models) {
        this.models = ImmutableList.copyOf(models);
    }

    /**
     * The probability of the sample configuration being observed.
     *
     * @return probability of the sample configuration being observed.
     */
    @Override
    public Map<M, BigDecimal> getResult() {
        return result;
    }

    @Override
    public ProbComputer_Exact_Multi<C, M> of(final Function<C, Map<M, BigDecimal>> cacheValFunc) {

        return new ProbComputer_Exact_Multi<C, M>(models) {

            @Override
            public Map<M, BigDecimal> getValue(final C s) {
                return cacheValFunc.apply(s);
            }
        };
    }

    @Override
    public void onBoundaryCondn(final RecursionEvent<C> event) {

        final C sampleConfig = event.getSampleConfig();

        if (getCache().get(sampleConfig) == null) {

            final Map<M, BigDecimal> mrca_probs = new HashMap<>(models.size());

            for (final M model : models) {

                final BigDecimal mrca_prob = BigDecimal.valueOf(sampleConfig.probAtMRCA());

                mrca_probs.put(model, mrca_prob);
            }

            getCache().put(sampleConfig, mrca_probs);
        }
    }

    @Override
    public void onStartingIteratingEvents(final RecursionEvent<C> event) {

        final C sampleConfig           = event.getSampleConfig();
        final Map<M, BigDecimal> probs = new HashMap<>(models.size());

        for (final M model : models) {
            probs.put(model, BigDecimal.ZERO);
        }

        getCache().put(sampleConfig, probs);    // init
    }

    @Override
    public void onIteratingSingleEventTypes(final RecursionEvent<C> event) {

        final C sampleConfig           = event.getSampleConfig();
        final Map<M, BigDecimal> probs = new HashMap<>(models.size());

        for (final M model : models) {
            probs.put(model, BigDecimal.ZERO);
        }

        adder.put(sampleConfig, probs);
    }

    @Override
    public void onPostVisitAnsConfig(final RecursionEvent<C> event) {

        final C sampleConfig                    = event.getSampleConfig();
        final C ancestralConfig                 = event.getAncestralConfig();
        final Map<M, BigDecimal> previousProbs  = getCache().get(sampleConfig);
        final Map<M, BigDecimal> ancestralProbs = getValue(ancestralConfig);

        for (final M model : models) {

            sampleConfig.setModel(model);

            final BigDecimal previousProb  = previousProbs.get(model);
            final BigDecimal ancestralProb = ancestralProbs.get(model);
            final BigDecimal transition_prob = sampleConfig.transitionProb(event.getEventType(),
                                                   ancestralConfig);
            final BigDecimal value         = transition_prob.multiply(ancestralProb);

            previousProbs.put(model, previousProb.add(value));
        }
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
        return "Exact Probability: " + result;
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(getClass(), "ExactProbComputer.description");
    }
}
