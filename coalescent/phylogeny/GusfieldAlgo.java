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
import coalescent.phylogeny.GeneTree.Edge;
import coalescent.phylogeny.GeneTree.Node;
import commons.util.ArrayUtil;
import org.javatuples.Pair;

import java.util.*;

/**
 * An algorithm [1] by Dan Gusfield to test and build phylogeny for infinite-sites data.<p>
 * [1] "Efficient Algorithms for Inferring Evolutionary Trees", By Dan Gusfield, Networks Vol. 21
 * (1991) 19-28.
 *
 * @author Susanta Tewari
 * @version 1.3.0 June 30, 2011
 * @since 1.3.0 Oct 28, 2010
 */
public class GusfieldAlgo {

    private final int[][] mPrime;

    /** characters for mPrime */
    private final String[] mPrime_characters;
    private final K69Data.ArrayData arrayData;
    private final int[][] LArray;
    private final int[] L;

    /**
     * Constructs ...
     *
     *
     * @param data
     */
    public GusfieldAlgo(final K69Data data) {

        this.arrayData = data.getArrayData();

        final int[][] M             = arrayData.getS();
        final String[] m_characters = arrayData.getMutations();


        // STEP 1: Treat columns as binary numbers, considering the first row containing the most
        // significant bit. Sort these numbers in decresing order to sort the columns accordingly.
        // mPrime_characters would contain the sorted characters.
        // key: binary number value: column index, used in sorting column of M
        final Map<Integer, Integer> indexMap    = new HashMap<>();
        final Map<Integer, String> characterMap = new HashMap<>();
        String binaryString                     = "";

        for (int j = 0; j < M[0].length; j++) {

            binaryString = "";

            for (int[] aM : M) {
                binaryString += "" + aM[j];
            }

            final Integer number = Integer.valueOf(binaryString, 2);

            if (characterMap.containsKey(number)) {

                characterMap.put(number,
                                 characterMap.get(number) + Node.MC_PATH_DELIM + m_characters[j]);

            } else {
                characterMap.put(number, "" + m_characters[j]);
            }

            indexMap.put(number, j);
        }


        // sorted map with decreasing order
        final SortedMap<Integer, Integer> sortedMap = new TreeMap<>(Collections.reverseOrder());

        sortedMap.putAll(indexMap);

        final SortedMap<Integer, String> sortedCharMap = new TreeMap<>(Collections.reverseOrder());

        sortedCharMap.putAll(characterMap);

        this.mPrime_characters = sortedCharMap.values().toArray(new String[0]);


        // STEP 2: sort the columns of M, removing the duplicate columns, Call the new matrix M'
        mPrime = new int[M.length][sortedMap.size()];

        int counter = 0;

        for (final Integer key : sortedMap.keySet()) {

            final int colIndex = sortedMap.get(key);

            for (int i = 0; i < mPrime.length; i++) {
                mPrime[i][counter] = M[i][colIndex];
            }

            counter++;
        }


        // STEP 3: L[i][j] = Max (k) | k < j AND M'[i][k-1] == 1
        // = 0 if no such k exists
        // Here k is position rather than array index
        LArray = new int[mPrime.length][mPrime[0].length];
        L      = new int[mPrime[0].length];

        for (int j = 0; j < mPrime[0].length; j++) {

            int maxJ = 0;

            for (int i = 0; i < mPrime.length; i++) {

                int maxK = 0;

                for (int k = 0; k < j; k++) {

                    if (mPrime[i][k] == 1) {
                        maxK = k + 1;
                    }
                }

                LArray[i][j] = maxK;

                if ((mPrime[i][j] == 1) && (LArray[i][j] > maxJ)) {
                    maxJ = LArray[i][j];
                }
            }

            L[j] = maxJ;
        }
    }

    /**
     * @return {@code true} if data has phylogeny; {@code false} otherwise
     */
    public boolean isPhylogeny() {

        for (int i = 0; i < mPrime.length; i++) {

            for (int j = 0; j < mPrime[0].length; j++) {

                if ((mPrime[i][j] == 1) && (LArray[i][j] != L[j])) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * @return root node and frequency map; or {@code null} if data lacks phylogeny
     */
    public Pair<Node, Map<Node, Integer>> buildGeneTree() {

        if (!isPhylogeny()) return null;


        // create root
        final Node root = new Node(null, "r");


        // create the nodes and edges based on L()
        for (int i = 0; i < L.length; i++) {

            if (L[i] == 0) {
                root.addChild("n" + (i + 1), mPrime_characters[i]);
            } else {

                final Node lookupNode = root.findNode("n" + L[i]);

                lookupNode.addChild("n" + (i + 1), mPrime_characters[i]);
            }
        }


        // create vector c using M'
        final int[] c = new int[mPrime.length];

        for (int i = 0; i < mPrime.length; i++) {

            int position = 0;

            for (int j = 0; j < mPrime[0].length; j++) {

                if (mPrime[i][j] == 1) {
                    position = (j + 1);
                }
            }

            c[i] = position;
        }


        // process edges in c
        final String[] alleles = arrayData.getAlleles();

        for (int i = 0; i < c.length; i++) {

            final String nodeLabel = alleles[i];

            if (c[i] == 0) {

                root.addChild(nodeLabel, Edge.NO_MUTATION_CHARACTER);

                continue;
            }

            final Edge lookedupEdge = root.findEdge(mPrime_characters[c[i] - 1]);
            final Node edgeHead     = lookedupEdge.getChild();

            if (edgeHead.isLeaf()) {
                edgeHead.setLabel(nodeLabel);
            } else {
                edgeHead.addChild(nodeLabel, Edge.NO_MUTATION_CHARACTER);
            }
        }


        // set the frequency
        final List<Node> alleleNodes  = root.leafs();
        final Map<Node, Integer> freq = new HashMap<>();
        final int[] n                 = arrayData.getN();

        for (int i = 0; i < alleles.length; i++) {

            for (final Node node : alleleNodes) {

                if (node.getLabel().equals(alleles[i])) {

                    freq.put(node, n[i]);

                    break;
                }
            }
        }

        return new Pair<>(root, freq);
    }

    public int[][] getMPrime() {
        return ArrayUtil.clone(mPrime);
    }

    public int[] getL() {
        return L.clone();
    }

    /**
     * Mutation labels sorted after the columns of M'. Mutation labels for the same columns have
     * been concatenated by the string {@link coalescent.phylogeny.GeneTree.Node#MC_PATH_DELIM
     * Node#MC_PATH_DELIM}
     *
     * @return
     */
    public String[] getMPrimeCharacters() {
        return mPrime_characters.clone();
    }
}
