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

package coalescent.statistic;

import coalescent.EventType;
import coalescent.core.util.IterableUtil;
import coalescent.core.util.PairPredicate;
import coalescent.data.K69Data;
import coalescent.model.K69;
import coalescent.phylogeny.GeneTree;
import coalescent.phylogeny.GeneTree.Edge;
import coalescent.phylogeny.GeneTree.Node;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static coalescent.phylogeny.GeneTree.mutations;

/**
 * Sample configuration with unlabeled sequences and unordered mutations for data from
 * infinite-sites model.<p> This class is immutable.
 *
 * @author Susanta Tewari
 * @version 1.0 Dec 15, 2010
 */
public class K69_AC implements AC<K69_AC, K69> {

    /**
     * tracker instance for this gene tree, tracking information on the producer.
     */
    private Tracker tracker = null;

    /**
     * Keeps a reference of the source leaf nodes of the {@code GeneTree} object that produced this
     * object using method {@code applyEvent(EventType); it is {@code null} otherwise. The key: the
     * source leaf node of the producer. the value: leaf node of this object
     */
    private Map<Node, Node> srcCopy = null;

    /** Field description */
    private int hashCode = 0;

    /**
     * Provides the number of segregating sites
     *
     * @return the number of segregating sites
     */
    private int sn   = 0;
    private int mrca = -1;
    private int n    = -1;    // cache for getN()

    /**
     * this tree has only the distinct alleles
     */
    private final Node tree;

    /**
     * Manages allele frequency for this gene tree. Alleles are represented by the leaf nodes. Map
     * key is leaf node and its value is the frequency.
     */
    private final Map<Node, Integer> freq;

    /**
     * Used for implementing equals and hash code. Its key is the string representation of the
     * allele count path with the allele frequency embedded and the value is the frequency of such
     * string patterns.
     */
    private Map<String, Integer> freqStrMap;

    /** Field description */
    private K69 model;

    /**
     * Distinct:
     * <ol>
     *     <li>same parent</li>
     *     <li>same {@link Node#getUpperMCPath()}</li>
     *     <li>same frequency</li>
     * </ol>
     * @return distinct alleles with mutations.
     */
    private Map<Node, Integer> da;
    private GeneTree geneTree;

    /**
     * Creates an instance of the sample configuration from the specified data.
     *
     * @param data infinite-sites data
     */
    public K69_AC(final K69Data data) {

        geneTree   = GeneTree.getInstance(data);
        this.tree  = geneTree.getRoot();
        this.freq  = geneTree.getFreq();
        this.model = data.getModel();
    }

    /**
     * Creates a new object when an event of the specified type occurs on the given allele. The
     * {@code mergeAllele} of the clone tracker is not configured here, it should be configured (if
     * applicable) by the caller.
     *
     * @param cloneSrc source configuration of this gene tree
     * @param eventType the type of event
     * @param allele the allele in {@code cloneSrc} on which the event takes place
     */
    public K69_AC(final K69_AC cloneSrc, final EventType eventType, final Node allele) {

        this(cloneSrc);

        addTrackerInfo(eventType, allele);    // configure tracker
    }

    private K69_AC(final K69_AC cloneSrc) {

        srcCopy  = new HashMap<>();
        geneTree = new GeneTree(cloneSrc.geneTree, srcCopy);
        tree     = geneTree.getRoot();
        freq     = geneTree.getFreq();
        model    = cloneSrc.model;
    }

    /**
     * @param tree root node
     * @param freq leaf nodes as keys and their frequencies as values
     */
    private K69_AC(final K69 model, final GeneTree geneTree) {

        this.geneTree = geneTree;
        this.tree     = geneTree.getRoot();
        this.freq     = geneTree.getFreq();
        this.model    = model;
    }

