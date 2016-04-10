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

package coalescent.recursion.listener;

import coalescent.recursion.RecursionComputer;
import coalescent.recursion.RecursionEvent;
import coalescent.statistic.AC;

import java.util.*;

/**
 *
 *
 * @version 1.4.2
 * @author Susanta Tewari
 * @param <C>
 * @history Created on 11/7/12
 * @since 1.4.0
 */
public class FwdCountChecker<C extends AC<C, ?>> extends RecursionComputer<C, Collection<C>, Void> {

    private final Map<C, Collection<C>> callers = new HashMap<>();

    @Override
    public void onInitRecursion(final RecursionEvent<C> event) {

        final C ac = event.getSrc().getSampleConfig();

        getCache().put(ac, new ArrayList<C>(2));
    }

    @Override
    public void onPreVisitAnsConfig(final RecursionEvent<C> event) {


        // get sample & ancestral config
        final C sampleConfig    = event.getSampleConfig();
        final C ancestralConfig = event.getAncestralConfig();


        // add callers.
        if (!getCache().containsKey(ancestralConfig))
            getCache().put(ancestralConfig, new ArrayList<C>());

        getCache().get(ancestralConfig).add(sampleConfig);
    }

    /**
     *
     * @param callee statistic that is called
     * @return total number of callers for the specified callee
     */
    public int getCallerCount(final AC<?, ?> callee) {
        return getCache().get(callee).size();
    }

    public Set<C> getCallee() {
        return getCache().keySet();
    }
}
