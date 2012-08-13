/*
 * Copyright 2004 Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package fromwsdl.mime.text_plain_754.client;

import junit.framework.TestCase;

import javax.xml.ws.Holder;


/**
 * Test case for issue: 754 - tests text/plain in mime binding
 *
 * @author Jitendra Kotamraju
 */
public class HelloLiteralTest extends TestCase {

    private static CatalogPortType port;

    public HelloLiteralTest(String name) throws Exception {
        super(name);
    }

    @Override
    public void setUp() {
        CatalogService service = new CatalogService();
        port = service.getCatalogPort();
    }

    public void testEchoString() throws Exception {
        Holder<String> outStr = new Holder<String>("output");
        Holder<String> att = new Holder<String>();
        port.echoString("input", "attInput", outStr, att);
        assertEquals("output", outStr.value);
        assertEquals("att", att.value);
    }

}
