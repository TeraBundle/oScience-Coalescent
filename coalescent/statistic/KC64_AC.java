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
import coalescent.data.KC64Data;
import coalescent.model.KC64;
import commons.util.ExceptionUtil;

import java.math.BigDecimal;
import java.util.*;

/**
 * Allele spectrum for data from infinite-alleles model. For a sample of size
 * {@code n}, it is a vector of n-tuples where the {@code i}-th tuple corresponds to the frequency
 * domain {@code i} and its value gives the number of alleles with frequency {@code i} in the
 * sample.<p>
 * This is an immutable class.
 *
 * @author Susanta Tewari
 * @version 1.0.0 Jul 2, 2010
 * @see coalescent.model.KC64 infinite-alleles model(KC64)
 */
@SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
public final class KC64_AC implements AC<KC64_AC, KC64> {

    /**
     * Used in computing the probability. The domain on which the event tok place to create this
     * object.
     */
    private int freqDomain = -1;

    /**
     * index denotes (fi-1) and the value at an index denotes ti. For example, the cannonical string
     * 3^1 can be expressed as [0, 0, 1 and 2^3_4^2 can be expressed as [0, 3, 0, 2].
     */
    private int[] a;

    /** Field description */
    private KC64 model;

    /**
     * A private copy constructor used to provide immutability
     *
     * @param an
     */
    private KC64_AC(final KC64_AC an) {

        this.a     = an.a.clone();
        this.model = an.model;
    }

    public KC64_AC(final KC64Data<?, ?> data) {

        final SortedMap<Integer, Set<String>> map = new TreeMap<>();
        int n                                     = 0;

        for (final String allele : data.getAlleles()) {

            final Integer freq = data.getAlleleFrequency(allele);

            n += freq;

            if (!map.containsKey(freq)) map.put(freq, new HashSet<String>());

            map.get(freq).add(allele);
        }

        a = new int[n];

        for (final Integer f : map.keySet()) {
            a[f - 1] = map.get(f).size();
        }

        a          = remove_right_zeroes(a);
        this.model = data.getModel();
    }

    /**
     * Creates an instance from the cannonical string representation of the data. The cannonical
     * string representation is of the form f1^t1_f2^t2... where fi and ti are positive and represent
     * the frequency domain and the alleles on that frequency domain, respectively.
     *
     * @param canForm th cannonical string representation of the data
     */
    public KC64_AC(final KC64 model, final String cannonicalForm) {
        this(KC64Data.ofCannonicalForm(model, cannonicalForm));
    }

    public int[] getAVector() {
        return a;
    }

    @Override
    public KC64 getModel() {
        return model;
    }

    @Override
    public void setModel(final KC64 model) {
        this.model = model;
    }

    /**
     * removes right zeroes
     *
     * @param a
     *
     * @return
     */
    private static int[] remove_right_zeroes(final int[] a) {

        for (int i = a.length - 1; i >= 0; i--) {

            if (a[i] != 0) {
                return Arrays.copyOfRange(a, 0, i + 1);
            }
        }

        return a;
    }

    /**
     * Checks if a mutation event could be defined on it
     *
     * @return true if a mutation event could be defined; false otherwise
     */
    private Boolean isMutable() {
        return a[0] > 0;
    }

    /**
     * Applies mutation event on the copy of the statistic
     *
     * @return modified copy of the statistic
     */
    private KC64_AC applyMutation() {

        assert isMutable() : "applying mutation on a non-mutable state.";

        final KC64_AC clone = new KC64_AC(this);

        clone.a[0]--;


        // note that removing right zeroes is unnecessary for mutation
        return clone;
    }

    /**
     * Provides the frequency domains for the specified event type. The alleles are frequency
     * domains for this configuration.
     *
     *
     * @param type
     * @return frequency domains for the specified event type
     */
    @Override
    public Set<Integer> alleles(final EventType type) {

        if (type == null) {
            ExceptionUtil.throwIllegalArgExNull("type");
        }

        final Set<Integer> result = new HashSet<>(a.length);

        if (type == EventType.COALESCENT) {

            for (Iterator<Integer> it = freqDomainIterator(); it.hasNext(); ) {

                final Integer freq_domain = it.next();

                if (canCoalesce(freq_domain)) {
                    result.add(freq_domain);
                }
            }

            return result;

        } else if (type == EventType.MUTATION) {

            if (isMutable()) {
                result.add(1);
            }

            return result;
        }

        throw new IllegalArgumentException("Unsupported event type: " + type);
    }

    /**
     * @param allele frequency domain
     * @param type
     *
     * @return
     * is not supported by this statistic 3.) {@code allele} and {@code type} are not mutually
     * compatible i.e., the specified allele was not produced via method {@code alleles())
     */
    @Override
    public KC64_AC apply(final Object allele, final EventType type) {

        if (allele == null) {
            ExceptionUtil.throwIllegalArgExNull("allele");
        }

        if (type == null) {
            ExceptionUtil.throwIllegalArgExNull("type");
        }

        if (!(allele instanceof Integer)) {
            throw new IllegalArgumentException("allele " + allele + " is not an integer");
        }


        // cannot throw ClassCastException now
        final Integer actual_allele = (Integer) allele;

        if (!alleles(type).contains(actual_allele)) {

            throw new IllegalArgumentException("allele " + allele + " and event type " + type
                                               + " are not compatible");
        }

        if (type == EventType.COALESCENT) {
            return applyCoalescent(actual_allele);
        } else if (type == EventType.MUTATION) {
            return applyMutation();
        }

        throw new IllegalArgumentException("Unsupported event type: " + type);
    }

