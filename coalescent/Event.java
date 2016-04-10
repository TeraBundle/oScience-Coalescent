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

import coalescent.statistic.AC;

import java.util.Objects;

/**
 * A value object that describes the transition of one configuration (called {@code pre}) to
 * another (called {@code post}) as a result of an event during evolution.
 *
 * @param <S> extends AC;  the configuration type
 * @author Susanta Tewari
 * @version 1.4.2
 * @history First Oct 6, 2010.
 * @since 1.3.0
 */
public class Event<S extends AC<?, ?>> {

    private final S pre;
    private final S post;
    private final Object preAllele;    // allele on pre-config
    private final EventType type;    // pre-allele event type

    /**
     * @param pre pre-configuration
     * @param post post-configuration
     * @param preAllele allele in the pre-configuration on which the event took place
     * @param type type of the evolutionary event
     * @throws NullPointerException if any param is {@code null}
     */
    public Event(final S pre, final S post, final Object preAllele, final EventType type) {

        Objects.requireNonNull(pre);
        Objects.requireNonNull(post);
        Objects.requireNonNull(preAllele);
        Objects.requireNonNull(type);

        this.pre       = pre;
        this.post      = post;
        this.preAllele = preAllele;
        this.type      = type;
    }

    /**
     * @return pre configuration
     */
    public S getPre() {
        return pre;
    }

    /**
     * @return post configuration
     */
    public S getPost() {
        return post;
    }

    /**
     * @return allele on the pre config
     */
    public Object getPreAllele() {
        return preAllele;
    }

    /**
     * @return corresponding event type on the pre config
     */
    public EventType getType() {
        return type;
    }

    @Override
    public String toString() {

        return new StringBuilder(30).append("[").append(preAllele).append(",").append(type).append(
            ",").append(post).append("]").toString();
    }
}
