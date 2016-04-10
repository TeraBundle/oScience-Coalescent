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

package coalescent.data;

import coalescent.model.K69;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import commons.core.MutableData;
import commons.util.ExceptionUtil;
import commons.util.PrintUtil;
import org.apache.commons.lang.StringUtils;

import java.util.*;

import static commons.util.ExceptionUtil.throwArgEx;

/**
 * Data corresponding to the model {@link coalescent.model.K69 K69}. It displays immutability.
 * {@code K69Data} may not create a phylogeny and this can be verified by the following code.
 * <pre>
 *     CheckPhylogeny.getInstance(this).isPhylogeny()
 * </pre>
 *
 * <p>
 * This is recognized as a short coming which will be fixed ({@code JIRA Issue COALESCENT-66}) in a
 * later version.
 * </p>
 *
 * <p>
 * It follows some invariants which are checked during object creation. They are:
 * <ul>
 *  <li> all sequences are of the same length and there are no duplicates</li>
 *  <li> only segregating sites are present </li>
 * </ul>
 * </p>
 *
 * @author Susanta Tewari
 * @version 1.4.2
 * @since 1.0.0
 * @history First on Jul 2, 2010.
 */
public class K69Data extends KC64Data<K69, K69Data> {


    // todo refactor: efficient data structure. Map<string,Object[]> as in BG00Data

    /**
     * key: allele label value: allele sequence
     */
    private final ImmutableMap<String, String> alleleSeq;

    /**
     * key: allele label value: allele frequency
     */
    private final ImmutableMap<String, Integer> alleleFreq;
    private final ImmutableSet<String> alleleLabels;
    private final ImmutableList<String> mutationLabels;

    protected K69Data(final K69 model, final ImmutableMap<String, String> alleleSeq,
                      final ImmutableMap<String, Integer> alleleFreq,
                      final ImmutableSet<String> alleleLabels,
                      final ImmutableList<String> mutationLabels) {

        super(model, alleleFreq);

        this.alleleSeq      = alleleSeq;
        this.alleleFreq     = alleleFreq;
        this.alleleLabels   = alleleLabels;
        this.mutationLabels = mutationLabels;
    }

    public K69Data(final double theta, final String[] alleleSeq, final String[] alleleFreq) {
        this(theta, createAlleleLabels(alleleSeq.length), alleleSeq, alleleFreq);
    }

    /**
     * Constructs ...
     *
     * @param theta description
     * @param alleleLabels description
     * @param alleleSeq description
     * @param alleleFreq description
     */
    public K69Data(final double theta, final String[] alleleLabels, final String[] alleleSeq,
                   final String[] alleleFreq) {

        this(theta, ImmutableSet.copyOf(alleleLabels), convertAlleleSeq(alleleLabels, alleleSeq),
             convertAlleleFreq(alleleLabels, alleleFreq), createMutations(alleleSeq[0].length()));
    }

    /**
     * Constructs ...
     *
     *
     *
     * @param theta description
     * @param alleleLabels
     * @param alleleSeq
     * @param alleleFreq
     * @param mutations
     */
    public K69Data(final double theta, final String[] alleleLabels, final String[] alleleSeq,
                   final String[] alleleFreq, final String[] mutations) {

        this(theta, ImmutableSet.copyOf(alleleLabels), convertAlleleSeq(alleleLabels, alleleSeq),
             convertAlleleFreq(alleleLabels, alleleFreq), Arrays.asList(mutations));
    }