    /**
     *
     * @param toString {@code K69_AC.toString()}
     * @return new instance {@code d | d.toString() == toString }
     */
    public static K69Data of_K69_AC_toString(double theta, String toString) {

        String content = toString.substring(1, toString.length() - 1);    // get allele-freq
        final Map<String, Integer> alleleFreq =
            Maps.transformValues(Splitter.on(", ").withKeyValueSeparator("=").split(content),
                                 new Function<String, Integer>() {

            @Override
            public Integer apply(String s) {
                return Integer.parseInt(s);
            }

        });


        // prepare mutations
        List<String> mutations = new ArrayList<>();

        for (String allele : alleleFreq.keySet()) {

            for (String mutation : Splitter.on(Node.MC_PATH_DELIM).split(allele)) {

                if (mutation.equals("0") || mutations.contains(mutation)) continue;

                mutations.add(mutation);
            }
        }

        Collections.sort(mutations);


        // prepare sequence
        Map<String, String> alleleSeq = new HashMap<>();

        for (String allele : alleleFreq.keySet()) {

            StringBuilder sb = new StringBuilder();

            for (String m : mutations) {

                if (Arrays.asList(allele.split(Node.MC_PATH_DELIM)).contains(m)) sb.append('1');
                else sb.append('0');
            }

            alleleSeq.put(allele, sb.toString());
        }

        return new K69Data(theta, alleleFreq.keySet(), alleleSeq, alleleFreq, mutations);
    }

    /**
     * @return a new config as this one except all alleles have frequency 1.
     */
    public K69_AC of_Singleton() {

        K69_AC clone = new K69_AC(this);

        for (Node leaf : clone.freq.keySet()) {
            clone.freq.put(leaf, 1);
        }

        return clone;
    }

    public K69 getModel() {
        return model;
    }

    @Override
    public void setModel(final K69 model) {
        this.model = model;
    }

    /**
     * Tree is represented by a node. It returns the root node.
     *
     * @return the root node of this tree
     */
    public Node getTree() {
        return tree;
    }

    /**
     * Alleles are represented by the leaf nodes. Map key is leaf node and its value is the
     * frequency.
     *
     * @return allele frequency for this gene tree
     */
    public Map<Node, Integer> getFreq() {
        return freq;
    }

    /**
     * @return the tracker of this gene tree
     */
    public final Tracker getTracker() {
        return tracker;
    }

    /**
     *
     *
     * @return
     */
    public Map<Node, Node> getSrcCopy() {
        return srcCopy;
    }

    /**
     * @return a new instance of MRCA
     */
    public static K69_AC MRCA(final K69 model) {
        return MRCA(model, 1);
    }

    /**
     * @param mrcaFrequency MRCA frequency
     * @return a new instance of MRCA with the specified frequency
     */
    public static K69_AC MRCA(final K69 model, final int mrcaFrequency) {
        return new K69_AC(model, GeneTree.of_Singleton(mrcaFrequency));
    }

    /**
     *
     *
     * @param eventType event type that created this gene tree
     * @param allele allele on {@code cloneSrc} on which {@code eventType} took place to create this
     * gene tree
     */
    private void addTrackerInfo(final EventType eventType, final Node allele) {


        // config the tracker
        tracker           = new Tracker();
        tracker.eventType = eventType;
        tracker.allele    = allele;
    }

    /**
     * Creates a new instance of {@code GeneTree} using the root node and the leaf
     * frequencies.
     *
     * @return a new instance of {@code GeneTree}
     */
    public final GeneTree getGeneTree() {
        return geneTree;
    }

    /**
     * @param depth iteration depth
     * @return nodes at the specified depth
     */
    public Collection<Node> itr_atDepth(int depth) {
        return geneTree.itr_atDepth(depth);
    }

