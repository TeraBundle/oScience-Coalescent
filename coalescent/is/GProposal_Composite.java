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
import coalescent.model.PopGenModel;
import coalescent.statistic.AC;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @since since
 * @version version
 * @author Susanta Tewari
 * @history Created on 6/23/2014.
 */
public abstract class GProposal_Composite<E extends Enum<E>, S extends AC<S, M>,
        M extends PopGenModel> extends GProposal<S, M> {

    private final Map<E, GProposal<S, M>> proposals;

    /**
     *
     * @param sample sample
     */
    public GProposal_Composite(S sample, Map<E, GProposal<S, M>> proposals) {

        super(sample);

        this.proposals = proposals;
    }

    protected abstract E selectProposal(S config);

    @Override
    protected final BigDecimal proposalWeight(S config, Object allele, EventType eventType) {

        final GProposal<S, M> dProposal = proposals.get(selectProposal(config)); // get the delegate proposal

        return dProposal.proposalWeight(config, allele, eventType);
    }


}
