/*
 * Copyright 2016. All Rights Reserved.
 * TeraBundle Anlytics Pvt. Ltd. http://www.terabundle.com
 * For queries, contact Susanta Tewari at tewaris@terabundle.com.
 */

package coalescent.model;

import com.google.common.collect.ImmutableSet;
import commons.core.Core_0_Model;
import commons.core.Core_6_Dep_Singletons;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Objects;

/**
 * A <a href="https://en.wikipedia.org/wiki/Diffusion_process">diffusion model</a>
 * (continuous time approximation of the discrete population size) of waiting time
 * for the past evolutionary forces, including breeding. <p/>
 *
 * Event types describe the evolutionary forces. The model specifies the probability of the most
 * recent event going backward and is implemented in the corresponding sub-class of
 * {@linkplain AC AC}. <p/>
 *
 * @author Susanta Tewari
 * @version 1.0.0 Jul 15, 2010
 */
public abstract class CoalescentModel implements Core_0_Model {

    
    private final ImmutableSet eventTypes;

    /**
     *
     * @param types event types
     * @throws NullPointerException if, parameter is {@code null}
     * @throws IllegalArgumentException if, {@code types} is empty
     */
    protected CoalescentModel(final EventType... types) {

        Objects.requireNonNull(types);

        if (types.length == 0) throw new IllegalArgumentException("empty types");

        eventTypes = ImmutableSet.copyOf(types);
    }

    /**
     * Event types supported by this model
     *
     * @return supported event types
     */
    public final ImmutableSet<EventType> getEventTypes() {
        return eventTypes;
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this, Core_6_Dep_Singletons.TO_STRING_STYLE).append("events", eventTypes).toString();
    }
}