    @Override
    public Iterable<Node> alleles(final EventType type) {

        Objects.requireNonNull(type);

        final Set<Node> result = new HashSet<>(getKn());

        switch (type) {

            case COALESCENT :
                for (final Node leaf : freq.keySet()) {

                    if (canCoalesce(leaf)) {
                        result.add(leaf);
                    }
                }

                return result;

            case MUTATION :
                for (final Node leaf : freq.keySet()) {

                    if (canMutate(leaf)) {
                        result.add(leaf);
                    }
                }

                return result;

            default :
                throw new IllegalArgumentException("Unsupported event type: " + type);
        }
    }

    /**
     * @param leaf leaf node
     * @return {@code true} if the allele {@code leaf} can take a coalescent event; {@code false}
     *         otherwise
     */
    private boolean canCoalesce(final Node leaf) {

        final int frequency = freq.get(leaf);

        return frequency > 1;
    }

    /**
     * @param leaf leaf node
     * @return {@code true} if the allele {@code leaf} can take a mutation event; {@code false}
     *         otherwise
     */
    private boolean canMutate(final Node leaf) {

        final int frequency = freq.get(leaf);

        return (frequency == 1) && (leaf.getParent().getChildEdge(leaf).mutationCount() > 0);
    }

    /**
     *
     * @param allele
     * @param type
     *
     * @return
     * type} is not supported by this statistic 3.) {@code allele} and {@code type} are not mutually
     * compatible i.e., the specified allele was not produced via method {@code alleles())
     */
    @Override
    public K69_AC apply(final Object allele, final EventType type) {

        Objects.requireNonNull(allele);
        Objects.requireNonNull(type);

        if (!(allele instanceof Node)) throw new ClassCastException();

        final Node actual_allele = (Node) allele;

        switch (type) {

            case COALESCENT :
                return applyCoalescent(actual_allele);

            case MUTATION :
                return applyMutation(actual_allele);

            default :
                throw new IllegalArgumentException("Unsupported event type: " + type);
        }
    }

    public K69_AC applyFwd(final Object allele, final EventType type) {

        Objects.requireNonNull(allele);
        Objects.requireNonNull(type);

        if (!(allele instanceof Node)) throw new ClassCastException();

        final Node actual_allele = (Node) allele;

        switch (type) {

            case COALESCENT :
                return applyCoalescentFwd(actual_allele);

            case MUTATION :
                return applyMutationFwd(actual_allele);

            default :
                throw new IllegalArgumentException("Unsupported event type: " + type);
        }
    }

    public Collection<Node> allelesByFreq(final int freq) {
        return geneTree.allelesByFreq(freq);
    }

    public Collection<Node> allelesByPattern(final String m_upper_path) {
        return geneTree.allelesByPattern(m_upper_path);
    }

    /**
     * @param leaf leaf node
     * @return a new instance of ancestral configuration by applying coalecsent event on the
     *         specified allele {@code leaf}
     */
    private K69_AC applyCoalescent(final Node leaf) {

        final int frequency = freq.get(leaf);
        final K69_AC clone  = new K69_AC(K69_AC.this, EventType.COALESCENT, leaf);

        clone.freq.put(clone.srcCopy.get(leaf), frequency - 1);

        return clone;
    }

    private K69_AC applyCoalescentFwd(final Node leaf) {

        final int frequency = freq.get(leaf);
        final K69_AC clone  = new K69_AC(K69_AC.this, EventType.COALESCENT, leaf);

        clone.freq.put(clone.srcCopy.get(leaf), frequency + 1);

        return clone;
    }

    /**
     * @param allele leaf node
     * @return a new instance of ancestral configuration by applying mutation event on the specified
     *         allele {@code leaf}
     */
    private K69_AC applyMutation(final Node allele) {

        final K69_AC clone = new K69_AC(K69_AC.this, EventType.MUTATION, allele);

        clone.mutate();

        return clone;
    }

