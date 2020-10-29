/*
 * Copyright 2016. All Rights Reserved.
 * TeraBundle Anlytics Pvt. Ltd. http://www.terabundle.com
 * For queries, contact Susanta Tewari at tewaris@terabundle.com.
 */

package coalescent.model;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version
 * @author Susanta Tewari
 * @history Created on 12/1/12.
 */
public class CoalescentModel__Test {

    @Test(expected = NullPointerException.class)
    public void test_PopGenModel_failures_null() {
        new CoalescentModel_ERR_NULL();
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_PopGenModel_failures_illegal_arg() {
        new CoalescentModel_ERR_EMPTY();
    }

    @Test
    public void test_getEventTypes() {

        final ImmutableSet<EventType> eventTypes = new DefaultCoalescentModel().getEventTypes();

        assertEquals(1, eventTypes.size());
    }

    @Test
    public void test_toString() throws Exception {
        new DefaultCoalescentModel().toString();
    }



    /**
     *
     */
    private static class DefaultCoalescentModel extends CoalescentModel {

        DefaultCoalescentModel() {
            super(PopGen_EventType.COALESCENT);
        }

        public Double eventProb(final EventType type, final Integer n) {
            return null;
        }
    }

    /**
     *
     */
    private static class CoalescentModel_ERR_EMPTY extends CoalescentModel {

        CoalescentModel_ERR_EMPTY() {
            super();
        }

        public Double eventProb(final EventType type, final Integer n) {
            return null;
        }
    }

    /**
     *
     */
    private static class CoalescentModel_ERR_NULL extends CoalescentModel {

        CoalescentModel_ERR_NULL() {
            super(null);
        }

        public Double eventProb(final EventType type, final Integer n) {
            return null;
        }
    }


}