    /**
     * A detailed constructor, typically used by data drivers.
     *
     *
     * @param theta description
     * @param alleles
     * @param alleleSeq key: allele label; value: allele sequence
     * @param alleleFreq key: allele label; value: allele frequency
     * @param mutations of the parameters is {@code null}
     * @throws NullPointerException if any
     * @throws IllegalArgumentException if,
     * <ol>
     *     <ol> <li>{@code mutationRate} &lt 0</li></ol>
     *     <li>
     *         {@code alleles.size() != alleleSeq.size() != alleleFreq.size()} and/or they
     *         are empty
     *     </li>
     *     <li>all sequences are not of the same length</li>
     *     <li>{@code mutations.size != sequence length}</li>
     *      <li>a non-segregating site is present</li>
     *      <li>there is a duplicate sequence</li>
     * </ol>
     * <li>
     */
    @SuppressWarnings("TypeMayBeWeakened")
    public K69Data(final double theta, final Set<String> alleles,
                   final Map<String, String> alleleSeq, final Map<String, Integer> alleleFreq,
                   final List<String> mutations) {

        super(new K69(theta), alleleFreq);


        // null
        Objects.requireNonNull(alleles);
        Objects.requireNonNull(alleleSeq);
        Objects.requireNonNull(alleleFreq);
        Objects.requireNonNull(mutations);


        // empty
        throwArgEx(alleles.isEmpty(), "empty alleles");
        throwArgEx(alleleSeq.isEmpty(), "empty alleleSeq");
        throwArgEx(alleleFreq.isEmpty(), "empty alleleFreq");
        throwArgEx(mutations.isEmpty(), "empty mutations");


        // alleles.size() != alleleSeq.size() != alleleFreq.size()
        if ((alleles.size() != alleleSeq.size()) || (alleles.size() != alleleFreq.size())) {

            throw new IllegalArgumentException("Different lengths: " + " alleles: " + alleles
                                               + " alleleSeq: " + alleleSeq + " alleleFreq: "
                                               + alleleFreq);
        }


        // all sequences are not of the same length
        final String an_allele = alleles.iterator().next();
        final int seqLength    = alleleSeq.get(an_allele).length();

        for (final String allele : alleleSeq.keySet()) {

            if (alleleSeq.get(allele).length() != seqLength) {

                throw new IllegalArgumentException("Length of the sequence is different from the "
                                                   + "length of the previous alleles for allele "
                                                   + allele);
            }
        }


        // mutations.size != seqLength
        if (mutations.size() != seqLength) {
            throw new IllegalArgumentException("mutations.size != sequence length");
        }


        // a non-segregating site is present
        for (int i = 0; i < seqLength; i++) {

            final char site        = alleleSeq.get(an_allele).charAt(i);
            boolean nonSegregating = true;

            for (final String allele : alleleSeq.keySet()) {

                if (alleleSeq.get(allele).charAt(i) != site) {
                    nonSegregating = false;
                }
            }

            if (nonSegregating) {
                throw new IllegalArgumentException("Non-segregating at site " + (i + 1));
            }
        }


        // there is a duplicate sequence
        for (final String allele : alleleSeq.keySet()) {

            if (Collections.frequency(alleleSeq.values(), alleleSeq.get(allele)) > 1) {
                throw new IllegalArgumentException("Duplicate sequence for allele " + allele);
            }
        }


        // immutable
        this.alleleLabels   = ImmutableSet.copyOf(alleles);
        this.alleleSeq      = ImmutableMap.copyOf(alleleSeq);
        this.alleleFreq     = ImmutableMap.copyOf(alleleFreq);
        this.mutationLabels = ImmutableList.copyOf(mutations);
    }

    public static K69Data of(final double theta, final Set<String> alleles,
                             final Map<String, String> alleleSeq,
                             final Map<String, Integer> alleleFreq, final List<String> mutations) {
        return new K69Data(theta, alleles, alleleSeq, alleleFreq, mutations);
    }


    // <editor-fold defaultstate="collapsed" desc="Constructor Converter Helpers">

    /**
     * Method description
     *
     *
     * @param alleleLabels
     * @param alleleFreq
     *
     * @return
     */
    private static Map<String, Integer> convertAlleleFreq(final String[] alleleLabels,
            final String[] alleleFreq) {


        // null
        if (alleleFreq == null) ExceptionUtil.throwIllegalArgExNull("alleleFreq");

        if (alleleLabels.length != alleleFreq.length) {

            throw new IllegalArgumentException("Different lengths: " + " alleleLabels: "
                                               + alleleLabels + " alleleFreq: " + alleleFreq);
        }

        final Map<String, Integer> alleleFreq_temp = new HashMap<>();

        for (int i = 0; i < alleleLabels.length; i++) {
            alleleFreq_temp.put(alleleLabels[i], Integer.valueOf(alleleFreq[i]));
        }

        return alleleFreq_temp;
    }