    private K69_AC applyMutationFwd(final Node leaf) {

        final K69_AC clone     = new K69_AC(K69_AC.this, EventType.MUTATION, leaf);
        int this_leaf_freq     = getFreq().get(leaf);
        Node clone_this_leaf   = clone.getSrcCopy().get(leaf);
        Edge clone_this_edge   = clone_this_leaf.getParentEdge();
        int clone_this_m_count = clone.getTree().getLowerMC();    // mutation #

        /**
         * Wild: frequency is reduced by 1 and a child w/ mutation is added
         *
         * Non-Wild: wild child node is created with frequency reduced by 1 and a
         * child w/ mutation is added (after removing the old one)
         */
        if (this_leaf_freq == 1) clone_this_edge.addMutation(clone_this_m_count + 1 + "");
        else if (leaf.isWild()) {

            clone.getFreq().put(clone_this_leaf, this_leaf_freq - 1);

            Node node = clone.getTree().addChild("", clone_this_m_count + 1 + "");

            clone.getFreq().put(node, 1);

        } else {

            Node wild_child_node = clone_this_leaf.addChild("", Edge.NO_MUTATION_CHARACTER);

            clone.getFreq().put(wild_child_node, this_leaf_freq - 1);

            Node nonwild_child_node = clone_this_leaf.addChild("", clone_this_m_count + 1 + "");

            clone.getFreq().remove(clone_this_leaf);
            clone.getFreq().put(nonwild_child_node, 1);
        }

        return clone;
    }

    /**
     * This modifies this object and should be invoked on a clone. A clone can be obtained from the
     * copy constructor. It also modifies the tracker.mergeAllele is applicable.
     */
    public void mutate() {

        final Node src_leaf  = tracker.allele;
        final Node this_leaf = srcCopy.get(src_leaf);
        final Edge this_edge = this_leaf.getParent().getChildEdge(this_leaf);

        if ((this_edge.mutationCount() > 1) || (src_leaf.getParent().getWildEdge() == null)) {
            this_edge.removeMutation();    // m1 mutation
        } else {


            // m2 mutation
            final Edge srcNoMutationEdge = src_leaf.getParent().getWildEdge();
            final Node srcMergeLeaf      = srcNoMutationEdge.getChild();
            final Node this_mergeLeaf    = srcCopy.get(srcMergeLeaf);

            tracker.mergeAllele = srcMergeLeaf;

            final List<Node> this_siblings = this_leaf.getParent().getChildren();

            assert this_siblings.size() >= 2 : "sibling size is: " + this_siblings.size();

            final int newFreq = freq.get(this_mergeLeaf) + 1;

            if (this_siblings.size() > 2) {

                this_leaf.getParent().removeChild(this_leaf);
                freq.remove(this_leaf);
                freq.put(this_mergeLeaf, newFreq);

            } else {


                // i.e. == 2
                freq.remove(this_leaf);
                this_leaf.getParent().removeChild(this_leaf);

                if (this_mergeLeaf.getParent().isRoot()) {
                    freq.put(this_mergeLeaf, newFreq);
                } else {

                    freq.remove(this_mergeLeaf);
                    this_mergeLeaf.getParent().removeChild(this_mergeLeaf);
                    freq.put(this_leaf.getParent(), newFreq);
                }
            }
        }
    }

    public BigDecimal transitionProb(final EventType type, final Node allele) {

        switch (type) {

            case COALESCENT :    // optz. over coalescentProb().multiply(forwardCoalescentProb(ac))
                final double n     = getN();
                final double theta = model.getMutationRate();

                return new BigDecimal((freq.get(allele) - 1) / (n - 1 + theta));

            case MUTATION :
                return mutationProb().multiply(fwdMutationProb(getMergeAllele(allele)));

            default :
                throw new IllegalArgumentException("Unsupported event type: " + type);
        }
    }

    /**
     * Finds the merge allele if mutation-2 applies on the specified allele.
     * @param allele allele on which mutation-2 is applied
     * @return the merge allele or {@code null} if not possible
     */
    private Node getMergeAllele(Node allele) {

        final Edge edge     = allele.getParent().getChildEdge(allele);
        final Edge wildEdge = allele.getParent().getWildEdge();

        return ((edge.mutationCount() > 1) || (wildEdge == null)) ? null : wildEdge.getChild();
    }

