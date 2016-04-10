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

/**
 * Manages bus related to {@code MetaInf_AC} for external systems.
 *
 * <p>
 *     Meta information acts as optional in most cases. It has also read/write attributes. It can
 *     supply a default instance when none is existing, especially during write. Overall, it
 *     simplifies access to meta-inf.
 * </p>
 *
 * @since 1.4.2
 * @version 1.4.2
 * @author Susanta Tewari
 * @history Created on 1/4/14.
 */
public interface MetaInf_Mngr<T extends MetaInf_AC> {

    /**
     * @return {@code true} if user wants to use meta-inf; {@code false} otherwise
     */
    boolean isActive();

    /**
     * @return {@code true} if meta information on the data is available; {@code false} otherwise
     */
    boolean hasInfo();

    /**
     * @return default new instance of the meta-inf when the writer is used for the first time
     */
    T getDefault();

    /**
     * @return meta-inf for {@link #getStatistic()}
     */
    T load();

    /**
     * @param metaInf metaInf to be store
     */
    void store(T metaInf);
}
