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

import coalescent.Event;
import coalescent.EventType;
import coalescent.Genealogy;
import coalescent.model.K69;
import coalescent.statistic.K69_AC;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static coalescent.EventType.MUTATION;

/**
 * @since 1.4.2
 * @version 1.4.2
 * @author Susanta Tewari
 * @history Created on 1/28/14.
 */
final class GProposals_Multi_K69 {

    public static GProposal_Multi<K69_AC, K69> of_gtEGT_Default(final K69_AC ac, K69[] models) {
        return new GProposal_Multi<K69_AC, K69>(GProposals_K69.of_EGT(ac), models);
    }

    public static GProposal_Multi<K69_AC, K69> of_gtEGT(final K69_AC ac, K69[] models) {

        return new GProposal_Multi.NID<K69_AC, K69>(GProposals_K69.of_EGT(ac), models) {

            @Override
            protected BigDecimal[] factorImpl(Genealogy<K69_AC, K69>[] x) {
                return probability(x);
            }
            @Override
            public BigDecimal[] probability(Genealogy<K69_AC, K69>[] x) {

                final List<Event<K69_AC>> event_chain         = x[0].getEventChain();
                final Map<K69_AC, ElementSampler> eSamplerMap = cache[0];
                final double t0                               = models[0].getMutationRate();
                BigDecimal[] result                           = new BigDecimal[models.length];

                Arrays.fill(result, BigDecimal.ONE);

                for (final Event<K69_AC> event : event_chain) {

                    final EventType type          = event.getType();
                    final K69_AC config           = event.getPre();
                    final ElementSampler eSampler = eSamplerMap.get(config);
                    final int n                   = config.getN();

                    for (int i = 0; i < models.length; i++) {

                        final double t    = models[i].getMutationRate();
                        BigDecimal factor = new BigDecimal((n - 1 + t0) / (n - 1 + t));

                        if (type == MUTATION) factor = factor.multiply(new BigDecimal(t / t0));

                        final BigDecimal prob       = eSampler.getWeightSum();
                        final BigDecimal prb_scaled = prob.multiply(factor);

                        result[i] = result[i].multiply(prb_scaled);
                    }
                }

                return result;
            }
        };
    }

    public static GProposal_Multi<K69_AC, K69> of_gtEGT_Improved(final K69_AC ac, K69[] models) {

        return new GProposal_Multi<K69_AC, K69>(GProposals_K69.of_EGT(ac), models) {}
        ;
    }

    public static GProposal_Multi<K69_AC, K69> of_gtSD(final K69_AC ac, K69[] models) {
        return new GProposal_Multi.NID(GProposals_K69.of_SD(ac), models);
    }

    public static GProposal_Multi<K69_AC, K69> of_gtHUW(final K69_AC ac, K69[] models) {
        return new GProposal_Multi.NID(GProposals_K69.of_HUW(ac), models);
    }
}