    @Override
    public BigDecimal transitionProb(final EventType type, final K69_AC ac) {

        switch (type) {

            case COALESCENT :    // optz. over coalescentProb().multiply(forwardCoalescentProb(ac))
                final double n     = getN();
                final double theta = model.getMutationRate();
                final Node aa      = findThisAllele(ac.tracker.allele);

                return new BigDecimal((freq.get(aa) - 1) / (n - 1 + theta));

            case MUTATION :
                return mutationProb().multiply(forwardMutationProb(ac));

            default :
                throw new IllegalArgumentException("Unsupported event type: " + type);
        }
    }

    /**
     * The probability of observing a coalescent event next among the competing transitionTypes in the model
     * in a sample of given size. It is given by the following formula.
     * (n - 1) / ( n - 1 + theta ) (n = sample size)
     *
     * @return the probability of observing a coalescent event next
     */
    public BigDecimal coalescentProb() {

        final int n        = getN();
        final double theta = model.getMutationRate();

        return new BigDecimal(((n - 1) / (n - 1 + theta)));
    }

    /**
     * The probability of observing a mutation event next among the competing transitionTypes in the model in
     * a sample of given size. It is given by the following formula.
     * theta / ( n - 1 + theta ) (n = sample size)
     * Note that, this method cannot be extracted to <code>MutationModel</code> as the probability
     * depends on all the competing forces.
     *
     * @return the probability of observing a mutation event next
     */
    public BigDecimal mutationProb() {

        final int n        = getN();
        final double theta = model.getMutationRate();

        return new BigDecimal((theta / (n - 1 + theta)));
    }

    public void forwardEventProbArgCheck(final EventType type, final K69_AC ancestralConfig) {

        if (type == null) {
            throw new NullPointerException("type==null");
        }

        if (ancestralConfig == null) {
            throw new NullPointerException("ancestralConfig==null");
        }

        if (type != ancestralConfig.tracker.eventType) {
            throw new IllegalArgumentException("invalid ancestral config");
        }
    }

    protected BigDecimal forwardCoalescentProb(final K69_AC ac) {

        final Node aa    = findThisAllele(ac.tracker.allele);
        final double val = (double) (freq.get(aa) - 1) / (getN() - 1);

        return new BigDecimal(val);
    }

    /**
     * @param ac ancestral config
     * @return forward mutation probability
     */
    protected BigDecimal forwardMutationProb(final K69_AC ac) {
        return fwdMutationProb(findThisAllele(ac.tracker.mergeAllele));
    }

    private BigDecimal fwdMutationProb(Node merge_allele) {

        return (merge_allele == null)
               ? new BigDecimal(1.0 / getN())
               : new BigDecimal((freq.get(merge_allele) + 1.0) / getN());
    }

    /**
     * @param anotherAllele an allele from this or another object that is potentially equal to an
     * allele in this object in terms of {@code getUpperMutationPath}
     * @return the matching allele on this object
     */
    public Node findThisAllele(final Node anotherAllele) {

        if (anotherAllele == null) return null;

        if (freq.containsKey(anotherAllele)) {
            return anotherAllele;
        } else {

            for (final Node thisAllele : freq.keySet()) {

                if (thisAllele.getUpperMutationPath().equals(
                        anotherAllele.getUpperMutationPath())) {
                    return thisAllele;
                }
            }
        }

        throw new IllegalArgumentException("matching allele not found");
    }

    @Override
    public Integer getN() {

        if (n == -1) {

            n = 0;

            for (final Integer val : freq.values()) {
                n += val;
            }
        }

        return n;
    }

    /**
     * @return mutation count
     */
    public Integer getSn() {

        if (sn == 0) sn = tree.getLowerMC();

        return sn;
    }

