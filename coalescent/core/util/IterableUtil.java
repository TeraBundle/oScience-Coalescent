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

package coalescent.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Susanta Tewari
 * @since Feb 6, 2012
 */
public class IterableUtil {

    /**
     * Constructs ...
     *
     */
    private IterableUtil() {}

    /**
     * Creates a new iterable by filtering {@code col} with the given {@code predicate}.
     *
     * @param col collection
     * @param predicate predicate to use for filtering
     * @param <T> type parameter
     * @return filtered collection
     */
    public static <T> Collection<T> filter(final Collection<T> col,
            final PairPredicate<T> predicate) {

        final Collection<T> s1     = new ArrayList<>(col);
        final Collection<T> s2     = new ArrayList<>(col);
        final Collection<T> result = new ArrayList<>(s1.size());

        while (!s1.isEmpty()) {

            final T a = s1.iterator().next();

            for (Iterator<T> itr_s2 = s2.iterator(); itr_s2.hasNext(); ) {

                final T b = itr_s2.next();

                if (predicate.apply(a, b)) {

                    s1.remove(b);
                    itr_s2.remove();
                }
            }

            s1.remove(a);
            result.add(a);
        }

        return result;
    }
}
