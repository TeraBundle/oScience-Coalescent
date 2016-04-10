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
import commons.util.FormatUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes the total number of genealogies of a sample configuration by observing its recursion
 * (via the post-order implementation).
 *
 * @author Susanta Tewari
 * @version 1.0 Dec 3, 2010
 */
public final class GenealogyCounter<C extends AC<C, ?>>
        extends RecursionComputer<C, Void, BigInteger> {

    /**
     * cache of counts of genealogies for the ancestral configurations
     */
    private Map<AC, BigInteger> genealogyCountCache = new HashMap<>();

    /**
     * keeps track of the ancestral configs during onPostVisitAnsConfig()
     */
    private final Map<C, List<C>> ancestralConfigs = new HashMap<>();

    /** Field description */
    private BigInteger count;

    /**
     * Provides the total number of genealogies of the sample configuration (statistic) used in the
     * last computation.
     *
     * @return total number of genealogies of the last sample configuration
     */
    public BigInteger genealogiesCount() {
        return count;
    }

    /**
     * Method description
     *
     *
     * @param event
     */
    @Override
    public void onBoundaryCondn(final RecursionEvent<C> event) {

        final C sampleConfig = event.getSampleConfig();

        if (genealogyCountCache.get(sampleConfig) == null) {
            genealogyCountCache.put(sampleConfig, new BigInteger("1"));
        }
    }

    /**
     * Method description
     *
     *
     * @param event
     */
    @Override
    public void onStartingIteratingEvents(final RecursionEvent<C> event) {

        final C sampleConfig = event.getSampleConfig();

        genealogyCountCache.put(sampleConfig, new BigInteger("0"));


        // computation for a new sample config starts.
        ancestralConfigs.put(sampleConfig, new ArrayList<C>());
    }

    /**
     * Method description
     *
     *
     * @param event
     */
    @Override
    public void onPostVisitAnsConfig(final RecursionEvent<C> event) {


        // get the previous genealogies count for the sample config
        final C sampleConfig      = event.getSampleConfig();
        final BigInteger previous = genealogyCountCache.get(sampleConfig);


        // add the count for the ancestral config
        final C ancestralConfig = event.getAncestralConfig();


        // sampleConfig could give equal ancestral configurations.
        // unlike computing exact probs, we need to count once.
        if (ancestralConfigs.get(sampleConfig).contains(ancestralConfig)) {
            return;
        }

        ancestralConfigs.get(sampleConfig).add(ancestralConfig);
        genealogyCountCache.put(sampleConfig,
                                previous.add(genealogyCountCache.get(ancestralConfig)));
    }

    /**
     * Method description
     *
     *
     * @param event
     */
    @Override
    public void onFinishedIteratingEvents(final RecursionEvent<C> event) {

        final C sampleConfig = event.getSampleConfig();

        ancestralConfigs.remove(sampleConfig);
    }

    /**
     * Method description
     *
     *
     * @param event
     */
    @Override
    public void onFinishedRecursion(final RecursionEvent<C> event) {

        super.onFinishedRecursion(event);

        final C sampleConfig = event.getSampleConfig();

        if (event.getStatistic().equals(sampleConfig)) {

            count  = genealogyCountCache.get(sampleConfig);
            result = count;


            // de-allocate the resources
            genealogyCountCache = null;
        }
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public String toString() {

        super.toString();

        return "Total # of ancestral genealogies: " + FormatUtil.format(count);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public String getDescription() {
        return "GenealogyCounter";
    }
}
