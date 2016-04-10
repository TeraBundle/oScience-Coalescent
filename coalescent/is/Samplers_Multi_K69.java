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

import coalescent.model.K69;
import coalescent.statistic.K69_AC;
import commons.is.Sampler_Multi;

import java.util.List;

/**
 * @since 1.4.2
 * @version 1.4.2
 * @author Susanta Tewari
 * @history Created on 1/25/14.
 */
public class Samplers_Multi_K69 {

    /** Canonical name for EGT proposal */
    public static final String gtEGT          = "gt_EGT";
    public static final String gtEGT_Default  = "gt_EGT_Default";
    public static final String gtEGT_Improved = "gt_EGT_Improved";

    /** Canonical name for SD proposal */
    public static final String gtSD = "gt_SD";

    /** Canonical name for HUW proposal */
    public static final String gtHUW = "gt_HUW";

    /**
     *
     * @param ac sample configuration
     * @param models
     * @return sampler based on EGT proposal
     */
    public static Sampler_Multi<?> of_gtEGT(K69_AC ac, final List<K69> models) {
        return of_gtEGT(ac, models, 500);
    }

    public static Sampler_Multi<?> of_gtEGT_Default(K69_AC ac, final List<K69> models) {
        return of_gtEGT_Default(ac, models, 500);
    }

    public static Sampler_Multi<?> of_gtEGT_Improved(K69_AC ac, final List<K69> models) {
        return of_gtEGT_Improved(ac, models, 500);
    }

    /**
     *
     * @param ac sample configuration
     * @param models
     * @return sampler based on EGT proposal
     */
    public static Sampler_Multi<?> of_gtSD(K69_AC ac, final List<K69> models) {
        return of_gtSD(ac, models, 500);
    }

    /**
     *
     * @param ac sample configuration
     * @param models
     * @return sampler based on EGT proposal
     */
    public static Sampler_Multi<?> of_gtHUW(K69_AC ac, final List<K69> models) {
        return of_gtHUW(ac, models, 500);
    }

    /**
     *
     * @param ac sample configuration
     * @param sizePerOrder
     * @return sampler based on EGT proposal
     */
    public static Sampler_Multi<?> of_gtEGT(K69_AC ac, final List<K69> models, int sizePerOrder) {

        final Sampler_Multi s = of(gtEGT, GProposals_Multi_K69.of_gtEGT(ac, toArray(models)));

        s.setIteratorBySampleSize(ac.eventsToMRCA() * sizePerOrder);

        return s;
    }

    public static Sampler_Multi<?> of_gtEGT_Default(K69_AC ac, final List<K69> models,
            int sizePerOrder) {

        final Sampler_Multi s = of(gtEGT_Default,
                                   GProposals_Multi_K69.of_gtEGT_Default(ac, toArray(models)));

        s.setIteratorBySampleSize(ac.eventsToMRCA() * sizePerOrder);

        return s;
    }

    public static Sampler_Multi<?> of_gtEGT_Improved(K69_AC ac, final List<K69> models,
            int sizePerOrder) {

        final Sampler_Multi s = of(gtEGT_Improved,
                                   GProposals_Multi_K69.of_gtEGT_Improved(ac, toArray(models)));

        s.setIteratorBySampleSize(ac.eventsToMRCA() * sizePerOrder);

        return s;
    }

    /**
     *
     * @param ac sample configuration
     * @param sizePerOrder
     * @return sampler based on EGT proposal
     */
    public static Sampler_Multi<?> of_gtSD(K69_AC ac, final List<K69> models, int sizePerOrder) {

        final Sampler_Multi s = of(gtSD, GProposals_Multi_K69.of_gtSD(ac, toArray(models)));

        s.setIteratorBySampleSize(ac.eventsToMRCA() * sizePerOrder);

        return s;
    }

    /**
     *
     * @param ac sample configuration
     * @param sizePerOrder
     * @return sampler based on EGT proposal
     */
    public static Sampler_Multi<?> of_gtHUW(K69_AC ac, final List<K69> models, int sizePerOrder) {

        final Sampler_Multi s = of(gtHUW, GProposals_Multi_K69.of_gtHUW(ac, toArray(models)));

        s.setIteratorBySampleSize(ac.eventsToMRCA() * sizePerOrder);

        return s;
    }

    private Samplers_Multi_K69() {}


    // <editor-fold desc="Readability Helpers">
    private static K69[] toArray(List<K69> models) {
        return models.toArray(new K69[0]);
    }

    private static Sampler_Multi of(String name, GProposal_Multi<K69_AC, K69> pDist) {
        return new Sampler_Multi(name, pDist, pDist.of_Factor(), Samplers_K69.TARGET_FUNC_ID);
    }    // </editor-fold>
}
