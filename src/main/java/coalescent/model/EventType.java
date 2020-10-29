/*
 * Copyright 2020. All Rights Reserved.
 * TeraBundle Analytics Pvt. Ltd. http://www.terabundle.com
 * For queries, contact Dr. Susanta Tewari at tewaris@terabundle.com.
 */

package coalescent.model;

/**
 * A functional extension of enumeration of population genetic events that are to be
 * modelled and analyzed.<p>
 *
 * A distinct abbreviation is defined for all event types using {@linkplain #toString()}
 * that facilitates in printing.
 *
 * @param <T> en Enum that extends this type
 * @author Susanta Tewari
 * @since 1.0.3 Oct 16, 2020
 */
public interface EventType<T extends Enum<T> & EventType<T>> {

}
