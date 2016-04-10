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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An adapter for the listener interface of the post-order recursion implementation.<p> It provides
 * default implementations for all the methods and the subclasses can override only the methods they
 * are interested in. Subclasses should look at the javadoc of the overriding method to see if the
 * {@code super} method needs to be called. Failing to do so may throw {@code
 * IllegalStateException}.
 *
 * @author Susanta Tewari
 * @version 1.0 Dec 4, 2010
 * @param <X> Value type against AC
 * @param <Y> Result type
 */
public abstract class RecursionComputer<C extends AC<C, ?>, X, Y>
        implements ExactRecursionListeners.ME<C> {

    private final Map<C, X> cache = new ConcurrentHashMap<>();
    protected Y result;
    protected C sampleConfig;
    private boolean recursionFinished = false;

    public X getValue(final C s) {
        return cache.get(s);
    }

    @Override
    public void removeAC(final C s) {
        getCache().remove(s);
    }

    public Map<C, X> getCache() {
        return cache;
    }

    public final void clearCache() {
        cache.clear();
    }

    public Y getResult() {
        return result;
    }

    public C getSampleConfig() {
        return sampleConfig;
    }

    /**
     * An empty string. The subclasses should override this method to provide a brief description on
     * the results.<p> Must be called by the overriding method as the first statement.
     *
     * @return empty string
     */
    @Override
    public String toString() {

        if (!recursionFinished) throw new IllegalStateException("early call");

        return "";
    }

    @Override
    public String getDescription() {
        return "RecursionComputer";
    }

    @Override
    public String getUpdateChunk() {
        return null;
    }

    /**
     * {@inheritDoc}<p> Must be called by the overriding method as the first statement.
     */
    @Override
    public void onFinishedRecursion(final RecursionEvent<C> event) {
        recursionFinished = true;
    }


    // <editor-fold desc="Empty Stub">
    @Override
    public void onInitRecursion(final RecursionEvent<C> event) {}

    @Override
    public void onBoundaryCondn(final RecursionEvent<C> event) {}

    @Override
    public void onStartingIteratingEvents(final RecursionEvent<C> event) {}

    @Override
    public void onIteratingSingleEventTypes(final RecursionEvent<C> event) {}

    @Override
    public void onPreVisitAnsConfig(final RecursionEvent<C> event) {}

    @Override
    public void onPostVisitAnsConfig(final RecursionEvent<C> event) {}

    @Override
    public void onFinishedIteratingSingleEventTypes(final RecursionEvent<C> event) {}

    @Override
    public void onFinishedIteratingEvents(final RecursionEvent<C> event) {}    // </editor-fold>
}
