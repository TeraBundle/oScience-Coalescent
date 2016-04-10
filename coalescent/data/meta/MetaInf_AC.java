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

package coalescent.data.meta;

import coalescent.recursion.RecursionListener;

/**
 * @author Susanta Tewari
 * @version 1.4.2
 * @history Created on 11/25/13.
 * @since 1.4.2
 */
public interface MetaInf_AC {

    /**
     * Extracts meta-inf from the specified computation (optional)
     * @param computer computation
     */
    void processComputation(RecursionListener computer);
}
