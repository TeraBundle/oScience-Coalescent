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
 * @since
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


    // <editor-fold desc="PopGenModel Impls">

    /**
     * Class description
     *
     * @version        Enter version here..., 12/12/03
     * @author         Susanta Tewari
     */
    private static class DefaultCoalescentModel extends CoalescentModel {

        DefaultCoalescentModel() {
            super(EventType.COALESCENT);
        }

        public Double eventProb(final EventType type, final Integer n) {
            return null;
        }
    }

    /**
     * Class description
     *
     * @version        Enter version here..., 12/12/03
     * @author         Susanta Tewari
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
     * Class description
     *
     * @version        Enter version here..., 12/12/03
     * @author         Susanta Tewari
     */
    private static class CoalescentModel_ERR_NULL extends CoalescentModel {

        CoalescentModel_ERR_NULL() {
            super(null);
        }

        public Double eventProb(final EventType type, final Integer n) {
            return null;
        }
    }


    // </editor-fold>
}
