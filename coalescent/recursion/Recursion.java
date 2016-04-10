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
import coalescent.recursion.listener.exe.ExactRecursionExeEvent;
import coalescent.recursion.listener.exe.ExactRecursionExeListener;
import coalescent.statistic.AC;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static coalescent.recursion.RecursionState.*;
import static java.lang.Thread.currentThread;

/**
 * Recursion of the sample configuration backwards to its MRCA.
 *
 * @author Susanta Tewari
 * @version 1.0.0 Jun 14, 2010
 * @param <L>
 */
public class Recursion<C extends AC<C, ?>, L extends RecursionListener<C>> {

    private final Logger logger = Logger.getLogger(getClass().getName());
    private final C sampleConfig;

    /**
     * @param sampleConfig the sample configuration for which the recursion is run
     */
    public Recursion(final C sampleConfig) {

        this.sampleConfig = sampleConfig;

        initCache();
    }

    /**
     * all the listeners are freed after the recursion is complete and before this method returns
     * @throws InterruptedException
     */
    public final void runRecursion() throws InterruptedException {

        if (listeners.isEmpty()) logger.warning("No computations for the recursion");

        fireEvent(new RecursionEvent(this, INIT_RECURSION, sampleConfig, null, null, null));
        startRecursion();
        fireEvent(new RecursionEvent(this, FINISHED_RECURSION, sampleConfig, null, null, null));
    }

    /**
     * @throws InterruptedException
     */
    protected void startRecursion() throws InterruptedException {
        recurse(sampleConfig);
    }

    /**
     * Implements the recursion
     *
     * @param ac allele spectrum
     * @throws InterruptedException
     */
    private void recurse(final C ac) throws InterruptedException { //J-

        if (ac.isMRCA()) {

            addToCache(ac);

            fire(BOUNDARY_CONDN, ac, null, null, null);

            return;
        }

        fire(ITR_EVENTS_ON, ac, null, null, null);

        for (final EventType type : getSampleConfig().getModel().getEventTypes()) {

            final Iterable<?> alleles = ac.alleles(type); /*---*/

            fire(ITR_EVENT_TYPE_ON, ac, null, null, type);

            for (final Object allele : alleles) {

                final C preAC = ac.apply(allele, type); /*---*/

                fire(PRE_VISIT_AC, ac, preAC, allele, type);

                if (currentThread().isInterrupted()) throw new InterruptedException();

                if (notInCache(preAC)) recurse(preAC); /*---*/

                fire(POST_VISIT_AC, ac, preAC, allele, type);
            }

            fire(ITR_EVENT_TYPE_OFF, ac, null, null, type);
        }

        fire(ITR_EVENTS_OFF, ac, null, null, null);

        addToCache(ac);

        fireExeEvent(); //J+

    }


    // <editor-fold desc="Cache">
    private Map<Integer, Set<C>> cacheMap;

    /**
     * @return
     */
    public Map<Integer, Set<C>> getCacheMap() {
        return cacheMap;
    }

    /**
     * @param aMember
     * @return
     */
    protected Set<C> getAncestralCache(final C aMember) {

        if (aMember.isEventsToMRCABounded()) {

            final Integer index = aMember.eventsToMRCA();

            return cacheMap.get(index);

        } else {
            return cacheMap.get(0);
        }
    }

    private void initCache() {

        int eventsToMRCA = 0;

        if (sampleConfig.isEventsToMRCABounded()) {
            eventsToMRCA = sampleConfig.eventsToMRCA();
        }

        cacheMap = new ConcurrentHashMap<>(eventsToMRCA + 1);

        for (int i = 0; i < eventsToMRCA + 1; i++) {
            cacheMap.put(i, Collections.newSetFromMap(new ConcurrentHashMap<C, Boolean>(10)));
        }
    }

    /**
     * @param s
     */
    protected void addToCache(final C s) {

        if (s.isEventsToMRCABounded()) {

            final Integer index = s.eventsToMRCA();

            cacheMap.get(index).add(s);

        } else {
            cacheMap.get(0).add(s);
        }
    }

