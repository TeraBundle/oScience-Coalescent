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

package coalescent.is;

import coalescent.EventType;
import coalescent.model.K69;
import coalescent.phylogeny.GeneTree;
import coalescent.recursion.ProbComputer_Exact;
import coalescent.recursion.Recursion;
import coalescent.statistic.K69_AC;

import static coalescent.EventType.COALESCENT;
import static coalescent.EventType.MUTATION;
import static coalescent.phylogeny.GeneTree.Node;

import static com.google.common.collect.Lists.newArrayList;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @since since
 * @version version
 * @author Susanta Tewari
 * @history Created on 8/15/2014.
 */
final class GProposal_K69_Exact extends GProposal<K69_AC, K69> {

    public static final Logger lgr = Logger.getLogger(GProposal_K69_Exact.class.getName());
    private ProbComputer_Exact<K69_AC> exactProb;

    public GProposal_K69_Exact(K69_AC sample) {
        super(sample);
    }

    @Override
    public void init() { //J-

        final K69_AC exactSample = getSample().of_Singleton();
        lgr.info("Using exact data: " + exactSample.toString1());


        final Recursion<K69_AC, ProbComputer_Exact<K69_AC>> recursion =
                new Recursion<>(exactSample);

        exactProb = new ProbComputer_Exact();
        recursion.addExactRecursionEventListener(exactProb);


        // run exact
        try {
            recursion.runRecursion();
            lgr.info("Finished exact recursion.");

        } catch (InterruptedException e) {
            e.printStackTrace();

        } //J+

    }

    @Override
    protected BigDecimal proposalWeight(final K69_AC config, final Object allele, final EventType eventType) { //J-

        final K69_AC config_s = config.of_Singleton();

        // allele_s
        final Map<Node, Node> srcCopy_s = config_s.getSrcCopy();
        final Node allele_s             = srcCopy_s.get(allele);

        // probability
        final K69_AC ac_s         = config_s.apply(allele_s, MUTATION);    // always on mutation
        final BigDecimal prob_s   = exactProb.getValue(ac_s);    // guaranteed by recursion
        final BigDecimal weight_s = config_s.transitionProb(eventType, allele_s).multiply(prob_s);

        // freq
        final Integer freq = config.getFreq().get(allele);

        return weight_s.multiply(new BigDecimal(freq));    //J+ freq * exact-sampling

    }

    @Override
    public void clear() {
        exactProb.clearCache();
    }
}
