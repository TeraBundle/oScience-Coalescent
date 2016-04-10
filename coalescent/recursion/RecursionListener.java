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

/**
 * Defines an interface for listeners that may want to trace the post-order recursion
 * implementation.<p>
 *
 * @author Susanta Tewari
 * @version 1.0 Dec 3, 2010
 */
public interface RecursionListener<C extends AC<C, ?>> {

    /**
     * A brief description of what the listener`s job.
     *
     * @return a brief description of what the listener`s job.
     */
    String getDescription();

    /**
     * @param event event of type {@link RecursionState#INIT_RECURSION}
     */
    void onInitRecursion(RecursionEvent<C> event);

    /**
     * Invoked when boundary condition event occurs during the traversal of the recursion.
     *
     * @param event @param event event of type {@link RecursionState#BOUNDARY_CONDN}
     */
    void onBoundaryCondn(RecursionEvent<C> event);

    /**
     * invoked when iteration of the ancestral configs are about to start during the traversal of
     * the recursion.
     *
     * @param event @param event event of type {@link RecursionState#ITR_EVENTS_ON}
     */
    void onStartingIteratingEvents(RecursionEvent<C> event);

    /**
     * Invoked when iteration of the event types for a sample configuration are about to start
     * during the traversal of the recursion.
     *
     * @param event @param event event of type {@link RecursionState#ITR_EVENT_TYPE_ON}
     */
    void onIteratingSingleEventTypes(RecursionEvent<C> event);

    /**
     * As defined in {@link RecursionState#PRE_VISIT_AC}.
     *
     * @param event @param event event of type {@link RecursionState#PRE_VISIT_AC}
     */
    void onPreVisitAnsConfig(RecursionEvent<C> event);

    /**
     * As defined in {@link RecursionState#POST_VISIT_AC}.
     *
     * @param event @param event event of type {@link RecursionState#POST_VISIT_AC}
     */
    void onPostVisitAnsConfig(RecursionEvent<C> event);

    /**
     * Invoked when iteration of the event types for a sample configuration are about to start
     * during the traversal of the recursion.
     *
     * @param event @param event event of type {@link RecursionState#ITR_EVENT_TYPE_OFF}
     */
    void onFinishedIteratingSingleEventTypes(RecursionEvent<C> event);

    /**
     * Invoked right after all the events for the sample config are processed.
     *
     * @param event @param event event of type {@link RecursionState#ITR_EVENTS_OFF}
     */
    void onFinishedIteratingEvents(RecursionEvent<C> event);

    /**
     * Invoked when iteration-finished event occurs during the traversal of the recursion.
     *
     * @param event @param event event of type {@link RecursionState#FINISHED_RECURSION}
     */
    void onFinishedRecursion(RecursionEvent<C> event);

    /**
     * an update on the listener`s computation
     *
     * @return an update on the listener`s computation or {@code null} to indicate none is available
     *         or provided.
     */
    String getUpdateChunk();
}
