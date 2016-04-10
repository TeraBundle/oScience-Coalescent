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
import com.google.common.base.Function;

import java.util.Map;

/**
 *
 *
 * @version 1.4.2
 * @author Susanta Tewari
 * @history Created on 4/3/13
 * @since 1.4.0
 */
public class ExactRecursionListeners {

    /**
     * @version 1.4.2
     * @since 1.4.0
     * @history Created on 04/03/2013
     * @author Susanta Tewari
     */
    public static interface ME<C extends AC<C, ?>> extends RecursionListener<C> {

        /**
         * removes the given config from cache
         * @param s config to be removed
         */
        void removeAC(C s);
    }

    /**
     * @param <X>
     * @param <Y>
     * @version 1.4.2
     * @since 1.4.0
     * @history Created on 04/03/2013
     * @author Susanta Tewari
     * @param <T>
     */
    public static interface MT<C extends AC<C, ?>, X, Y, T extends MT<C, X, Y, T>>
            extends RecursionListener<C> {

        T of(final Function<C, X> cacheValFunc);

        X getValue(C s);

        Map<C, X> getCache();

        C getSampleConfig();

        Y getResult();
    }

    /**
     * @param <X>
     * @param <Y>
     * @version 1.4.2
     * @since 1.4.0
     * @history Created on 04/03/2013
     * @author Susanta Tewari
     * @param <T>
     */
    public static interface MT_ME<C extends AC<C, ?>, X, Y, T extends MT_ME<C, X, Y, T>>
            extends MT<C, X, Y, T>, ME<C> {

        @Override
        T of(final Function<C, X> cacheValFunc);
    }
}
