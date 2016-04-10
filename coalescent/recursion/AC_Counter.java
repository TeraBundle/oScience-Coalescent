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

/**
 * Computes the total number of sample configurations ancestral to a sample configuration by
 * observing its recursion (via the post-order implementation).
 *
 * @author Susanta Tewari
 * @version 1.0 Dec 6, 2010
 */
public class AC_Counter<C extends AC<C, ?>> extends RecursionComputer<C, Void, BigInteger> {

    public AC_Counter() {
        result = BigInteger.valueOf(0L);
    }

    /**
     * Total number of sample configurations ancestral (including the sample) to the sample
     * configuration being observed.
     *
     * @return total number of ancestral configurations for the sample configuration being observed
     */
    @Override
    public BigInteger getResult() {
        return result;
    }

    @Override
    public void onBoundaryCondn(final RecursionEvent<C> event) {
        result = result.add(BigInteger.valueOf(1L));
    }

    @Override
    public void onFinishedIteratingEvents(final RecursionEvent<C> event) {
        result = result.add(BigInteger.valueOf(1L));
    }

    @Override
    public String toString() {
        return "Total # of ancestral configurations: " + FormatUtil.format(result);
    }

    @Override
    public String getUpdateChunk() {
        return "Config count: " + result;
    }
}
