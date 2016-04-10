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

package coalescent.recursion.listener.exe;

import java.util.List;

/**
 * @since 1.4.0
 * @version 1.4.2
 * @author Susanta Tewari
 * @history Created on Oct 28, 2012.
 */
public class ACinCacheCounter implements ExactRecursionExeListener {

    private ExactRecursionExeEvent event;

    @Override
    public void receivedExactRecursionExeEvent(final ExactRecursionExeEvent event) {
        this.event = event;
    }

    public List<Integer> getCacheSize() {
        return event.getCacheSize();
    }

    /**
     *
     * @return {@code true} if it has received at least one event; {@code false} otherwise
     */
    public boolean isReady() {
        return event != null;
    }

    public long getTotalCacheSize() {
        return event.getTotalCacheSize();
    }
}
