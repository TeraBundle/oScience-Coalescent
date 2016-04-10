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
import coalescent.recursion.ProbComputer_Exact;
import coalescent.recursion.Recursion;
import coalescent.statistic.K69_AC;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

import static coalescent.EventType.COALESCENT;
import static coalescent.EventType.MUTATION;
import static coalescent.phylogeny.GeneTree.Node;
import static com.google.common.collect.Lists.newArrayList;

/**
 * @since since
 * @version version
 * @author Susanta Tewari
 * @history Created on 8/15/2014.
 */
final class GProposal_K69_ExactDelegate extends GProposal<K69_AC, K69> {

    private ProbComputer_Exact<K69_AC> exactProb;
    private GProposal<K69_AC, K69> proposal;

    /** performance */
    private K69_AC last_config;
    private boolean inCache;
    public static final Logger lgr = Logger.getLogger(GProposal_K69_ExactDelegate.class.getName());

    public GProposal_K69_ExactDelegate(K69_AC sample, GProposal<K69_AC, K69> delegate) {

        super(sample);

        this.proposal = delegate;
    }

    @Override
    public void init() { //J-

        final K69_AC exactSample = getExactSample_HUW(getSample().of_Singleton());
        lgr.info("Using exact data: " + exactSample.toString1());


        final Recursion<K69_AC, ProbComputer_Exact<K69_AC>> recursion =
            new Recursion<>(exactSample);

        exactProb = new ProbComputer_Exact();
        recursion.addExactRecursionEventListener(exactProb);

        new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    recursion.runRecursion();
                    lgr.info("Finished exact recursion.");
                } catch (InterruptedException e) {
                    return;
                }
            }

        }).start(); //J+

    }

    /**
     * Creates an ancestor at a certain (32) level using the delegate proposal.
     *
     * @param config
     * @return an ancestor at a certain (32) level
     */
    private K69_AC getExactSample_HUW(K69_AC s) {

        final int gap = s.eventsToMRCA() - 32;

        return (gap <= 0) ? s : proposal.of(s).sample().getEventChain().get(gap).getPost();
    }

    /**
     * Creates an ancestor at a certain (32) level.
     *
     * @param config
     * @return an ancestor at a certain (32) level
     */
    private static K69_AC getExactSample(K69_AC s) {

        final int gap = s.eventsToMRCA() - 32;

        for (int i = 0; i < gap; i++) {

            List<Node> alleles_c = newArrayList(s.alleles(COALESCENT));

            if (!alleles_c.isEmpty()) {
                s = s.apply(alleles_c.get(0), COALESCENT);
            } else {

                List<Node> alleles_m = newArrayList(s.alleles(MUTATION));

                s = s.apply(alleles_m.get(0), MUTATION);
            }
        }

        return s;
    }

    @Override
    protected BigDecimal proposalWeight(final K69_AC config, final Object allele,
            final EventType eventType) {

        if (last_config != config) inCache = exactProb.getValue(config) != null;

        if (!inCache) return proposal.proposalWeight(config, allele, eventType);
        else {

            final K69_AC ac       = config.apply(allele, eventType);
            final BigDecimal prob = exactProb.getValue(ac);

            return config.transitionProb(eventType, (Node) allele).multiply(prob);
        }
    }

    @Override
    public void clear() {
        exactProb.clearCache();
    }
}
