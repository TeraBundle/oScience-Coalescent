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

/**
 * @param <T> type parameter
 * @since 1.2.1
 * @version 1.2.1
 * @author Susanta Tewari
 * @history Created on 10/13/12.
 */
public interface PairPredicate<T> {

    /**
     * Predicate based on a and b
     *
     * @param a value a
     * @param b value b
     * @return the predicate value of a & b
     */
    boolean apply(T a, T b);
}
