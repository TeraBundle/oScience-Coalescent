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

package coalescent;

import coalescent.model.PopGenModel;
import coalescent.statistic.AC;

import java.math.BigDecimal;
import java.util.List;

/**
 * Describes a chain of events from the sample to the MRCA.
 *
 * @param <S>
 * @param <M>
 * @author Susanta Tewari
 * @version 1.0 Nov 17, 2010
 * @see Event
 */
public class Genealogy<S extends AC<S, M>, M extends PopGenModel> {

    private final List<Event<S>> eventChain;

    /**
     * @param eventChain vent chain from the sample to the MRCA
     */
    public Genealogy(final List<Event<S>> eventChain) {
        this.eventChain = eventChain;
    }

    /**
     * The probability of this genealogy. It is computed by the following formula:
     *
     * @return the probability of this genealogy
     */
    public BigDecimal probability() {

        BigDecimal prob = BigDecimal.valueOf(1.0);
        S s             = getSampleConfig();

        for (final Event<S> event : eventChain) {

            final S ancestralConfig          = event.getPost();
            final EventType type             = event.getType();
            final BigDecimal transition_prob = s.transitionProb(type, ancestralConfig);

            prob = prob.multiply(transition_prob);

            if (ancestralConfig.isMRCA()) {
                prob = prob.multiply(new BigDecimal(ancestralConfig.probAtMRCA()));
            }

            s = ancestralConfig;
        }

        return prob;
    }

    /**
     * The chain of events from the MRCA corresponding to this genealogy
     *
     * @return the chain of events corresponding to this genealogy
     */
    public List<Event<S>> getEventChain() {
        return eventChain;
    }

    /**
     * A textual representation of this genealogy. It has information on the sample configurations
     * and their event types starting from the sample configuration all the way up to the
     * MRCA. It does not end with a new line character.
     *
     * @return a textual representation of this genealogy
     */
    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder();
        final String delimiter      = "->";

        builder.append(getSampleConfig()).append(delimiter);

        for (final Event<S> event : eventChain) {
            builder.append(event).append(delimiter);
        }

        builder.delete(builder.lastIndexOf(delimiter), builder.length());

        return builder.toString();
    }

    /**
     * A textual representation of this genealogy with information on only the event types by their
     * abbreviations.
     *
     * @return a textual representation of this genealogy using abbreviation of the event types
     * @see EventType
     */
    public String toStringEventSeq() {

        final StringBuilder builder = new StringBuilder();

        for (final Event<S> event : eventChain) {
            builder.append(event.getType());
        }

        return builder.toString();
    }

    /**
     * the population genetic model under the genealogy is based on
     * @return
     */
    public M getModel() {
        return getSampleConfig().getModel();
    }

    public void setModel(final M m) {

        getSampleConfig().setModel(m);

        for (final Event<S> event : eventChain) {
            event.getPost().setModel(m);
        }
    }

    /** Field description */
    private S getSampleConfig() {
        return eventChain.get(0).getPre();
    }
}
