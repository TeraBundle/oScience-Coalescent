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

import static coalescent.is.GProposals_K69.of_HUW;
import static coalescent.is.GProposals_K69.of_SD;

import java.util.HashMap;
import java.util.Map;

/**
 * @since since
 * @version version
 * @author Susanta Tewari
 * @history Created on 6/23/2014.
 */
public class GProposals_Composite_K69 {

    public enum SD_HUW { SD, HUW }

    public static GProposal_Composite<SD_HUW, K69_AC, K69> of_SD_HUW(final K69_AC sample) {

        Map<SD_HUW, GProposal<K69_AC, K69>> proposals = new HashMap<>();

        proposals.put(SD_HUW.SD, of_SD(sample));
        proposals.put(SD_HUW.HUW, of_HUW(sample));

        return new GProposal_Composite<SD_HUW, K69_AC, K69>(sample, proposals) {

            @Override
            protected SD_HUW selectProposal(K69_AC config) {

                final int n = config.getN();
                final int m = config.getSn();

                return (n - 1) / 2.0 > m ? SD_HUW.SD : SD_HUW.HUW;
            }
        };
    }

    /**
     * To prevent instantiation.
     */
    private GProposals_Composite_K69() {}
}
