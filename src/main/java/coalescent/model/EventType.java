/*
 * Copyright 2020. All Rights Reserved.
 * TeraBundle Analytics Pvt. Ltd. http://www.terabundle.com
 * For queries, contact Dr. Susanta Tewari at tewaris@terabundle.com.
 */

package coalescent.model;

/**
 * An enumeration of all type of population genetic events that are to be modelled and analyzed.<p>
 * A distinct abbreviation is defined for all event types that facilitates in printing.
 * @author Susanta Tewari
 * @since 1.0.3 Oct 8, 2010
 */
public interface EventType<T extends Enum<T> & EventType<T>> {

}