    /**
     * Provides the number of (distinct) alleles.
     *
     * @return the number of (distinct) alleles
     */
    public Integer getKn() {
        return freq.size();
    }

    /**
     * @return key: mutation; value: number of alleles carrying that mutation
     */
    public Map<String, Integer> getDm() {

        Map<String, Integer> result = new HashMap<>();

        for (Node a : freq.keySet()) {

            final String path = a.getUpperMutationPath();

            for (String m : mutations(path)) {

                int old_count = 0;

                if (result.containsKey(m)) old_count = result.get(m);

                result.put(m, old_count + freq.get(a));
            }
        }

        return result;
    }

    /**
     * @deprecated do not use
     * Key: distinct allele; Value: multiplicity
     * @return distinct alleles map
     */
    Map<Node, Integer> getDa() {

        if (da != null) return da;

        da = new HashMap<>();

        final Set<Node> black_set = new HashSet<>();
        final HashSet<Node> set2  = new HashSet<>(freq.keySet());

        for (Node a : freq.keySet()) {

            if (black_set.contains(a)) continue;

            int count = 1;

            for (Iterator<Node> itr = set2.iterator(); itr.hasNext(); ) {

                Node b = itr.next();

                if (a == b) {

                    itr.remove();

                    continue;
                }

                final boolean c1 = a.getParent() == b.getParent();
                final boolean c2 = c1 && (freq.get(a) == freq.get(b));
                final boolean c3 = c2 && a.getUpperMCPath().equals(b.getUpperMCPath());

                if (c3) {

                    itr.remove();
                    black_set.add(b);

                    count++;
                }
            }

            da.put(a, count);
        }

        return da;
    }

    @Override
    public Integer eventsToMRCA() {

        if (mrca == -1) mrca = getSn() + getN() - 1;

        return mrca;
    }

    @Override
    public boolean isEventsToMRCABounded() {
        return true;
    }

    @Override
    public Boolean isMRCA() {
        return getN() == 1;
    }

    @Override
    public Double probAtMRCA() {

        if (!isMRCA()) {
            throw new IllegalStateException("AC is not an MRCA");
        }

        return 1.0;
    }

    /**
     * Method description
     *
     *
     * @param obj
     *
     * @return
     */
    public boolean equalsMy(final Object obj) {

        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj.getClass() != getClass()) {
            return false;
        }

        final K69_AC rhs = (K69_AC) obj;

        return getFreqStrMap().equals(rhs.getFreqStrMap());
    }

    /**
     * Method description
     *
     *
     * @param obj
     *
     * @return
     */
    @Override
    public boolean equals(final Object obj) {

        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() != getClass()) return false;

        final K69_AC rhs = (K69_AC) obj;

        if (!model.equals(rhs.model)) return false;