    /**
     * Method description
     *
     *
     * @param alleleLabels
     * @param alleleSeq
     *
     * @return
     */
    private static Map<String, String> convertAlleleSeq(final String[] alleleLabels,
            final String[] alleleSeq) {


        // null
        if (alleleSeq == null) ExceptionUtil.throwIllegalArgExNull("alleleSeq");

        if (alleleLabels.length != alleleSeq.length) {

            throw new IllegalArgumentException("Different lengths: " + " alleleLabels: "
                                               + alleleLabels + " alleleSeq: " + alleleSeq);
        }

        final Map<String, String> alleleSeq_temp = new HashMap<>();

        for (int i = 0; i < alleleLabels.length; i++) {
            alleleSeq_temp.put(alleleLabels[i], alleleSeq[i]);
        }

        return alleleSeq_temp;
    }    // </editor-fold>

    public K69Data deleteMutations(String... mutations) {

        List<Integer> indices = new ArrayList<>();

        for (String mutation : mutations) {

            throwArgEx(!mutationLabels.contains(mutation), "mutation " + mutation + " not present");
            indices.add(mutationLabels.indexOf(mutation));
        }

        List<String> new_mutations = new ArrayList<>(mutationLabels);

        new_mutations.removeAll(Arrays.asList(mutations));

        final HashMap<String, String> map = new HashMap<>(alleleSeq);

        for (String a : map.keySet()) {

            final String s         = map.get(a);
            final char[] chars     = s.toCharArray();
            final char[] new_chars = new char[chars.length - indices.size()];
            int counter            = 0;

            for (int i = 0; i < chars.length; i++) {

                if (indices.contains(i)) continue;

                new_chars[counter] = chars[i];

                counter++;
            }

            final String new_seq = String.copyValueOf(new_chars);

            map.put(a, new_seq);
        }


        // deal with potential duplicate sequences created by removing sites
        Map<String, Integer> freq_map    = new HashMap<>();
        BiMap<String, String> new_seqMap = HashBiMap.create();
        Set<String> new_alleles          = new HashSet<>(alleleLabels);

        for (String a : map.keySet()) {

            final String seq = map.get(a);

            if (!new_seqMap.containsValue(seq)) {

                new_seqMap.put(a, seq);
                freq_map.put(a, alleleFreq.get(a));

            } else {

                final String a0 = new_seqMap.inverse().get(seq);    // existing allele
                int new_freq    = alleleFreq.get(a0) + alleleFreq.get(a);

                freq_map.put(a0, new_freq);
                freq_map.remove(a);
                new_alleles.remove(a);
            }
        }

        final double theta = getModel().getMutationRate();

        return of(theta, new_alleles, new_seqMap, freq_map, new_mutations);
    }

    public ImmutableMap<String, String> getAlleleSeqMap() {
        return alleleSeq;
    }

    /**
     * @param allele an allele in this data set
     * @return sequence of the specified allele
     */
    public final String getAlleleSequence(final String allele) {

        checkAllelePresent(allele);

        return alleleSeq.get(allele);
    }

    public Collection<String> allelesByFreq(final int freq) {

        return Collections2.filter(alleleLabels, new Predicate<String>() {

            @Override
            public boolean apply(String input) {
                return alleleFreq.get(input) == freq;
            }

        });
    }

    public Collection<String> allelesByMC(final int count) {

        return Collections2.filter(alleleLabels, new Predicate<String>() {

            @Override
            public boolean apply(String input) {
                return count_mutation_in_seq(alleleSeq.get(input)) == count;
            }

        });
    }

    private static int count_mutation_in_seq(String seq) {
        return StringUtils.countMatches(seq, "1");
    }

    /**
     * Method description
     *
     *
     * @param mutationCount
     *
     * @return
     */
    public static List<String> createMutations(final int mutationCount) {

        final List<String> result = new ArrayList<>();

        for (int i = 1; i <= mutationCount; i++) {
            result.add("" + i);
        }

        return result;
    }

