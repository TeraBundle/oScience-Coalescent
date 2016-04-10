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

package coalescent.model;

import coalescent.EventType;
import com.google.common.collect.ImmutableSet;
import commons.core.Model;
import commons.core.Singletons;
import commons.util.ExceptionUtil;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A composite model is a detailed population genetic model under study. It is collection of atomic
 * models with their behaviour customized to the model under study.
 *
 * @author Susanta Tewari
 * @version 1.0.0 Jul 15, 2010
 */
public abstract class PopGenModel implements Model {

    /** Field description */
    private final ImmutableSet eventTypes;

    /**
     *
     * @param types event types
     * @throws NullPointerException if, parameter is {@code null}
     * @throws IllegalArgumentException if, {@code types} is empty
     */
    protected PopGenModel(final EventType... types) {

        if (types == null) ExceptionUtil.throwIllegalArgExNull("types");
        if (types.length == 0) throw new IllegalArgumentException("empty types");

        eventTypes = ImmutableSet.copyOf(types);
    }

    /**
     * The probability of observing an event of the specified type next, when {@code n}
     * ancestors are present.
     *
     * @param type a population genetic event type
     * @param n number of ancestors present
     * @return the event probability
     */
    public abstract Double eventProb(EventType type, Integer n);

    /**
     * Event types supported by this model
     *
     * @return supported event types
     */
    public final ImmutableSet<EventType> getEventTypes() {
        return eventTypes;
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this, Singletons.TO_STRING_STYLE).append("event-types",
                                   eventTypes).toString();
    }
}