//      return checkTrees(tree, freq, rhs.tree, rhs.freq);
        return (getSn() == rhs.getSn()) && getFreqStrMap().equals(rhs.getFreqStrMap());
    }

    static boolean checkTrees(final K69_AC one, final K69_AC two) {
        return checkTrees(one.tree, one.freq, two.tree, two.freq);
    }

    /**
     * Method description
     *
     *
     * @param tree1
     * @param freq1
     * @param tree2
     * @param freq2
     *
     * @return
     */
    private static boolean checkTrees(final Node tree1, final Map<Node, Integer> freq1,
                                      final Node tree2, final Map<Node, Integer> freq2) {

        final Collection<Edge> this_edges = new ArrayList<>(tree1.getEdges());
        final Collection<Edge> that_edges = new ArrayList<>(tree2.getEdges());
        final Iterator<Edge> this_itr     = this_edges.iterator();

        while (this_itr.hasNext()) {

            final Edge this_edge          = this_itr.next();
            final Node this_child         = this_edge.getChild();
            boolean found                 = false;
            final Iterator<Edge> that_itr = that_edges.iterator();

            while (that_itr.hasNext()) {

                final Edge that_edge  = that_itr.next();
                final Node that_child = that_edge.getChild();

                if (this_edge.mutationCount() != that_edge.mutationCount()) {
                    continue;
                }


                // both a leaf or neither is
                if (this_child.isLeaf() == !that_child.isLeaf()) {
                    continue;
                }


                // if leaf, their frequencies must match
                if (this_child.isLeaf()) {

                    if (freq1.get(this_child) == freq2.get(that_child)) {

                        found = true;

                        that_itr.remove();

                        break;
                    }

                } else {


                    // both are internal nodes
                    if (checkTrees(this_child, freq1, that_child, freq2)) {

                        found = true;

                        that_itr.remove();

                        break;
                    }
                }
            }

            if (!found) {
                return false;
            } else {
                this_itr.remove();
            }
        }

        return this_edges.isEmpty() && that_edges.isEmpty();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    private Map<String, Integer> getFreqStrMap() {

        if (freqStrMap == null) {
            freqStrMap = createFreqStrMap();
        }

        return freqStrMap;
    }

    /**
     * Two alleles a & b have the same pattern iff, LMC(a) = LMC(b) && F(a) = F(b)
     *
     * @return distinct allele patterns
     * TODO Use PairPredicate from commons.util in commons-1.2.1
     * @deprecated  do not use; will be removed;  was created for a special usecase. the code has
     * been relocated.
     */
    public Iterable<Node> distinctAllelePatterns() {

        final PairPredicate<Node> pairPredicate = new PairPredicate<Node>() {

            @Override
            public boolean apply(final Node a, final Node b) {

                final boolean condition_1 = a.getParent() == b.getParent();
                final boolean condition_3 = a.getLowerMCs().equals(b.getLowerMCs());
                final boolean condition_2 = freq.get(a) == freq.get(b);

                return condition_1 && condition_2 && condition_3;
            }
        };

        return IterableUtil.filter(freq.keySet(), pairPredicate);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    private Map<String, Integer> createFreqStrMap() {

        final Map<String, Integer> result = new HashMap<>(freq.size());

        for (final Node leaf : freq.keySet()) {

            final String pattern = leaf.getUpperMutationPath() + "-" + freq.get(leaf);

            if (result.containsKey(pattern)) {

                final int oldVal = result.get(pattern);

                result.remove(pattern);
                result.put(pattern, oldVal + 1);

            } else {
                result.put(pattern, 1);
            }
        }

        return result;
    }

    /**
     * Method description
     *
     *
     * @param val
     *
     * @return
     */
    private String encrypt(final String val) {

        final MessageDigest messageDigest;

        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }

        messageDigest.update(val.getBytes());

        return new String(messageDigest.digest());
    }

    @Override
    public final int hashCode() {

        if (hashCode == 0) {

            hashCode = createFreqStrMap().hashCode();

            if (hashCode == 0) {
                throw new RuntimeException("hashcode 0");
            }
        }

        return hashCode;
    }

    @Override
    public String toString() {
        return freq.toString();
    }

    /**
     * @return more information than {@link #toString1()}.
     */
    public String toString1() {

        StringBuilder sb = new StringBuilder();

        sb.append("Data: " + toString());
        sb.append("\nDistinct allele count: " + getKn());
        sb.append("\nMutation count: " + getSn());
        sb.append("\nSample size: " + getN());

        return sb.toString();
    }

    /**
     * Information on the producer (object that created this one) configuration helpful in
     * computation; {@code null} for the observed sample configuration.
     */
    @SuppressWarnings({ "PackageVisibleField", "PublicField" })
    public class Tracker {

        /**
         * producer object that created this gene tree
         */
        K69_AC producer;

        /**
         * the event type used in the production
         */
        EventType eventType;

        /**
         * the allele involved in the production
         */
        Node allele;

        /**
         * An allele in the producer that matches with the allele produced when event type {@code
         * eventType} acted on the field {@code allele}.
         */
        public Node mergeAllele;
    }
}