    private static String[] createAlleleLabels(final int count) {

        final String[] result = new String[count];
        final String suffix   = "a-";

        for (int i = 0; i < count; i++) {
            result[i] = suffix + i;
        }

        return result;
    }

    /**
     * Throws {@code UnsupportedOperationException} if the returned list is modified
     *
     * @return mutation labels in the order of alleles
     */
    public List<String> getMutations() {
        return mutationLabels;
    }

    public ArrayData getArrayData() {
        return new ArrayData();
    }

    /**
     * Total number of segregating sites
     *
     * @return number of segregating sites
     */
    public int getMutationCount() {
        return alleleSeq.get(alleleSeq.keySet().iterator().next()).length();
    }

    /**
     * Provides the segregation score
     *
     *
     *
     * @param allele allele for which the score is sought
     * @param site site position , not index
     * @return the segregation score
     * @throws IllegalArgumentException if,
     * <ol>
     *     <li>allele is not present</li>
     *     <li>{@code site} is out of bounds {@code [0, getMutationCount()]}</li>
     * </ol>
     */
    public int getMutationScore(final String allele, final int site) {

        if (!alleleSeq.containsKey(allele)) {
            throw new IllegalArgumentException("Missing allele " + allele);
        }

        final String allele_seq = alleleSeq.get(allele);

        if ((site <= 0) || (site > allele_seq.length())) {
            throw new IllegalArgumentException("invalid site");
        }

        return Integer.valueOf("" + allele_seq.toCharArray()[site - 1]);
    }

    @Override
    public MutableData<K69, K69Data> getMutable() {

        return new MutableData<K69, K69Data>(getModel()) {

            @Override
            public K69Data getImmutable() {
                return new K69Data(getModel(), alleleSeq, alleleFreq, alleleLabels, mutationLabels);
            }
        };
    }

    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder();

        builder.append("s: ").append(Arrays.deepToString(getArrayData().getS())).append(
            " allele-freq: ").append(alleleFreq);

        return builder.toString();
    }

    /**
     * @return a multi-line output involving sequences and their frequencies
     */
    public String toString1() {

        String[][] outputTable = new String[alleleFreq.size() + 1][2];
        int i                  = 0;

        for (String allele : alleleFreq.keySet()) {

            final String freq     = alleleFreq.get(allele).toString();
            final String sequence = alleleSeq.get(allele);

            outputTable[i] = new String[] { freq, sequence };

            i++;
        }

        String n_k_s = getSampleSize() + "/" + getAlleles().size() + "/" + getMutationCount();

        outputTable[i] = new String[] { "N/K/S", n_k_s };

        return PrintUtil.print2D(outputTable, 2, 1, null);
    }

    /**
     * Class description
     *
     * @version        Enter version here..., 12/11/23
     * @author         Susanta Tewari
     */
    public class ArrayData {

        private final List<String> alleleLabels_list;

        public ArrayData() {
            alleleLabels_list = ImmutableList.copyOf(alleleLabels);
        }

        public int[][] getS() {

            final int[][] s = new int[alleleLabels_list.size()][];

            for (int i = 0; i < s.length; i++) {

                final String allele     = alleleSeq.get(alleleLabels_list.get(i));
                final char[] char_array = allele.toCharArray();
                final int[] temp        = new int[char_array.length];

                for (int j = 0; j < char_array.length; j++) {
                    temp[j] = Integer.parseInt("" + char_array[j]);
                }

                s[i] = temp;
            }

            return s;
        }

        public int[] getN() {

            final int[] n = new int[alleleLabels_list.size()];

            for (int i = 0; i < n.length; i++) {
                n[i] = alleleFreq.get(alleleLabels_list.get(i));
            }

            return n;
        }

        public String[] getAlleles() {
            return alleleLabels_list.toArray(new String[0]);
        }

        public String[] getMutations() {
            return mutationLabels.toArray(new String[0]);
        }
    }
}