    /**
     * @param s
     * @return
     */
    protected boolean isInCache(final C s) {

        if (s.isEventsToMRCABounded()) {

            final Integer index = s.eventsToMRCA();

            return cacheMap.get(index).contains(s);

        } else {
            return cacheMap.get(0).contains(s);
        }
    }

    protected List<Integer> getCacheSize() {

        final Integer[] cacheSize = new Integer[cacheMap.size()];

        for (final Integer level : cacheMap.keySet()) {
            cacheSize[level] = cacheMap.get(level).size();
        }

        return Arrays.asList(cacheSize);

    }    // </editor-fold>


    // <editor-fold desc="Listeners">
    private final Collection<L> listeners                            = new HashSet<>(10);
    private final Collection<ExactRecursionExeListener> exeListeners = new ArrayList<>(10);

    /**
     * @return
     */
    public Collection<L> getListeners() {
        return listeners;
    }

    /**
     * @param event
     */
    protected final void fireEvent(final RecursionEvent event) {

        for (final RecursionListener listener : listeners) {

            switch (event.getRecursionState()) {

                case INIT_RECURSION :
                    listener.onInitRecursion(event);

                    break;

                case BOUNDARY_CONDN :
                    listener.onBoundaryCondn(event);

                    break;

                case ITR_EVENTS_ON :
                    listener.onStartingIteratingEvents(event);

                    break;

                case ITR_EVENT_TYPE_ON :
                    listener.onIteratingSingleEventTypes(event);

                    break;

                case PRE_VISIT_AC :
                    listener.onPreVisitAnsConfig(event);

                    break;

                case POST_VISIT_AC :
                    listener.onPostVisitAnsConfig(event);

                    break;

                case ITR_EVENT_TYPE_OFF :
                    listener.onFinishedIteratingSingleEventTypes(event);

                    break;

                case ITR_EVENTS_OFF :
                    listener.onFinishedIteratingEvents(event);

                    break;

                case FINISHED_RECURSION :
                    listener.onFinishedRecursion(event);

                    break;

                default :
                    Logger.getLogger(this.getClass().getName()).log(
                        Level.WARNING,
                        "" + "Missed enum type in switch statement, needs immediate inspection.");
            }
        }
    }

    /**
     * @param listener
     */
    public void addExactRecursionEventListener(final L listener) {
        listeners.add(listener);
    }

    /**
     * @param listener
     */
    public void removeExactRecursionEventListener(final L listener) {
        listeners.remove(listener);
    }

    /**
     * @param listener
     */
    public void addExactRecursionExeListener(final ExactRecursionExeListener listener) {

        if (exeListeners.contains(listener)) {

            Logger.getLogger(getClass().getName()).log(Level.INFO,
                             "Listener {0} trying to register itself more than once: ", listener);

        } else {
            exeListeners.add(listener);
        }
    }

    /**
     * @param listener
     */
    public void removeExactRecursionExeListener(final ExactRecursionExeListener listener) {
        exeListeners.remove(listener);
    }

    /**
     */
    protected void fireExeEvent() {

        if (exeListeners.isEmpty()) return;


        // estimate cache size
        final List<Integer> cacheSize = getCacheSize();


        // collect the computation chunks
        final List<String> compChunks = new ArrayList<>(listeners.size());

        for (final RecursionListener listener : listeners) {

            if (listener.getUpdateChunk() != null) {
                compChunks.add(listener.getUpdateChunk());
            }
        }

        final ExactRecursionExeEvent event = new ExactRecursionExeEvent(this, cacheSize,
                                                 compChunks);

        for (final ExactRecursionExeListener listener : exeListeners) {
            listener.receivedExactRecursionExeEvent(event);
        }

    }    // </editor-fold>


    // <editor-fold desc="Readability">
    private boolean notInCache(final C s) {
        return !isInCache(s);
    }

    /**
     * improves method recursion().
     */
    private void fire(final RecursionState recursionState, final AC sampleConfig,
                      final AC ancestralConfig, final Object allele, final EventType eventType) {

        fireEvent(new RecursionEvent(this, recursionState, sampleConfig, ancestralConfig, allele,
                                     eventType));
    }


    // </editor-fold>

    /**
     * @return sample configuration
     */
    public C getSampleConfig() {
        return sampleConfig;
    }
}
