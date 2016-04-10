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
import coalescent.model.PopGenModel;

import java.math.BigDecimal;

/**
 * The {@code AC} class defines a recursion on an associated sample configuration and
 * calculates the related probabilities.<p> <b>Future Changes:</b> <ul> <li> Class will be renamed
 * to Recursion <li> Method {@code getN()} renamed to {@code getAncestorCount()} </ul>
 * <b>Recursion:</b><br> Recursion is a quintessential part of computation in population genetics.
 * The recursion on the associated sample configuration is essentially a Markov Chain (MC) on the
 * state space of all possible sample configurations. Underlying this MC is a continuous time markov
 * process which is a continuous time approximation over the discrete generations through the
 * history of evolution. Each step in the MC is dictated by a population genetic event under the
 * model. It is represented by the method {@code applyEvent(EventType)} which generates the possible
 * states for the specified event type. The probability of this event type is given by {@code
 * eventProb(EventType, Model)} which uses the associated model to compute the probability. Method
 * {@code forwardEventProb(EventType, AncestralConfig)} computes the probability (conditional and
 * forward in time) of the associated sample configuration given that the specified type of the
 * event took place on the specified ancestral sample configuration.<p> For some models, the
 * absorbing state can be found in a finite number of steps and calculated analytically. This is
 * checked by the method {@code isEventsToMRCABounded()}. Typically, an absorbing state is the one
 * for which the probability can be computed analytically. The method {@code eventsToMRCA()}
 * calculates the number of steps till the MRCA. The absorbing state is synonymous with the Most
 * Recent Common Ancestor (MRCA) of the statistic. The method {@code isMRCA()} checks if the current
 * configuration is at the absorbing state and {@code probAtMRCA()} computes the probability at the
 * MRCA.<p> <b>Optimization:</b><br> The recursion involves same sample configurations many times
 * over. It would be wasteful to do the computations afresh on these same configurations. The
 * dynamic algorithm that is used to implement the recursion stores computations on the previous
 * sample configurations. This brings us to the issue of identity. To avoid recomputing, the
 * implementing class must correctly implement (override) the {@code equals()} and {@code
 * hashcode()} methods. Note that while not implementing these methods should only affect
 * performance not the correctness of the computations involved (except any explicit computation
 * that uses these methods-e.g., counting the distinct ancestral configurations) but a partial or
 * wrong implementation will lead to unpredictable results. <p> <b>Design Patterns:</b><br> The
 * implementing classes should be immutable. They should also define the {@code toString} method to
 * provide a summary description of the statistic. All the description must be in a line and the
 * description should not end with a new line. <b>Notes to implementing classes:</b><br> It is
 * important that the runtime type of the implementing class is same as the type parameter {@code
 * T}. The following code snippet shows how this can be achieved by the implementing classes.
 * <pre>
 *   {@code
 *     MyStatistic implements AC<MyStatistic>
 *    }
 * If the implementing class is abstract then it should be defined as follows.
 *   {@code
 *     MyStatistic<T extends MyStatistic> implements AC<T>
 *    }
 * </pre>
 *
 * @param <T> subtype of {@code AC}
 * @param <M> subtype of {@code PopGenModel}
 * @author Susanta Tewari
 * @since 1.0.0 Aug 20, 2010
 */
@SuppressWarnings("JavaDoc")
public interface AC<T extends AC<T, M>, M extends PopGenModel> {

    /**
     * Provides an iterable over the alleles that can undergo the specified event type.
     *
     * @param type the type of the event acting on this sample configuration
     * @return alleles that can undergo the specified event type
     * @throws NullPointerException if {@code type} is {@code null}
     * @throws IllegalArgumentException if {@code type} is not supported by this statistic
     */
    Iterable<? extends Object> alleles(EventType type);

    /**
     * A new configuration is created using the specified allele and event type.
     *
     * @param allele an allele on this configuration
     * @param type event type acting on the compatible (produced via method {@code alleles())
     * allele
     * @return a new configuration created using the specified allele and event type
     * @throws NullPointerException if any of the parameters is {@code null}
     * @throws IllegalArgumentException if 1.) {@code allele} is not of the correct type 2.) {@code
     * type} is not supported by this statistic 3.) {@code allele} and {@code type} are not mutually
     * compatible i.e., the specified allele was not produced via method {@code alleles())
     */
    T apply(Object allele, EventType type);

    /**
     * Computes the transition probability from this element to the specified element {@code transitionElement}.
     * The specified element {@code transitionElement} must be one of the values returned by {@link #transitions(event)}.
     *
     * @param type event type
     * @param ac one of the values returned by {@link #alleles(coalescent.EventType)}
     * @return transition probability from this element to the specified one
     * @throws NullPointerException if any of the parameters is {@code null}
     * @throws IllegalArgumentException if 1) {@code event} is not present in {@link #transitionTypes()} 2) {@code transitionElement}
     * is not present in {@link #transitions(java.lang.Enum event)}
     */
    BigDecimal transitionProb(EventType type, T ac);

    /**
     * A statistic is an MRCA if its probability can be computed analytically i.e., invoking {@code
     * probAtMRCA()} will not throw {@code IllegalStateException}
     *
     * @return {@code true} if this statistic is an MRCA; {@code false} otherwise
     */
    Boolean isMRCA();

    /**
     * @return model for this statistic
     */
    M getModel();

    /**
     * Sets a new model for this statistic
     *
     * @param model new model for this statistic
     * @throws IllegalArgumentException if the model is incompatible
     */
    void setModel(M model);

    /**
     * Computes analytically the probability of the statistic if it is an MRCA.
     *
     * @return the probability of the statistic at the MRCA
     * @throws IllegalStateException
     */
    Double probAtMRCA();

    /**
     * The sample size of the data this staistic is based on.
     *
     * @return the sample size
     */
    Integer getN();

    /**
     * For some models events to MRCA is bounded and for others it is not. It is called bounded when
     * any statistic in the model can reach the MRCA in finite number of steps. Since most of the
     * recursions have boundary conditions at the MRCA, it affects critically to computations based
     * on the statistic.<p>
     *
     * @return {@code true} if the number of events to MRCA is bounded; {@code false} otherwise
     * TODO remove this method. Premature addition.
     */
    boolean isEventsToMRCABounded();

    /**
     * Computes the total number of events needed to reach the MRCA from this sample configuration.
     *
     * @return number of events to MRCA
     * @throws IllegalStateException if {@code isEventsToMRCABounded()} returns {@code false}
     */
    Integer eventsToMRCA();
}
