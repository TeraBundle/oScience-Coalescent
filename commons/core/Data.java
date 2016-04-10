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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Base data class. This class and all its sub classes are immutable by contract.
 *
 * @param <M> data model
 * @param <R> runtime type of this class
 * @author Susanta Tewari
 * @since 1.1.0
 */
public abstract class Data<M extends Model, R extends Data<M, R>> implements Statistic<R, R> {

    /** Field description */
    private final M m;

    /**
     * Initializes the model instance.
     *
     * @param model the data model
     */
    public Data(final M model) {

        if (model == null) {
            throw new NullPointerException("parameter model is null");
        }

        this.m = model;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public M getModel() {
        return m;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public MutableData<M, R> getMutable() {
        return null;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public R getData() {
        return (R) this;
    }

    /**
     * Since data is the statistic here and it is assumed immutable, the argument is returned.
     *
     * @param data an instance of data
     * @return the passed instance of data
     */
    @Override
    @SuppressWarnings("unchecked")
    public R factory(final R data) {
        return data;
    }

    /**
     * The number of data elements present in the sample.
     *
     * @return the sample size
     */
    public abstract int getSampleSize();

    /**
     * Two {@code Data} instances are equal if they have equal models.
     *
     * @param obj the object to compare against
     * @return {@code false} if {@code obj} is either {@code null}, from a different class or have
     *         different model objects (specified by their {@code equals}), in this order; {@code
     *         true} otherwise
     */
    @Override
    public boolean equals(final Object obj) {

        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        @SuppressWarnings("unchecked") final Data<M, R> other = (Data<M, R>) obj;

        return new EqualsBuilder().append(m, other.m).isEquals();
    }

    /**
     * Computes hash code of the data model.
     *
     * @return a hash code of the data model
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(m).toHashCode();
    }

    /**
     * Contains information on data model.
     *
     * @return information on data model
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, Singletons.TO_STRING_STYLE).append("model", m).toString();
    }
}
