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

import java.util.ArrayList;
import java.util.List;

/**
 * @since 1.4.0
 * @version 1.4.2
 * @author Susanta Tewari
 * @history Created on Feb 7, 2013.
 */
public final class FocusedFwdCountChecker<C extends AC<C, ?>>
        extends RecursionComputer<C, Void, Void> {

    /** focused ancestral config */
    private final C focus;

    /** callers for the focus */
    private final List<C> callers;

    public FocusedFwdCountChecker(final C focus) {

        this.focus = focus;
        callers    = new ArrayList<>(10);
    }

    @Override
    public void onPreVisitAnsConfig(final RecursionEvent<C> event) {

        final C sampleConfig    = event.getSampleConfig();
        final C ancestralConfig = event.getAncestralConfig();

        if (ancestralConfig.equals(focus)) callers.add(sampleConfig);
    }

    public int getCallerCount() {
        return callers.size();
    }

    public List<C> getCallers() {
        return callers;
    }

    public C getFocus() {
        return focus;
    }
}
