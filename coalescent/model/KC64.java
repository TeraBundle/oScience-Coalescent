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

package coalescent.model;

import coalescent.EventType;
import commons.core.Singletons;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import static commons.util.ExceptionUtil.throwArgEx;

/**
 * This model was first proposed by Kimura and Crow (1964, Genetics) and popularly known as
 * infinite alleles model. The following is the specification of the model.
 * <ol>
 * <li> Panmictic population
 * <li> Mutations create new alleles (not existing in the current population nor in the previous
 * ones)
 * <li> Alleles are selectively neutral
 * <li> Genes are passed from parents to offsprings exactly. Hence the reproduction system is
 * effectively that of a haploid i.e., no recombination is present.
 * </ol>
 *
 * @author Susanta Tewari
 * @version 1.0.0 Jul 2, 2010
 */
public class KC64 extends PopGenModel {

    /**
     * population mutation rate &theta; = 4Nu
     * N = effective population size, u = mutation rate/generation/chromosome.
     */
    private final double theta;

    /**
     * Creates model KC64 with known parameter &theta;
     *
     * @param mutationRate mutation parameter
     * @throws IllegalArgumentException if, <ol> <li>{@code mutationRate} &lt 0</li></ol>
     */
    public KC64(final double mutationRate) {

        super(EventType.COALESCENT, EventType.MUTATION);

        throwArgEx(mutationRate < 0, getClass(), "KC64_Theta_Positive", mutationRate);

        this.theta = mutationRate;
    }

    /**
     * Method description
     *
     *
     * @param type
     * @param n
     *
     * @return
     */
    @Override
    public Double eventProb(final EventType type, final Integer n) {

        switch (type) {

            case COALESCENT :
                return coalescentProb(n);

            case MUTATION :
                return mutationProb(n);

            default :
                throw new RuntimeException("wrong event type: " + type);
        }
    }

    /**
     * The probability of observing a coalescent event next among the competing events in the model
     * in a sample of given size. It is given by the following formula.
     * (n - 1) / ( n - 1 + theta ) (n = sample size)
     *
     * @param n number of ancestral lineages
     * @return the probability of observing a coalescent event next
     */
    private Double coalescentProb(final Integer n) {
        return ((n - 1) / (n - 1 + theta));
    }

    /**
     * The probability of observing a mutation event next among the competing events in the model in
     * a sample of given size. It is given by the following formula.
     * theta / ( n - 1 + theta ) (n = sample size)
     *
     * @param n number of ancestral lineages
     * @return the probability of observing a mutation event next
     */
    private Double mutationProb(final Integer n) {
        return (theta / (n - 1 + theta));
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) return false;

        final KC64 rhs = (KC64) o;

        return new EqualsBuilder().append(theta, rhs.theta).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(theta).toHashCode();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Double getMutationRate() {
        return theta;
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this, Singletons.TO_STRING_STYLE).appendSuper(
            super.toString()).append("mutation-rate", theta).toString();
    }
}
