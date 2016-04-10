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

package commons.core;

/**
 * @param <D>
 * @param <R> a subtype of this type
 * @author Susanta Tewari
 */
public interface Statistic<D extends Data<? extends Model, D>, R extends Statistic<D, R>> {

    /**
     * Create a new instance of statistic from the specified instance of data.
     *
     * @param data an instance of data
     * @return a new instance of statistic based on the specified data
     */
    R factory(final D data);

    /**
     * Return the data this statistic is based on
     *
     * @return data of this statistic
     */
    D getData();

    /**
     * Create a mutable object from this object so that the mutable object can be edited and in
     * turn returns an immutable version of it. Note that this method always returns the same
     * mutable instance for every instance of this interface.
     *
     * @return a mutable version of this object or {@code null} if mutable versions are not
     *         supported
     */
    Mutable<R> getMutable();
}
