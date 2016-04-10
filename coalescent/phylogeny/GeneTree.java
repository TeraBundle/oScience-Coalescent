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
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import commons.util.MathUtil;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Susanta Tewari
 * @since Jul 25, 2011
 */
public class GeneTree {

    /** this tree has only the distinct alleles */
    private final Node tree;

    /** Manages frequency for the leaf nodes (alleles) key: leaf node value: frequency */
    private final Map<Node, Integer> freq;

    /**
     *
     * @param src
     * @param srcCopy
     */
    public GeneTree(GeneTree src, @Nullable Map<Node, Node> srcCopy) {

        tree = new Node(null, "r");
        freq = new HashMap<>();

        if (src.tree.isLeaf()) freq.put(tree, src.freq.get(src.tree));
        else Node.copy(tree, freq, srcCopy, src.tree, src.freq);
    }

    /**
     * @param tree root of the tree
     * @param freq leaf frequency map
     */
    private GeneTree(final Node tree, final Map<Node, Integer> freq) {

        this.tree = tree;
        this.freq = freq;
    }

    /**
     * @param data data for gene tree
     * @return a new instance of {@code GeneTree}
     */
    public static GeneTree getInstance(final K69Data data) {

        final Pair<Node, Map<Node, Integer>> tree_data = new GusfieldAlgo(data).buildGeneTree();

        return new GeneTree(tree_data.getValue0(), tree_data.getValue1());
    }

    public static GeneTree of_Singleton(final int frequency) {

        final Node tree                  = new Node(null, "r");
        final Node child                 = tree.addChild("", Edge.NO_MUTATION_CHARACTER);
        final Map<Node, Integer> freqMap = new HashMap<>(1);

        freqMap.put(child, frequency);

        return new GeneTree(tree, freqMap);
    }

    /**
     * @return root node
     */
    public Node getRoot() {
        return tree;
    }

    /**
     * @param depth iteration depth
     * @return nodes at the specified depth
     */
    public Collection<Node> itr_atDepth(final int depth) {


        // Node.getChildren returns reference (performance); thus needs wrapping
        final Collection<Node> children      = new ArrayList<>(tree.getChildren());
        final Collection<Node> children_next = new ArrayList<>(10);

        for (int i = 0; i < depth - 1; i++) {

            for (final Node child : children) {


                // Node.getChildren returns reference (performance); thus needs wrapping
                final List<Node> child_children = new ArrayList<>(child.getChildren());

                children_next.addAll(child_children);
            }

            children.clear();
            children.addAll(children_next);
            children_next.clear();
        }

        return children;
    }

    /**
     *
     * @param freq allele frequency
     * @return alleles matching the specified frequency
     */
    public Collection<Node> allelesByFreq(final int freq) {

        final ImmutableList<Node> alleles = ImmutableList.copyOf(getFreq().keySet());

        return Collections2.filter(alleles, new Predicate<Node>() {

            @Override
            public boolean apply(Node o) {
                return getFreq().get(o) == freq;
            }

        });
    }

