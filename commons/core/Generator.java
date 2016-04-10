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
 *
 * @param <I> input type
 * @param <O> output type
 * @version 1.2.3
 * @author Susanta Tewari
 * @history Created on 11/22/12
 * @since 1.2.3
 */
public interface Generator<I, O> {

    /**
     * @param input generator input
     * @param genCount number of outputs to be generated
     * @return outputs generated
     */
    Iterable<O> generate(final I input, final int genCount);

    /**
     * Model generator. Given a model it generates another.
     *
     * @param <M> model type
     */
    public static interface MODEL<I, M extends Model> extends Generator<I, M> {}
}
