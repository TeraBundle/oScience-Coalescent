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
import coalescent.Genealogy;
import coalescent.model.K69;
import coalescent.phylogeny.GeneTree.Node;
import coalescent.statistic.K69_AC;
import commons.is.Factor;
import commons.util.ApfloatUtil;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static commons.core.Singletons.MATHCONTEXT_128_HALF_UP;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

/**
 * @author Susanta Tewari
 * @version 1.4.2
 * @history Created on May 25, 2012.
 * @since 1.4.2
 */
public class GProposals_K69 {

    public static GProposal<K69_AC, K69> of_New(final K69_AC sample) {
//        return new GProposal_K69_ExactDelegate(sample, GProposals_K69.of_HUW(sample));
        return new GProposal_K69_Exact(sample);
    }

    /**
     * Stephens-Donnelly (SD) proposal for infinite-sites model.
     *
     * <p align="center">
     *     <img src="doc-files/SD-proposal.png.png"/>
     * </p>
     *
     *
     * @param sample sample configuration
     * @return gene tree based SD proposal
     */
    public static GProposal<K69_AC, K69> of_SD(final K69_AC sample) {

        return new GProposal<K69_AC, K69>(sample) {

            @Override
            protected BigDecimal proposalWeight(final K69_AC config, final Object allele,
                    final EventType eventType) {

                final Node actual_allele = (Node) allele;
                final int allele_freq    = config.getGeneTree().getFreq(actual_allele);

                return new BigDecimal(allele_freq);
            }
        };
    }

    /**
     * Ethier-Griffiths-Tavare (EGT) proposal infinite-sites model.
     *
     * <p align="center">
     *     <img src="doc-files/GT-proposal.png.png"/>
     * </p>
     *
     *
     * @param sample sample configuration
     * @return EGT proposal
     */
    public static GProposal<K69_AC, K69> of_EGT(final K69_AC sample) {

        return new GProposal<K69_AC, K69>(sample) {

            @Override
            public Factor<Genealogy<K69_AC, K69>> of_Factor() {

                return new Factor<Genealogy<K69_AC, K69>>() {

                    @Override
                    public BigDecimal getValue(final Genealogy<K69_AC, K69> x) {
                        return probability(x);
                    }
                };
            }
            @Override
            protected BigDecimal getProbability(ElementSampler eSampler) {
                return eSampler.getWeightSum();
            }
            @Override
            protected BigDecimal proposalWeight(final K69_AC config, final Object allele,
                    final EventType eventType) {
                return config.transitionProb(eventType, (Node) allele);
            }
        };
    }

    /**
     * Hobolth-Uyenoyamay-Wiuf (HUW) proposal infinite-sites model.
     *
     * <p align="center">
     *     <img src="doc-files/hobolth-proposal.png.png"/>
     * </p>
     *
     * @param sample sample configuration
     * @return HUW proposal
     */
    public static GProposal<K69_AC, K69> of_HUW(final K69_AC sample) {

        return new GProposal<K69_AC, K69>(sample) {

            private final double theta_not = getSample().getModel().getMutationRate();
            private final Map<String, BigDecimal> p_cache         = new HashMap<>();
            private final Map<String, BigDecimal> factorial_cache = new HashMap<>(50);
            @Override
            protected BigDecimal proposalWeight(final K69_AC config, final Object allele,
                    final EventType eventType) {

                final Map<String, Integer> dm = config.getDm();

                if (dm.isEmpty()) return ONE;

                final int n          = config.getN();
                final Node a         = (Node) allele;
                final BigDecimal n_k = new BigDecimal(config.getFreq().get(a));
                BigDecimal result    = ZERO;
                final String path    = a.getUpperMutationPath();

                for (String m : dm.keySet()) {

                    final Integer d = dm.get(m);
                    String key      = n + "-" + d;

                    if (!p_cache.containsKey(key)) p_cache.put(key, p(n, d));

                    final BigDecimal p = p_cache.get(key);
                    BigDecimal u       = null;

                    if (path.contains(m)) {
                        u = p.multiply(n_k).divide(new BigDecimal(d), MATHCONTEXT_128_HALF_UP);
                    } else {

                        u = ONE.subtract(p).multiply(n_k).divide(new BigDecimal(n - d),
                                         MATHCONTEXT_128_HALF_UP);
                    }

                    result = result.add(u);
                }

                return result;
            }
            private BigDecimal p(int n, int d) {

                if (d == 1) return p(n);

                BigDecimal nr    = ZERO;
                BigDecimal dr    = ZERO;
                boolean loop_ran = false;

                for (int k = 2; (k <= n - d + 1) && (n != k); k++) {

                    loop_ran = true;

                    BigDecimal F1 = new BigDecimal(d - 1).divide(new BigDecimal(n - k),
                                                   MATHCONTEXT_128_HALF_UP);
                    BigDecimal f2 = new BigDecimal(k - 1 + theta_not);
                    BigDecimal f3 = ApfloatUtil.choose(n - d - 1, k - 2);
                    BigDecimal f4 = ApfloatUtil.choose(n - 1, k - 1);
                    BigDecimal F2 = f3.divide(f2.multiply(f4), MATHCONTEXT_128_HALF_UP);

                    nr = nr.add(F1.multiply(F2));
                    dr = dr.add(F2);
                }

                if (!loop_ran) return ONE;

                assert dr != ZERO;

                return nr.divide(dr, MATHCONTEXT_128_HALF_UP);
            }
            private BigDecimal p(int n) {

                BigDecimal dr = ZERO;

                for (int k = 2; k <= n; k++) {

                    BigDecimal f1 = new BigDecimal(k - 1).divide(new BigDecimal(n - 1),
                                                   MATHCONTEXT_128_HALF_UP);
                    BigDecimal f2   = new BigDecimal(k - 1 + theta_not);
                    BigDecimal term = f1.divide(f2, MATHCONTEXT_128_HALF_UP);

                    dr = dr.add(term);
                }

                dr = dr.multiply(new BigDecimal(n - 1 + theta_not));

                assert dr != ZERO;

                return ONE.divide(dr, MATHCONTEXT_128_HALF_UP);
            }
            public BigDecimal choose(final int n, final int m) {

                final String key = n + "-" + m;

                if (factorial_cache.containsKey(key)) return factorial_cache.get(key);
                else {

                    final BigDecimal val = ApfloatUtil.choose(n, m);

                    factorial_cache.put(key, val);

                    return val;
                }
            }
        };
    }

    /**
     * To prevent instantiation.
     */
    private GProposals_K69() {}
}
