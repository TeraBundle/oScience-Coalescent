/*
 * Copyright 2020. All Rights Reserved.
 * TeraBundle Analytics Pvt. Ltd. http://www.terabundle.com
 * For queries, contact Dr. Susanta Tewari at tewaris@terabundle.com.
 */

package coalescent.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Susanta Tewari
 * @version version
 * @history Created on 3/27/2017.
 * @since since
 */
public class EventTypeTest {

    @Test
    public void test_eventTypes_toString() {

        Assert.assertEquals("C", EventType.COALESCENT.toString());
        Assert.assertEquals("M", EventType.MUTATION.toString());
        Assert.assertEquals("MG", EventType.MIGRATION.toString());
    }
}