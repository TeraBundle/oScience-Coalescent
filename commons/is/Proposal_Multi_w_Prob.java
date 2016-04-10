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

package commons.is;

import java.math.BigDecimal;

/**
 * Proposal distribution that can compute probability for its drawn samples.
 *
 * @param <X> domain of sampling
 * @history First created on 01/25/2014
 * @since 1.4.2
 * @version 1.4.2
 */
public interface Proposal_Multi_w_Prob<X> extends Proposal_Multi<X> {

    /**
     * @param x domain value obtained by the last call of {@link #sample()}
     * @return probability of {@code x} under this proposal
     */
    BigDecimal[] probability(X[] x);

    Factor_Multi<X> of_Factor();
}
