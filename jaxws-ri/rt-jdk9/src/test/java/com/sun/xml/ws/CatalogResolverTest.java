package com.sun.xml.ws;

import com.sun.xml.ws.util.xml.XmlCatalogUtil;
import junit.framework.TestCase;
import org.xml.sax.EntityResolver;

/**
 *
 * @author Roman Grigoriadi
 */
public class CatalogResolverTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testCatalogWithSecurityManager() {
        System.setSecurityManager(new SecurityManager());
        //Assert catalog manager not fails on AccessController checks
        final EntityResolver defaultCatalogResolver = XmlCatalogUtil.createDefaultCatalogResolver();
    }
}
