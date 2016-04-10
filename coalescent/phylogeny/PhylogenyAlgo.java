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

package coalescent.phylogeny;

import coalescent.data.K69Data;
import com.google.common.collect.ImmutableList;
import commons.core.Singletons;
import commons.util.ExceptionUtil;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * It defines an interface for checking phylogeny of {@code K69Data}.
 * <p>
 * Note that checking phylogeny is equivalent to checking the infinite sites assumption, a feature
 * of the {@code K69} model. {@code K69Data} is supposed to follow the {@code K69} model and a
 * phylogeny test can be used to check if the data deviates from the model.
 *
 * @author Susanta Tewari
 * @version 1.0 Nov 24, 2010
 */
public enum PhylogenyAlgo {

    /**
     * {@code PhylogenyAlgo} algorithm from Dan Gusfield. The reference:
     * "Efficient Algorithms for Inferring  Evolutionary Trees", By Dan Gusfield, Networks Vol. 21 (1991) 19-28.
     */
    GUSFIELD {

        @Override
        public boolean isPhylogeny(final K69Data data) {

            final GusfieldAlgo algo = new GusfieldAlgo(data);

            return algo.isPhylogeny();
        }
    },

    /**
     * {@code PhylogenyAlgo} algorithm from Richard R. Hudson. The reference:
     * "Statistical Properties of the number of recombination events in the history of a sample
     * of DNA sequences", By Hudson et. al., Genetics Vol. 111 (1985) 147-164.
     */
    FOUR_GAMETES {

        @Override
        public boolean isPhylogeny(final K69Data data) {

            final FourGametesAlgorithm algo = new FourGametesAlgorithm();
            final boolean phylogeny         = algo.isPhylogeny(data, FOUR_GAMETES);

            return phylogeny;
        }
    };

    protected DebugInfo debugInfo;

    /**
     * Checks the phylogeny of the specified data.
     *
     * @param data data whose phylogeny is tested
     * @return {@code true} if {@code data} has phylogeny; {@code false} otherwise
     */
    public abstract boolean isPhylogeny(K69Data data);

    /**
     * The debugging information is only relevant to the last call of {@link #isPhylogeny(K69Data)}.
     *
     * @return instance containing information for investigating false phyloegny.
     */
    public DebugInfo getDebugInfo() {
        return debugInfo;
    }

    /**
     * Information for investifating false phylogeny.
     */
    public static class DebugInfo {

        private PhylogenyAlgo algo;
        private String[] sites   = new String[2];
        private String[] alleles = new String[4];
        private String currentAllele;

        public DebugInfo(PhylogenyAlgo algo, String[] sites, String[] alleles,
                         String currentAllele) {

            this.algo          = algo;
            this.sites         = sites;
            this.alleles       = alleles;
            this.currentAllele = currentAllele;
        }

        public PhylogenyAlgo getAlgo() {
            return algo;
        }

        public String getSite1() {
            return sites[0];
        }

        public String getSite2() {
            return sites[1];
        }

        public String getAllele1() {
            return alleles[0];
        }

        public String getAllele2() {
            return alleles[1];
        }

        public String getAllele3() {
            return alleles[2];
        }

        public String getAllele4() {
            return alleles[3];
        }

        public String getCurrentAllele() {
            return currentAllele;
        }

        @Override
        public String toString() {

            final ToStringBuilder builder = new ToStringBuilder(this, Singletons.TO_STRING_STYLE);

            return builder.append("algo", algo).append("sites", sites).append("alleles",
                                  alleles).append("currentAllele", currentAllele).toString();
        }
    }

    /**
     * An implementation of {@code PhylogenyAlgo} from the following reference.
     * "Statistical Properties of the number of recombination events in the history of a sample
     * of DNA sequences", By Hudson et. al., Genetics Vol. 111 (1985) 147-164.
     *
     * @author Susanta Tewari
     * @version 1.0 Nov 24, 2010
     */
    private static class FourGametesAlgorithm {

        private final static Logger LOGGER = ExceptionUtil.getLogger(FourGametesAlgorithm.class);

        public boolean isPhylogeny(final K69Data data, PhylogenyAlgo algo) {

            final K69Data.ArrayData arrayData = data.getArrayData();
            final int[][] s                   = arrayData.getS();
            final List<String> alleles        = ImmutableList.copyOf(data.getAlleles());

            for (int i = 0; i < s[0].length - 1; i++) {

                for (int j = i + 1; j < s[0].length; j++) {


                    // for sites i and j, scan for all four gamete patterns
                    boolean gamete1 = false,
                            gamete2 = false,
                            gamete3 = false,
                            gamete4 = false;


                    // alleles that witnessed the gametes first
                    int k1 = 0,
                        k2 = 0,
                        k3 = 0,
                        k4 = 0;

                    for (int k = 0; k < s.length; k++) {

                        if ((s[k][i] == 0) && (s[k][j] == 0) &&!gamete1) {

                            k1      = k;
                            gamete1 = true;

                        } else if ((s[k][i] == 0) && (s[k][j] == 1) &&!gamete2) {

                            k2      = k;
                            gamete2 = true;

                        } else if ((s[k][i] == 1) && (s[k][j] == 0) &&!gamete3) {

                            k3      = k;
                            gamete3 = true;

                        } else if ((s[k][i] == 1) && (s[k][j] == 1) &&!gamete4) {

                            k4      = k;
                            gamete4 = true;
                        }

                        if (gamete1 && gamete2 && gamete3 && gamete4) {

                            final List<String> mutations = data.getMutations();
                            final String[] sites = new String[] { mutations.get(i),
                                    mutations.get(j) };
                            final String[] allele_s = new String[] { alleles.get(k1),
                                    alleles.get(k2), alleles.get(k3), alleles.get(k4) };
                            final String currentAllele   = alleles.get(k);

                            algo.debugInfo = new DebugInfo(FOUR_GAMETES, sites, allele_s,
                                                           currentAllele);

                            LOGGER.log(Level.FINE, "NotPhylogenyWhy", new Object[] {

                                (i + 1), (j + 1), alleles.get(k1), alleles.get(k2), alleles.get(k3),
                                alleles.get(k4)

                            });

                            return false;
                        }
                    }
                }
            }

            return true;
        }
    }
}
