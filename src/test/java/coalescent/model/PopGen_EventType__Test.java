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
public class PopGen_EventType__Test {

    @Test
    public void test_toString() {

        Assert.assertEquals("C", PopGen_EventType.COALESCENT);

        Assert.assertEquals("M", PopGen_EventType.MUTATION);

        Assert.assertEquals("MG", PopGen_EventType.MIGRATION);
    }
}