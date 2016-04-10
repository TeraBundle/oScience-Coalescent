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

/**
 * Enumeration of the steps of the post-order recursion implementation of a sample configuration
 * used by the implementation to invoke the corresponding methods on the registered listeners.
 *
 * @author Susanta Tewari
 * @since 1.3.0 Dec 3, 2010
 */
public enum RecursionState {

    /**
     * hook for listeners to learn about the traversal before it (traversal) starts
     */
    INIT_RECURSION,

    /**
     * boundary condition of the recursion
     */
    BOUNDARY_CONDN,

    /**
     * right before starting to iterate on the events
     */
    ITR_EVENTS_ON,

    /**
     * Right after the event type has been determined
     */
    ITR_EVENT_TYPE_ON,

    /**
     * after the configuration is created & before it`s visited (i.e., its ancestors are visited)
     */
    PRE_VISIT_AC,

    /**
     * after the configuration is visited ( visiting may be skipped if in cache)
     */
    POST_VISIT_AC, ITR_EVENT_TYPE_OFF,

    /**
     * right after finished iterating on all events
     */
    ITR_EVENTS_OFF,

    /**
     * recursion finished
     */
    FINISHED_RECURSION
}
