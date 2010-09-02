/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.api.ha;

import com.sun.xml.ws.api.ha.HighAvailabilityProvider;
import com.sun.xml.ws.api.ha.HighAvailabilityProvider.StoreType;
import junit.framework.TestCase;
import org.glassfish.ha.store.api.BackingStoreConfiguration;
import org.glassfish.ha.store.api.BackingStoreFactory;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class HighAvailabilityProviderTest extends TestCase {
    
    public HighAvailabilityProviderTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        HighAvailabilityProvider.INSTANCE.initHaEnvironment(null, null);
        
        super.tearDown();
    }

    /**
     * Test of initBackingStoreConfiguration method, of class HighAvailabilityProvider.
     */
    public void testInitBackingStoreConfiguration() {
        System.out.println("initBackingStoreConfiguration");

        BackingStoreConfiguration<String, Integer> result = HighAvailabilityProvider.INSTANCE.initBackingStoreConfiguration("store", String.class, Integer.class);
        assertEquals(null, result.getClusterName());
        assertEquals(null, result.getInstanceName());
        assertEquals("store", result.getStoreName());
        assertEquals(String.class, result.getKeyClazz());
        assertEquals(Integer.class, result.getValueClazz());

        
        HighAvailabilityProvider.INSTANCE.initHaEnvironment("cluster-1", "instance-a");

        result = HighAvailabilityProvider.INSTANCE.initBackingStoreConfiguration("store", String.class, Integer.class);
        assertEquals("cluster-1", result.getClusterName());
        assertEquals("instance-a", result.getInstanceName());
        assertEquals("store", result.getStoreName());
        assertEquals(String.class, result.getKeyClazz());
        assertEquals(Integer.class, result.getValueClazz());
    }

    /**
     * Test of getBackingStoreFactory method, of class HighAvailabilityProvider.
     */
    public void testGetNoopBackingStoreFactory() {
        System.out.println("getBackingStoreFactory");

        BackingStoreFactory explicitNoopFactory = HighAvailabilityProvider.INSTANCE.getBackingStoreFactory(StoreType.NOOP);
        assertNotNull(explicitNoopFactory);

        // retrieving BSF in a non-initialized environment
        BackingStoreFactory implicitNoopFactory = HighAvailabilityProvider.INSTANCE.getBackingStoreFactory(StoreType.IN_MEMORY);
        assertNotNull(implicitNoopFactory);

        assertEquals(explicitNoopFactory.getClass(), implicitNoopFactory.getClass());
    }

    /**
     * Test of isHaEnvironmentConfigured method, of class HighAvailabilityProvider.
     */
    public void testIsHaEnvironmentConfigured() {
        System.out.println("isHaEnvironmentConfigured");
        // initial state
        HighAvailabilityProvider instance = HighAvailabilityProvider.INSTANCE;
        assertFalse(instance.isHaEnvironmentConfigured());

        // attempt to init with nulls
        instance.initHaEnvironment(null, null);
        assertFalse(instance.isHaEnvironmentConfigured());

        instance.initHaEnvironment(null, "instance-a");
        assertTrue(instance.isHaEnvironmentConfigured());

        instance.initHaEnvironment("cluster-1", null);
        assertTrue(instance.isHaEnvironmentConfigured());

        // regular init
        instance.initHaEnvironment("cluster-1", "instance-a");
        assertTrue(instance.isHaEnvironmentConfigured());
    }

}
