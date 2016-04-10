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

package commons.is;

import java.math.BigDecimal;

/**
 * Implement this interface to provide efficient implementation of computing importance factor. Note
 * that the default implementation
 * <pre>
 *
 *   P(X)/Q(X)
 *
 *   P(X) = target distribution (X->R)
 *
 *   Q(X) = proposal distribution (X->R)
 *
 * </pre>
 * is automatically provided by {@code Sampler}.
 *
 * @param <X> domain of sampling
 * @version 1.4.2
 * @author Susanta Tewari
 * @since 1.4.2
 * @see Sampler
 */
public interface Factor<X> {

    /**
     * Computes the importance factor for the specified domain value.
     *
     * @param x domain value
     * @return importance factor for the specified domain value
     */
    BigDecimal getValue(X x);
}
