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

import coalescent.Event;
import coalescent.Genealogy;
import coalescent.statistic.AC;
import commons.util.FormatUtil;
import commons.util.MapUtil;

import java.math.BigDecimal;
import java.util.*;

/**
 * Builds all the genealogies of a sample configuration by observing its recursion (via post-order
 * implementation).
 *
 * @author Susanta Tewari
 * @version 1.0 Dec 3, 2010
 */
public final class GenealogyBuilder<C extends AC<C, ?>>
        extends RecursionComputer<C, List<Genealogy>, List<Genealogy>> {

    public GenealogyBuilder() {}

    @Override
    public void onBoundaryCondn(final RecursionEvent<C> event) {

        final C sampleConfig = event.getSampleConfig();

        if (getCache().get(sampleConfig) == null) {

            final List<Genealogy> genealogies = new ArrayList<>();

            genealogies.add(new Genealogy(new ArrayList<Event>()));
            getCache().put(sampleConfig, genealogies);
        }
    }

    @Override
    public void onStartingIteratingEvents(final RecursionEvent<C> event) {

        final C sampleConfig = event.getSampleConfig();

        getCache().put(sampleConfig, new ArrayList<Genealogy>());
    }

    @Override
    public void onPostVisitAnsConfig(final RecursionEvent<C> event) {

        final C sampleConfig              = event.getSampleConfig();
        final List<Genealogy> genealogies = getCache().get(sampleConfig);
        final C ancestralConfig           = event.getAncestralConfig();

        for (final Genealogy ancestralGenealogy : getValue(ancestralConfig)) {

            final List<Event> ancestralEvents = new ArrayList<>();

            ancestralEvents.add(new Event(sampleConfig, ancestralConfig, event.getAllele(),
                                          event.getEventType()));
            ancestralEvents.addAll(ancestralGenealogy.getEventChain());
            genealogies.add(new Genealogy(ancestralEvents));
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

    /**
     * Provides the genealogies of the sample configuration (statistic) used in the last computation
     * in post-order traversal of the genealogy tree.
     *
     * @return genealogies of the last sample configuration
     */
    public List<Genealogy> genealogies() {
        return result;
    }

    /**
     * Genealogies sorted by their probabilities in the descending order
     *
     * @return genealogies sorted in descending order of their probabilities
     */
    public Map<Genealogy, BigDecimal> genealogiesSortedByProb() {

        Map<Genealogy, BigDecimal> genealogy_prob_map = new HashMap<>();

        for (final Genealogy genealogy : result) {
            genealogy_prob_map.put(genealogy, genealogy.probability());
        }


        // sort by prob
        genealogy_prob_map = MapUtil.sortByValue(genealogy_prob_map, Collections.reverseOrder());

        return genealogy_prob_map;
    }

    /**
     * Prints genealogies in descending order of their probabilities. Also computes the sum of those
     * probabilities.
     *
     * @return
     */
    @Override
    public String toString() {

        super.toString();


        // prepare the necessary
        final Map<Genealogy, BigDecimal> genealogy_prob_map = genealogiesSortedByProb();
        BigDecimal totalProb                                = BigDecimal.valueOf(0.0);


        // start printing
        final StringBuilder builder = new StringBuilder();

        builder.append("Genealogies:\n");

        for (final Genealogy genealogy : genealogy_prob_map.keySet()) {

            final BigDecimal prob = genealogy_prob_map.get(genealogy);

            totalProb = totalProb.add(prob);

            builder.append("Event Sequence: " + genealogy.toStringEventSeq() + " Prob: "
                           + FormatUtil.format(prob) + " Genealogy: " + genealogy + "\n");
        }

        builder.append("Sum of genealogy probabilities: " + FormatUtil.format(totalProb) + "\n");

        return builder.toString();
    }
}
