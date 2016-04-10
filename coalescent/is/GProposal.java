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
import commons.is.Factor;
import commons.is.Proposal_w_Prob;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * @history First created on 06/02/2011.
 * @since 1.4.2
 */
public abstract class GProposal<S extends AC<S, M>, M extends PopGenModel>
        implements Proposal_w_Prob<Genealogy<S, M>> {

    private final S sample;
    private Map<S, ElementSampler> cache;
    private static final Logger LOGGER = Logger.getLogger(GProposal.class.getName());
    private long randomSeed;

    /**
     * @param sample sample
     */
    public GProposal(final S sample) {

        this.sample = sample;
        this.cache  = new HashMap<>(sample.eventsToMRCA());
    }

    public final GProposal<S, M> of(final S sample) {

        final GProposal<S, M> caller = this;

        return new GProposal<S, M>(sample) {

            @Override
            protected BigDecimal proposalWeight(S config, Object allele, EventType eventType) {
                return caller.proposalWeight(config, allele, eventType);
            }
        };
    }

    @Override
    public void init() {}

    @Override
    public void clear() {}

    /**
     * Specifies the proposal weight for the given configuration, allele and event type.
     *
     * @param config sample configuration
     * @param allele allele on the current configuration
     * @param eventType the chosen event type
     * @return the corresponding proposal probability
     */
    protected abstract BigDecimal proposalWeight(S config, Object allele, EventType eventType);

    @Override
    public final Genealogy<S, M> sample() {

        cache = new HashMap<>(sample.eventsToMRCA());

        final List<Event<S>> eventChain = new ArrayList<>(sample.eventsToMRCA());
        S config                        = sample;

        while (!config.isMRCA()) {

            final GProposal<S, M> gProposal = of_ConfigProposal(config);
            final ElementSampler eSampler   = of_ElementSampler(config, gProposal);

            cache.put(config, eSampler);

            final Element e = eSampler.propose();
            final S ac      = config.apply(e.getAllele(), e.getEventType());

            eventChain.add(new Event<>(config, ac, e.getAllele(), e.getEventType()));

            config = ac;
        }

        return new Genealogy<>(eventChain);
    }

    protected GProposal<S, M> of_ConfigProposal(final S config) {
        return this;
    }

    @Override
    public final BigDecimal probability(final Genealogy<S, M> x) {

        final List<Event<S>> event_chain = x.getEventChain();
        BigDecimal result                = BigDecimal.ONE;

        for (final Event<S> event : event_chain) {

            final S config                      = event.getPre();
            final ElementSampler elementSampler = cache.get(config);
            final BigDecimal proposal_prob      = getProbability(elementSampler);

            result = result.multiply(proposal_prob);
        }

        return result;
    }

    protected BigDecimal getProbability(ElementSampler eSampler) {
        return eSampler.getProbability();
    }

    @Override
    public Factor<Genealogy<S, M>> of_Factor() {
        return of_Factor(this);
    }

    public static <S extends AC<S, M>, M extends PopGenModel> Factor<Genealogy<S,
                   M>> of_Factor(final Proposal_w_Prob<Genealogy<S, M>> proposal) {

        return new Factor<Genealogy<S, M>>() {

            @Override
            public BigDecimal getValue(final Genealogy<S, M> x) {

                final BigDecimal targetVal     = x.probability();
                final BigDecimal proposalValue = proposal.probability(x);

                return targetVal.divide(proposalValue, MATHCONTEXT_128_HALF_UP);
            }
        };
    }

    public S getSample() {
        return sample;
    }

    @Override
    public void setRandomSeed(long randomSeed) {
        this.randomSeed = randomSeed;
    }

    private ElementSampler of_ElementSampler(final S config, GProposal<S, M> gProposal) {

        final List<Element> e_list = new ArrayList<>(config.getN());

        for (final EventType type : sample.getModel().getEventTypes()) {

            for (final Object allele : config.alleles(type)) {
                e_list.add(new Element(gProposal, config, allele, type));
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
     * An element of the proposal. {@code ElementSampler} proposes an {@code Element} which specifies
     * the proposed allele (on the config) and the related event type. Note that this information is
     * enough to generate the ancestral configuration of {@code config}.
     */
    private static class Element<S extends AC<S, M>, M extends PopGenModel> {

        private final Object allele;
        private final EventType eventType;
        private final S config;
        private GProposal<S, M> gProposal;

        protected Element(GProposal<S, M> gProposal, final S config, final Object allele,
                          final EventType eventType) {

            this.config    = config;
            this.allele    = allele;
            this.eventType = eventType;
            this.gProposal = gProposal;
        }

        /**
         * @return selected allele on the config
         */
        public Object getAllele() {
            return allele;
        }

        /**
         * @return selected event type on the config
         */
        public EventType getEventType() {
            return eventType;
        }

        public BigDecimal getWeight() {
            return gProposal.proposalWeight(config, allele, eventType);
        }
    }

    /**
     * Generates a random sample from a set of elements with weights.
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
