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

package coalescent.is;

import bd.commons.math.distribution.SampleSpace;
import coalescent.Event;
import coalescent.EventType;
import coalescent.Genealogy;
import coalescent.model.PopGenModel;
import coalescent.statistic.AC;
import commons.is.Factor_Multi;
import commons.is.Proposal_Multi_w_Prob;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

import static commons.core.Singletons.MATHCONTEXT_128_HALF_UP;
import static commons.util.ExceptionUtil.throwArgEx;

/**
 * Proposal for genealogy built in a sequential manner from the sample to the MRCA. Each step in
 * the sequence corresponds to an event in the genealogy.
 *
 * @param <S> sample type
 * @param <M> model type
 * @author Susanta Tewari
 * @version 1.4.2
 * @history First created on 01/25/2014.
 * @since 1.4.2
 */
public class GProposal_Multi<S extends AC<S, M>, M extends PopGenModel>
        implements Proposal_Multi_w_Prob<Genealogy<S, M>> {

    private final S sample;
    protected Map<S, ElementSampler>[] cache;
    private static final Logger LOGGER = Logger.getLogger(GProposal_Multi.class.getName());
    protected final M[] models;
    protected final GProposal<S, M> uProposal;    // univariate proposal

    /**
     * @param proposal
     * @param models
     */
    public GProposal_Multi(GProposal<S, M> proposal, M[] models) {

        this.uProposal = proposal;
        this.models    = models.clone();
        this.sample    = proposal.getSample();
        this.cache     = new HashMap[models.length];
    }

    @Override
    public Genealogy<S, M>[] sample() {

        cache = new HashMap[models.length];

        Genealogy<S, M>[] result = new Genealogy[models.length];

        for (int i = 0; i < models.length; i++) {
            result[i] = getSample(i);
        }

        return result;
    }

    protected Genealogy<S, M> getSample(int i) {

        final List<Event<S>> eventChain = new ArrayList<>(sample.eventsToMRCA());
        S config                        = sample;

        cache[i] = new HashMap<>(sample.eventsToMRCA());

        config.setModel(models[i]);

        while (!config.isMRCA()) {

            final ElementSampler elementSampler = createElementSampler(config);

            cache[i].put(config, elementSampler);

            final Element e         = elementSampler.propose();
            final S ancestralConfig = config.apply(e.getAllele(), e.getEventType());

            eventChain.add(new Event<>(config, ancestralConfig, e.getAllele(), e.getEventType()));

            config = ancestralConfig;
        }

        return new Genealogy<>(eventChain);
    }

    @Override
    public BigDecimal[] probability(final Genealogy<S, M>[] x) {

        BigDecimal[] result = new BigDecimal[models.length];

        for (int i = 0; i < models.length; i++) {

            BigDecimal prob = getProbability(x[i], i);

            result[i] = prob;
        }

        return result;
    }

    BigDecimal getProbability(Genealogy<S, M> x, int i) {

        final List<Event<S>> event_chain = x.getEventChain();
        BigDecimal prob                  = BigDecimal.ONE;

        for (final Event<S> event : event_chain) {

            final S config                      = event.getPre();
            final ElementSampler elementSampler = cache[i].get(config);
            final BigDecimal proposal_prob      = elementSampler.getProbability();

            prob = prob.multiply(proposal_prob);
        }

        return prob;
    }

    @Override
    public final Factor_Multi<Genealogy<S, M>> of_Factor() {

        return new Factor_Multi<Genealogy<S, M>>() {

            @Override
            public BigDecimal[] getValue(final Genealogy<S, M>[] x) {
                return factorImpl(x);
            }
            @Override
            public int size() {
                return models.length;
            }
        };
    }

    protected BigDecimal[] factorImpl(final Genealogy<S, M>[] x) {

        final BigDecimal[] result = new BigDecimal[x.length];
        final BigDecimal[] p_val  = probability(x);

        for (int i = 0; i < x.length; i++) {

            final BigDecimal t_val = x[i].probability();

            result[i] = t_val.divide(p_val[i], MATHCONTEXT_128_HALF_UP);
        }

        return result;
    }

    /**
     * A multi-point proposal that reuses sampling points across the model points. {@code NID} is
     * shorthand for {@code Non-Independent}.
     *
     * @param proposal
     * @param models
     * @param <S>
     * @param <M>
     * @return
     */
    public static class NID<S extends AC<S, M>, M extends PopGenModel>
            extends GProposal_Multi<S, M> {

        /**
         * @param proposal
         * @param models
         */
        public NID(GProposal<S, M> proposal, M[] models) {
            super(proposal, models);
        }

        @Override
        public Genealogy<S, M>[] sample() {

            cache = new HashMap[models.length];

            Genealogy<S, M>[] result = new Genealogy[models.length];

            Arrays.fill(result, getSample(0));
            Arrays.fill(cache, cache[0]);

            return result;
        }

        @Override
        protected BigDecimal[] factorImpl(Genealogy<S, M>[] x) {

            final BigDecimal prob = NID.this.getProbability(x[0], 0);
            BigDecimal[] result   = new BigDecimal[models.length];

            for (int i = 0; i < models.length; i++) {

                x[i].setModel(models[i]);

                result[i] = x[i].probability().divide(prob, MATHCONTEXT_128_HALF_UP);
            }

            return result;
        }
    }

    private ElementSampler createElementSampler(final S config) {

        final List<Element> e_list = new ArrayList<>(config.getN());

        for (final EventType type : sample.getModel().getEventTypes()) {

            for (final Object allele : config.alleles(type)) {
                e_list.add(new Element(config, allele, type));
            }
        }

        if (e_list.isEmpty()) {

            LOGGER.severe("config w/ missing alleles. config: " + config);

            throw new IllegalStateException("config w/ missing alleles");
        }

        final Element[] e_array = e_list.toArray((Element[]) Array.newInstance(Element.class, 0));

        return new ElementSampler(e_array);
    }

    /**
     * An element of the proposal. It contains enough information to generate its ancestral and its
     * proposal weight.
     */
    private final class Element {

        private final Object allele;
        private final EventType eventType;
        private final S config;

        protected Element(final S config, final Object allele, final EventType eventType) {

            this.config    = config;
            this.allele    = allele;
            this.eventType = eventType;
        }

        public Object getAllele() {
            return allele;
        }

        public EventType getEventType() {
            return eventType;
        }

        public BigDecimal getWeight() {
            return GProposal_Multi.this.uProposal.proposalWeight(config, allele, eventType);
        }
    }

    /**
     * Generates a random sample from a set of elements with weights.
     * todo scope of optimization: store proposed value and its prob.
     */
    public final class ElementSampler {

        private final Element[] elements;
        private SampleSpace<Element> space;
        private Element sample;

        ElementSampler(final Element[] elements) {

            throwArgEx(elements.length == 0, "empty elements");

            this.elements = elements;
        }

        private SampleSpace<Element> createSampleSpace() {

            final BigDecimal[] proposalWeights = new BigDecimal[elements.length];

            for (int i = 0; i < proposalWeights.length; i++) {
                proposalWeights[i] = elements[i].getWeight();
            }

            return new SampleSpace<>(elements, proposalWeights);
        }

        Element propose() {

            space  = createSampleSpace();
            sample = space.sample();

            return sample;
        }

        BigDecimal getProbability() {
            return space.elementProbability(sample);
        }

        public BigDecimal getWeightSum() {
            return space.getWeightSum();
        }

        @Deprecated
        BigDecimal getProbability(final Object allele, final EventType eventType) {

            for (final Element element : elements) {

                if (element.getAllele().equals(allele) && (element.getEventType() == eventType)) {
                    return createSampleSpace().elementProbability(element);
                }
            }

            throw new IllegalArgumentException(
                "no proposal found with the specified allele and event type");
        }
    }
}
