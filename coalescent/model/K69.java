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

/**
 * This model was first proposed by Kimura (1969, Genetics) and popularly known as
 * infinite sites model. The following is the specification of the model.
 * <ol>
 * <li> Panmictic population
 * <li> Mutations are always on a new site.
 * <li> Alleles are selectively neutral
 * <li> Genes are passed from parents to offsprings exactly. Hence the reproduction system is
 * effectively that of a haploid.
 * </ol>
 *
 * @author Susanta Tewari
 * @version 1.0.0 Jul 2, 2010
 */
public class K69 extends KC64 {

    /**
     * Constructs ...
     *
     *
     * @param theta
     * @throws IllegalArgumentException if, <ol> <li>{@code mutationRate} &lt 0</li></ol>
     */
    public K69(final double theta) {
        super(theta);
    }
}
