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

import coalescent.Genealogy;
import coalescent.model.K69;
import coalescent.statistic.K69_AC;
import com.google.common.base.Function;
import commons.is.Sampler;

import java.math.BigDecimal;

import static coalescent.is.GProposals_K69.*;

/**
 * In all the following factory methods, the default iterator is set by the sample size
 * ({@code[mutationCount + sampleSize-1]*500}) which can be changed via
 * {@code Sampler#setIteratorBy[X]} before {@code Sampler#run()} is called.
 *
 * @author Susanta Tewari
 * @version 1.4.2
 * @history Created on 12/1/13.
 * @since 1.4.2
 */
public class Samplers_K69 {

    /** Canonical name for EGT proposal */
    public static final String gtEGT = "gt_EGT";

    /** Canonical name for SD proposal */
    public static final String gtSD = "gt_SD";

    /** Canonical name for HUW proposal */
    public static final String gtHUW = "gt_HUW";

    /** Canonical name for mixed proposal on SD & HUW */
    public static final String gt_Mixed_SD_HUW = "gt_Mixed_SD_HUW";

    /** Canonical name for new proposal */
    public static final String gt_New = "gt_New";

    /** Canonical name for exact probability used as marker in various related computations */
    public static final String EXACT = "exactProb";

    /** identity target function */
    public static final Function<Genealogy<?, ?>, BigDecimal> TARGET_FUNC_ID =
        new Function<Genealogy<?, ?>, BigDecimal>() {

        @Override
        public BigDecimal apply(final Genealogy<?, ?> f) {
            return BigDecimal.ONE;
        }
    };

    /**
     * By default, sets iteration by size using {@code ac.eventsToMRCA() * 500}
     *
     * @param ac sample configuration
     * @return sampler based on EGT proposal
     */
    public static Sampler<?> of_gtEGT(K69_AC ac) {
        return of_gtEGT(ac, 500);
    }

    /**
     * By default, sets iteration by size using {@code ac.eventsToMRCA() * 500}
     *
     *
     * @param ac sample configuration
     * @return sampler based on EGT proposal
     */
    public static Sampler<?> of_gtSD(K69_AC ac) {
        return of_gtSD(ac, 500);
    }

    /**
     * By default, sets iteration by size using {@code ac.eventsToMRCA() * 500}
     *
     *
     * @param ac sample configuration
     * @return sampler based on EGT proposal
     */
    public static Sampler<?> of_gtHUW(K69_AC ac) {
        return of_gtHUW(ac, 500);
    }

    /**
     * By default, sets iteration by size using {@code ac.eventsToMRCA() * 500}
     *
     * @param ac sample configuration
     * @param perOrderSize
     * @return sampler based on EGT proposal
     */
    public static Sampler<?> of_gtEGT(K69_AC ac, int perOrderSize) {

        final GProposal<K69_AC, K69> pDist = of_EGT(ac);
        final Sampler sampler = new Sampler(gtEGT, pDist, pDist.of_Factor(), TARGET_FUNC_ID);

        sampler.setIteratorBySampleSize(ac.eventsToMRCA() * perOrderSize);

        return sampler;
    }

    /**
     * By default, sets iteration by size using {@code ac.eventsToMRCA() * 500}
     *
     *
     * @param ac sample configuration
     * @param perOrderSize
     * @return sampler based on EGT proposal
     */
    public static Sampler<?> of_gtSD(K69_AC ac, int perOrderSize) {

        final GProposal<K69_AC, K69> pDist = of_SD(ac);
        final Sampler sampler = new Sampler(gtSD, pDist, pDist.of_Factor(), TARGET_FUNC_ID);

        sampler.setIteratorBySampleSize(ac.eventsToMRCA() * perOrderSize);

        return sampler;
    }

    /**
     * By default, sets iteration by size using {@code ac.eventsToMRCA() * 500}
     *
     *
     * @param ac sample configuration
     * @param perOrderSize
     * @return sampler based on EGT proposal
     */
    public static Sampler<?> of_gtHUW(K69_AC ac, int perOrderSize) {

        final GProposal<K69_AC, K69> pDist = of_HUW(ac);
        final Sampler sampler = new Sampler(gtHUW, pDist, pDist.of_Factor(), TARGET_FUNC_ID);

        sampler.setIteratorBySampleSize(ac.eventsToMRCA() * perOrderSize);

        return sampler;
    }

    public static Sampler<?> of_gtMixed_SD_HUW(K69_AC ac) {
        return of_gtMixed_SD_HUW(ac, 500);
    }

    public static Sampler<?> of_gtMixed_SD_HUW(K69_AC ac, int perOrderSize) {

        final GProposal<K69_AC, K69> pDist = GProposals_Composite_K69.of_SD_HUW(ac);
        final Sampler sampler = new Sampler(gt_Mixed_SD_HUW, pDist, pDist.of_Factor(),
                                    TARGET_FUNC_ID);

        sampler.setIteratorBySampleSize(ac.eventsToMRCA() * perOrderSize);

        return sampler;
    }

    public static Sampler<?> of_gtNew(K69_AC ac) {
        return of_gtNew(ac, 500);
    }

    public static Sampler<?> of_gtNew(K69_AC ac, int perOrderSize) {

        final GProposal<K69_AC, K69> pDist = of_New(ac);
        final Sampler sampler = new Sampler(gt_New, pDist, pDist.of_Factor(), TARGET_FUNC_ID);

        sampler.setIteratorBySampleSize(ac.eventsToMRCA() * perOrderSize);

        return sampler;
    }

    private Samplers_K69() {}
}
