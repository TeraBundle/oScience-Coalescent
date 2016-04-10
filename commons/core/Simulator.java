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
 * @param <I> input type
 * @param <O> output type
 * @author Susanta Tewari
 * @version 1.2.3
 * @history Created on Sep 30, 2012.
 * @since 1.2.3
 */
public interface Simulator<I, O> {

    /**
     *
     *
     * @param input simulation input
     * @param simCount simulation count
     * @return a simulated instance
     */
    Iterable<O> simulate(final I input, int simCount);

    /**
     * Simulates data from the specified model.
     *
     * @param <M> model type
     * @param <D> data type
     */
    public static interface DATA<M extends Model, D extends Data<M, D>>
            extends Simulator<DataInput<M>, D> {}

    /**
     * @param <M>
     * @version 1.2.3
     * @since 1.2.3
     * @history Created on 03/22/2013
     * @author Susanta Tewari
     */
    public static class DataInput<M extends Model> {

        private final M model;
        private final int sampleSize;

        public DataInput(final M model, final int sampleSize) {

            this.model      = model;
            this.sampleSize = sampleSize;
        }

        public M getModel() {
            return model;
        }

        public int getSampleSize() {
            return sampleSize;
        }
    }
}
