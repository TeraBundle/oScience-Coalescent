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

import java.util.ArrayList;
import java.util.List;

/**
 * Builds all the sample configurations ancestral to a sample configuration (in backwards order) by
 * observing its recursion (via post-order implementation).
 *
 * @author Susanta Tewari
 * @version 1.0 Dec 3, 2010
 */
public final class AC_Builder<C extends AC<C, ?>> extends RecursionComputer<C, Void, List<C>> {

    public AC_Builder() {
        result = new ArrayList<>();
    }

    /**
     * All sample configurations ancestral to the sample configuration being observed. The
     * ancestral configurations are ordered backwards.
     *
     * @return ancestral configurations for the sample configuration being observed
     */
    @Override
    public List<C> getResult() {
        return result;
    }

    @Override
    public void onBoundaryCondn(final RecursionEvent<C> event) {
        result.add(event.getSampleConfig());
    }

    @Override
    public void onFinishedIteratingEvents(final RecursionEvent<C> event) {
        result.add(event.getSampleConfig());
    }

    @Override
    public String toString() {

        super.toString();

        final StringBuilder builder = new StringBuilder();

        builder.append("Ancestral Configurations:\n");

        for (final AC config : result) {
            builder.append(config + "\n");
        }

        builder.deleteCharAt(builder.length() - 1);

        return builder.toString();
    }
}
