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

import coalescent.recursion.Recursion;
import commons.util.MathUtil;

import java.util.Collections;
import java.util.List;

/**
 * @author Susanta Tewari
 */
public class ExactRecursionExeEvent {

    /** Field description */
    private final Recursion source;

    /** Field description */
    private final List<Integer> cacheSize;

    /** Field description */
    private final List<String> computationChunks;

    /**
     * @param source
     * @param cacheSize
     */
    public ExactRecursionExeEvent(final Recursion source, final List<Integer> cacheSize) {
        this(source, cacheSize, Collections.EMPTY_LIST);
    }

    /**
     * Constructs ...
     *
     *
     * @param source
     * @param cacheSize
     * @param computationChunks
     */
    public ExactRecursionExeEvent(final Recursion source, final List<Integer> cacheSize,
                                  final List<String> computationChunks) {

        if (source == null) {
            throw new NullPointerException("Source is null.");
        }

        this.source            = source;
        this.cacheSize         = Collections.unmodifiableList(cacheSize);
        this.computationChunks = Collections.unmodifiableList(computationChunks);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Recursion getSource() {
        return source;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public List<Integer> getCacheSize() {
        return cacheSize;
    }

    public long getTotalCacheSize() {
        return MathUtil.sumDoublePrecision(cacheSize).longValue();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public List<String> getComputationChunks() {
        return computationChunks;
    }
}