    /**
     * Path specification is as described in {@link Node#getUpperMCPath()}.
     * @param m_upper_path path pattern
     * @return alleles matching the specified pattern
     */
    public Collection<Node> allelesByPattern(final String m_upper_path) {

        final ImmutableList<Node> alleles = ImmutableList.copyOf(getFreq().keySet());

        return Collections2.filter(alleles, new Predicate<Node>() {

            @Override
            public boolean apply(Node o) {
                return o.getUpperMCPath().equals(m_upper_path);
            }

        });
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Map<Node, Integer> getFreq() {
        return freq;
    }

    /**
     * @param allele an allele of this gene tree
     * @return frequency of the specified allele
     */
    public Integer getFreq(final Node allele) {

        if (!freq.containsKey(allele)) {
            throw new IllegalArgumentException("allele is a not a leaf node");
        }

        return freq.get(allele);
    }

    /**
     * {@code label} is delimited using character {@link coalescent.phylogeny.GeneTree.Node#MC_PATH_DELIM}.
     *
     * @param label label containing mutations
     * @return list of mutations contained in the label (from left to right)
     */
    public static List<String> mutations(String label) {

        final List<String> list = new ArrayList<>(Arrays.asList(label.split(Node.MC_PATH_DELIM)));

        list.remove(Edge.NO_MUTATION_CHARACTER);

        return list;
    }

    /**
     */
    public static class Edge {

        public static final String NO_MUTATION_CHARACTER = "0";
        private final List<String> mutations             = new ArrayList<>();
        private final Node parent;
        private final Node child;
        private String label;

        /**
         * @param parent parent node
         * @param child child node
         * @param label edge label
         */
        public Edge(final Node parent, final Node child, final String label) {

            Objects.requireNonNull(child);
            Objects.requireNonNull(label);

            this.parent = parent;
            this.child  = child;
            this.label  = label;

            if (!label.equals(NO_MUTATION_CHARACTER)) mutations.addAll(mutations(label));
        }

        /**
         * Number of mutations on this edge
         *
         * @return
         */
        public int mutationCount() {
            return mutations.size();
        }

        /**
         * @return mutations on the edge label
         */
        public List<String> getMutations() {
            return mutations;
        }

        /**
         * Method description
         *
         *
         * @param mutationLabel
         */
        public void addMutation(final String mutationLabel) {

            mutations.add(mutationLabel);

            this.label = createLabel();
        }

        /**
         * Note that this can be invoked with single and multiple mutations.
         */
        public void removeMutation() {

            mutations.remove(mutations.size() - 1);

            if (mutations.isEmpty()) {
                this.label = NO_MUTATION_CHARACTER;
            } else {
                this.label = createLabel();
            }
        }

        private String createLabel() {

            final StringBuilder builder = new StringBuilder();

            for (int i = 0; i < mutations.size() - 1; i++) {
                builder.append(mutations.get(i) + Node.MC_PATH_DELIM);
            }

            if (!mutations.isEmpty()) {
                builder.append(mutations.get(mutations.size() - 1));
            }

            return builder.toString();
        }

        public Node getChild() {
            return child;
        }

        public String getLabel() {
            return label;
        }

        public Node getParent() {
            return parent;
        }

        @Override
        public String toString() {
            return mutations.isEmpty() ? "" : label;
        }
    }

    /**
     * Leaf nodes represent sample alleles and non-leaf nodes represent ancestral alleles to the
     * sample. An invariant of this class is, the number of edges should match the number of
     * children.
     */
    public static class Node {

        public static final String MC_PATH_DELIM = ",";
        protected final List<Node> children      = new ArrayList<>();
        protected final List<Edge> edges         = new ArrayList<>();
        protected final Node parent;
        private String label;

        /**
         * Constructs ...
         *
         *
         * @param parent
         * @param label
         */
        public Node(final Node parent, final String label) {

            this.parent = parent;
            this.label  = label;
        }

        /**
         * @param destNode destination node
         * @param destFreq leaf nodes under {@code destNode} as keys with their frequencies as values
         * @param destSrcCopy to be filled with the leaf nodes of {@code srcNode} as keys with the leaf
         * nodes of {@code destNode} as values; skipped if {@code null}
         * @param srcNode source node
         * @param srcFreq leaf nodes under {@code srcNode} as keys with their frequencies as values
         */
        public static void copy(final Node destNode, final Map<Node, Integer> destFreq,
                                final Map<Node, Node> destSrcCopy, final Node srcNode,
                                final Map<Node, Integer> srcFreq) {

            for (final Node srcChildNode : srcNode.getChildren()) {

                final Node destChildNode = destNode.addChild(srcChildNode.getLabel(),
                                               srcNode.getChildEdge(srcChildNode).getLabel());

                if (!srcChildNode.isLeaf()) {
                    copy(destChildNode, destFreq, destSrcCopy, srcChildNode, srcFreq);
                } else {

                    destFreq.put(destChildNode, srcFreq.get(srcChildNode));

                    if (destSrcCopy != null) destSrcCopy.put(srcChildNode, destChildNode);
                }
            }
        }

        /**
         * alleles (leafs) of the node
         *
         * @return alleles (leaf nodes) of this gene tree
         */
        public List<Node> leafs() {

            final List<Node> result = new ArrayList<>();

            if (isLeaf()) {

                result.add(this);

                return result;
            }

            for (final Node child : children) {

                if (child.isLeaf()) {
                    result.add(child);
                } else {
                    result.addAll(child.leafs());
                }
            }

            return result;
        }

        /**
         *
         * @return
         */
        public Edge getParentEdge() {

            if (isRoot()) {
                throw new IllegalArgumentException("It is a root node.");
            }

            return getParent().getChildEdge(this);
        }

        /**
         *
         * @param child
         *
         * @return
         */
        public Edge getChildEdge(final Node child) {

            for (final Edge edge : edges) {

                if ((edge.getChild() == child) && (edge.getParent() == this)) {
                    return edge;
                }
            }

            if (!children.contains(child)) {
                throw new IllegalArgumentException("Invalid child: ");
            }

            throw new IllegalStateException("Valid child does not have a corresponding edge");
        }

        Node findNode(final String nodeLabel) {

            if (label.equals(nodeLabel)) {
                return this;
            }

            for (final Node node : children) {

                final Node result = node.findNode(nodeLabel);

                if (result != null) {
                    return result;
                }
            }

            return null;
        }

        Edge findEdge(final String edgeLabel) {

            for (final Edge edge : edges) {

                if (edge.getLabel().equals(edgeLabel)) {
                    return edge;
                }
            }

            for (final Node node : children) {

                final Edge result = node.findEdge(edgeLabel);

                if (result != null) {
                    return result;
                }
            }

            return null;
        }

        /**
         * Every node must have an edge with no mutation
         *
         * @return edge with zero mutation or {@code null} if none is found.
         * @throws IllegalArgumentException if {@code isLeaf() == true}
         */
        public Edge getWildEdge() {

            if (isLeaf()) {
                throw new IllegalArgumentException("this node is a leaf node");
            }

            for (final Node child : getChildren()) {

                final Edge edge = getChildEdge(child);

                if (edge.mutationCount() == 0) {
                    return edge;
                }
            }

            return null;
        }

        /**
         * @return {@code true} if this contains a wild edge; {@code false} otherwise
         * @throws IllegalArgumentException if {@code isLeaf() == true}
         */
        public boolean hasWildEdge() {
            return getWildEdge() != null;
        }

        public Set<Edge> getNonWildEdges() {

            final Set<Edge> result = new HashSet<>();

            result.addAll(edges);
            result.remove(getWildEdge());

            return result;
        }

        public int getUpperMC() {
            return MathUtil.sumDoublePrecision(getUpperMCs()).intValue();
        }

        public List<Integer> getUpperMCs() {

            final List<Integer> result = new ArrayList<>();
            final String[] counts      = getUpperMCPath().split(MC_PATH_DELIM);

            for (final String count : counts) {
                result.add(Integer.parseInt(count));
            }

            return result;
        }

        public List<Integer> getLowerMCs() {

            final List<Integer> result = new ArrayList<>();
            final String[] counts      = getLowerMCPath().split(MC_PATH_DELIM);

            for (final String count : counts) {
                result.add(Integer.parseInt(count));
            }

            return result;
        }

        /**
         * @return lower mutation count path (from root)
         */
        public String getLowerMCPath() {
            return new StringBuilder(getUpperMCPath()).reverse().toString();
        }

        /**
         * @return list of nodes on the path to the root (excluding itself)
         */
        public List<Node> getAncestralNodes() {

            final List<Node> result = new ArrayList<>();
            Node child              = this;
            Node the_parent         = child.getParent();

            while (!child.isRoot()) {

                result.add(the_parent);

                child      = the_parent;
                the_parent = child.getParent();
            }

            return result;
        }

        /**
         * @return upper mutation path string
         */
        public String getUpperMCPath() {
            return createUpperMCPath();
        }

        /**
         * @return upper mutation path with mutation labels (o indicates no mutation)
         */
        public String getUpperMutationPath() {

            final StringBuilder stringBuilder = new StringBuilder();
            Node child                        = this;
            Node the_parent                   = child.getParent();

            while (!child.isRoot()) {

                stringBuilder.append(the_parent.getChildEdge(child).getLabel() + MC_PATH_DELIM);

                child      = the_parent;
                the_parent = child.getParent();
            }

            if (stringBuilder.length() > 0) {
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            }

            return stringBuilder.toString();
        }

        public List<String> getUpperMutations() {

            final List<String> list = new ArrayList<>();

            Collections.addAll(list, getUpperMutationPath().split(MC_PATH_DELIM));
            list.remove(Edge.NO_MUTATION_CHARACTER);

            return list;
        }

        private String createUpperMCPath() {

            final StringBuilder stringBuilder = new StringBuilder();
            Node child                        = this;
            Node the_parent                   = child.getParent();

            while (!child.isRoot()) {

                stringBuilder.append(the_parent.getChildEdge(child).mutationCount()
                                     + MC_PATH_DELIM);

                child      = the_parent;
                the_parent = child.getParent();
            }

            if (stringBuilder.length() > 0) {
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            }

            return stringBuilder.toString();
        }

        /**
         * The number of mutations under this node.
         *
         * @return number of mutations under this node.
         */
        public int getLowerMC() {

            int result = 0;

            for (final Node child : getChildren()) {

                result += getChildEdge(child).mutationCount();

                if (!child.isLeaf()) {
                    result += child.getLowerMC();
                }
            }

            return result;
        }

        /**
         * @param childNodeLabel label of the child node
         * @param edgeLabel
         * @return the added child node
         */
        public Node addChild(final String childNodeLabel, final String edgeLabel) {

            final Node child = new Node(this, childNodeLabel);

            children.add(child);
            edges.add(new Edge(this, child, edgeLabel));

            return child;
        }

        /**
         * @param child child to be removed
         */
        public void removeChild(final Node child) {

            if (!children.contains(child)) {
                throw new IllegalArgumentException("Invalid child: ");
            }

            edges.remove(getChildEdge(child));
            children.remove(child);
        }

        /**
         * @return an upper mutation path string with edge labels
         * @deprecated Use {@link #getUpperMCPath()} instead
         */
        public String leafToString() {

            if (!isLeaf()) {
                throw new IllegalArgumentException("allele is not a leaf node");
            }

            final StringBuilder stringBuilder = new StringBuilder();
            Node child                        = this;
            Node the_parent                   = child.getParent();

            while (!child.isRoot()) {

                stringBuilder.append(the_parent.getChildEdge(child).getLabel());

                child      = the_parent;
                the_parent = child.getParent();
            }

            return stringBuilder.toString();
        }

        /**
         * If the node is a leaf, returns the allele (mutation path)
         *
         * @return
         */
        @Override
        public String toString() {
            return isLeaf() ? getUpperMutationPath() : super.toString();
        }

        public String getLabel() {
            return label;
        }

        public List<Node> getChildren() {
            return children;
        }

        public List<Edge> getEdges() {
            return edges;
        }

        /**
         * Method description
         *
         *
         * @param label
         */
        public void setLabel(final String label) {
            this.label = label;
        }

        public boolean isRoot() {
            return parent == null;
        }

        public boolean isWild() {
            return getUpperMC() == 0;
        }

        public boolean isLeaf() {
            return children.isEmpty();
        }

        public Node getParent() {
            return parent;
        }
    }
}
