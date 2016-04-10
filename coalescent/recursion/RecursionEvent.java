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

package coalescent.recursion;

import coalescent.EventType;
import coalescent.model.PopGenModel;
import coalescent.statistic.AC;

/**
 * Contains information during the post-order recursion implementation of a sample onfiguration used
 * to notify the interested listeners.
 *
 * @author Susanta Tewari
 * @version 1.3.0 Dec 3, 2010
 */
public class RecursionEvent<C extends AC<C, ?>> {

    private final Recursion<C, ?> src;
    private final RecursionState recursionState;
    private final C sampleConfig;
    private final C ancestralConfig;
    private final Object allele;
    private final EventType eventType;
    private int responseCode;

    /**
     *
     * @param src
     * @param recursionState the type of the recursion event, indicating a particular point of
     * traversal.
     * @param sampleConfig the sample configuration; always non-null
     * @param ancestralConfig
     * @param allele
     * @param eventType
     */
    public RecursionEvent(final Recursion<C, ?> src, final RecursionState recursionState,
                          final C sampleConfig, final C ancestralConfig, final Object allele,
                          final EventType eventType) {

        this.src             = src;
        this.recursionState  = recursionState;
        this.sampleConfig    = sampleConfig;
        this.ancestralConfig = ancestralConfig;
        this.allele          = allele;
        this.eventType       = eventType;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(final int responseCode) {
        this.responseCode = responseCode;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public RecursionState getRecursionState() {
        return recursionState;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public C getSampleConfig() {
        return sampleConfig;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public C getAncestralConfig() {
        return ancestralConfig;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Object getAllele() {
        return allele;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public EventType getEventType() {
        return eventType;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public AC getStatistic() {
        return src.getSampleConfig();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public PopGenModel getModel() {
        return src.getSampleConfig().getModel();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Recursion<C, ?> getSrc() {
        return src;
    }
}