    /**
     * Creates the ancestral configuration for the current value of the statistic given the
     * frequency domain the coalescent event took place.
     * @param freqDomain frequency domain
     * @return new instance of the ancestral configuration
     */
    private KC64_AC applyCoalescent(final Integer freqDomain) {

        assert canCoalesce(freqDomain) : "applying coalescence on an incompatible state";

        final KC64_AC clone = new KC64_AC(this);

        if (freqDomain != (clone.a.length + 1)) {
            clone.a[freqDomain - 1]--;
        }

        clone.a[freqDomain - 2]++;

        clone.a          = remove_right_zeroes(clone.a);
        clone.freqDomain = freqDomain;

        return clone;
    }

    /**
     * Checks if a coalescent event could be defined on the given frequency domain
     *
     * @param freqDomain frequency domain on which the check is made
     * @return true if a coalescent event could be defined; false otherwise
     */
    private Boolean canCoalesce(final Integer freqDomain) {
        return (freqDomain >= 2) && (a[freqDomain - 1] > 0);
    }

    /**
     * The probability of observing a coalescent event next among the competing transitionTypes in the model
     * in a sample of given size. It is given by the following formula.
     * (n - 1) / ( n - 1 + theta ) (n = sample size)
     *
     * @return the probability of observing a coalescent event next
     */
    private BigDecimal coalescentProb() {

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
    private BigDecimal mutationProb() {

        final int n        = getN();
        final double theta = model.getMutationRate();

        return new BigDecimal((theta / (n - 1 + theta)));
    }

    @Override
    public BigDecimal transitionProb(final EventType type, final KC64_AC ac) {

        switch (type) {

            case COALESCENT :
                return coalescentProb().multiply(forwardCoalescentProb(ac));

            case MUTATION :
                return mutationProb();

            default :
                throw new IllegalArgumentException("Unsupported event type: " + type);
        }
    }


    // <editor-fold defaultstate="collapsed" desc="Implementation of forwardEventProb">

    /**
     * The conditional probability of observing the current sample configuration given the ancestral
     * configuration under a coalescent event. It is given by the following formula.
     * (j-1)* ( a[j-1] + 1 ) / (n-1)  ( j = frequency domain )
     *
     * @param ancestralConfig ancestral configuration
     * @return the forward conditional probability for coalescent event
     */
    private BigDecimal forwardCoalescentProb(final KC64_AC ancestralConfig) {

        if (ancestralConfig.freqDomain == -1) {
            throw new IllegalArgumentException("Invalid ancestral configuration");
        }

        final double v = ((ancestralConfig.freqDomain - 1)
                          * (a[ancestralConfig.freqDomain - 2] + 1.0)) / (double) (getN() - 1);

        return new BigDecimal(v);
    }

    /**
     * Creates an iterator that traverses the frequency domain (>0) of the allele spectrum from
     * the highest frequency domain to the singletons.
     *
     * @return
     */
    private Iterator<Integer> freqDomainIterator() {

        final List<Integer> frespec = new ArrayList<>();

        for (int i = a.length - 1; i > 0; i--) {

            if (a[i] > 0) {
                frespec.add(i + 1);
            }
        }

        return frespec.iterator();
    }

    /**
     * Provides the number of alleles for the given frequency domain.
     *
     * @param freqDomain the given frequency domain
     * @return the number of alleles for the given frequency domain
     */
    private Integer allelesCount(final Integer freqDomain) {

        if (freqDomain > a.length) {
            return 0;
        }

        return a[freqDomain - 1];

    }    // </editor-fold>

    /**
     * Returns the size of the sample on which the statistic is based.
     *
     * @return the sample size
     */
    @Override
    public Integer getN() {

        int result = 0;

        for (int i = 0; i < a.length; i++) {
            result += (i + 1) * a[i];
        }

        return result;
    }

    /**
     * Provides the number of (distinct) alleles.
     *
     * @return the number of (distinct) alleles
     */
    public Integer getKn() {
        return sumA();
    }

    /**
     * Sums the entries of vector {@code a}. The sum is assumed to fit in an integer.
     *
     * @return the sum of vector {@code a}
     */
    private int sumA() {

        int sum = 0;

        for (final int anA : a) {
            sum += anA;
        }

        return sum;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public Integer eventsToMRCA() {

        final int val = getN() - 1;

        assert val >= 0;

        return val;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public boolean isEventsToMRCABounded() {
        return true;
    }

    /**
     * Compares this allele spectrum to the specified object. The result is <code>true</code> if and
     * only if the argument is not <code>null</code> and is an <code>An</code> that has the
     * same canonical string representation.
     * {@inheritDoc}
     *
     * @param obj The object to compare this <code>An</code> against
     * @return <code>true</code> if the given object represents an <code>An</code> equivalent to
     *         this allele spectrum, <code>false</code> otherwise
     */
    @Override
    public boolean equals(final Object obj) {

        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj.getClass() != getClass()) {
            return false;
        }

        final KC64_AC rhs = (KC64_AC) obj;

        return Arrays.equals(a, rhs.a);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(a);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public String toString() {
        return Arrays.toString(a) + " N: " + getN() + " Kn: " + getKn();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public Boolean isMRCA() {
        return getN() == 1;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public Double probAtMRCA() {

        if (!isMRCA()) {
            throw new IllegalStateException("AC is not an MRCA");
        }

        return 1.0;
    }
}
