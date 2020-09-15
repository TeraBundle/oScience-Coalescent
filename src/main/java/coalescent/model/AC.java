/*
 * Copyright 2020. All Rights Reserved.
 * TeraBundle Analytics Pvt. Ltd. http://www.terabundle.com
 * For queries, contact Dr. Susanta Tewari at tewaris@terabundle.com.
 */

package coalescent.model;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * Defines coalescent recursion on ancestral configuration (AC).
 * <p>
 * An AC has a particular subset of events defined in {@linkplain EventType} and its alleles are
 * represented by an unspecified type. See {@linkplain #alleles(EventType)} and {@linkplain
 * #apply(Object, EventType, Object[])}. The co-efficients of the recursion are transition
 * probabilities between successive ACs.
 * </p>
 * <p>
 * Defines MRCA and its probability as the tail value of the recursion.
 * </p>
 * @param <T> subtype of {@code AC}
 * @param <M> subtype of {@code CoalescentModel}
 * @author Susanta Tewari
 * @since 1.0.0 Aug 20, 2010
 */
public interface AC<T extends AC<T, M>, M extends CoalescentModel> {

    /** Recursion - Ancestral Configurations */

    /**
     * Provides an iterable over the alleles that can undergo the specified event type. Since this
     * method is likely to be called frequently, implementations may not do argument checking.
     * Callers must ensure that only valid event types are passed.
     * @param type valid event types under this model; null not allowed
     * @return alleles that can undergo the specified event type
     * @throws NullPointerException if {@code type} is {@code null}
     * @throws IllegalArgumentException if {@code type} is not supported by this statistic
     */
    Iterable<? extends Object> alleles(EventType type);

    /**
     * A new configuration is created using the specified allele and event type. Since this method
     * is likely to be called frequently, implementations may not do argument checking. Callers must
     * ensure that only valid event types are passed.
     * @param allele an allele on this configuration
     * @param type valid event types under this model; null not allowed
     * @param extra {@code null} allowed;
     * @return a new configuration created using the specified allele and event type
     * @throws NullPointerException if any of the parameters is {@code null}
     * @throws IllegalArgumentException if 1.) {@code allele} is not of the correct type 2.) {@code
     * type} is not supported by this statistic 3.) {@code allele} and {@code type} are not mutually
     * compatible i.e., the specified allele was not produced via method {@code alleles()}
     */
    T apply(Object allele, EventType type, Object... extra);

    Iterable<EventType> getEventTypes();


    /**
     * A short-circuit for {@linkplain #alleles(EventType)},
     * {@linkplain #apply(Object, EventType, Object...) apply()} and
     * {@linkplain #getEventTypes()} as these methods are primarily used in its
     * default implementation.
     */
    default Set<T> nextConfigs() {

        final Set<T> result = new HashSet<>(10);

        for (final EventType eventType : getEventTypes()) {

            for (final Object allele : alleles(eventType)) {

                result.add(apply(allele, eventType, null));
            }
        }

        return result;
    }



    /**
     * Jump Prob to the Ancestral Configurations.
     *
     * @param allele
     * @param type
     * @param ac
     * @param model
     * @param eData
     * @return
     */
    BigDecimal jumpProb(Object allele, EventType type, T ac, M model, Object... eData);



    /** MRCA: Most Recent Common Ancestor - Recursion`s Initial Condition */

    /**
     * A statistic is an MRCA if its probability can be computed analytically i.e., invoking {@code
     * probAtMRCA()} will not throw {@code IllegalStateException}
     * @return {@code true} if this statistic is an MRCA; {@code false} otherwise
     */
    Boolean isMRCA();

    /**
     * Computes the total number of events needed to reach the MRCA from this sample configuration.
     * @return number of events to MRCA
     * @throws IllegalStateException if {@code isEventsToMRCABounded()} returns {@code false}
     */
    Integer eventsToMRCA();

    /**
     * Computes analytically the probability using the supplied model. Note, {@code ac} is not
     * modified with the supplied {@code model}.
     * @param model model on which the computation is based
     * @return the probability of the statistic at the MRCA, assuming the supplied model on {@code
     * ac}
     */
    Double probAtMRCA(M model);

}
