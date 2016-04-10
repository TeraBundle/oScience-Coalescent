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

import java.util.List;

/**
 * Model with an {@code order} parameter.
 *
 * @param <T> order type
 * @param <M> model type
 * @author Susanta Tewari
 * @version 1.2.3
 * @history Created on Sep 28, 2012.
 * @since 1.2.3
 */
public interface OrderModel<T, M extends Model> extends Model {

    /**
     * @return current order
     */
    List<T> getOrder();

    /**
     * @param order new order
     * @return a new model representing the specified order
     */
    M newOrder(List<T> order);
}
