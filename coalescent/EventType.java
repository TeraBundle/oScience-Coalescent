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

package coalescent;

/**
 * An enumeration of all type of population genetic events that are to be modelled and analyzed.<p>
 * A distinct abbreviation is defined for all event types that facilitates in printing.
 *
 * @author Susanta Tewari
 * @since 1.0.3 Oct 8, 2010
 */
public enum EventType {

    /**
     * This is the most common population genetic event that must be taken into account in any
     * analysis. The basic fact of biology that genes are correlated by ancestry needs to be
     * accounted for even in the most naive model.<p>
     * It has the abbreviation code C.
     */
    COALESCENT {

        @Override
        public String toString() {
            return "C";
        }
    },

    /**
     * The fact that all genes are not the same requires a theory of mutation. This is also a basic
     * population genetic event that needs to be included in any meaningful anaysis.<p>
     * It has the abbreviation code M.
     */
    MUTATION {

        @Override
        public String toString() {
            return "M";
        }
    },

    /**
     * The entire population of a species is not panmictic. One of the factors is location. Genes
     * that are close together reach their MRCA sooner than those further apart. This requires
     * a theory of migration.<p>
     * It has the abbreviation code MG.
     */
    MIGRATION {

        @Override
        public String toString() {
            return "MG";
        }
    },

    /**
     * Recombination event. It has the abbreviation code R.
     */
    RECOMBINATION {

        /**
         * Abbreviation code for this event.
         *
         * @return abbreviation code R
         */
        @Override
        public String toString() {
            return "R";
        }
    };
}
