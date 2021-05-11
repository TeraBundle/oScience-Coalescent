/*
 * Copyright 2020. All Rights Reserved.
 * TeraBundle Analytics Pvt. Ltd. http://www.terabundle.com
 * For queries, contact Dr. Susanta Tewari at tewaris@terabundle.com.
 */

package coalescent.model;

/**
 * @author Susanta Tewari
 * @version 1.0.0
 * @history Created on 10/16/2020.
 * @since 1.0.0
 */
public enum PopGen_EventType implements EventType<PopGen_EventType> {

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
    }
}
