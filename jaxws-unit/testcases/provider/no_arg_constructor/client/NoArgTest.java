/*
 * $Id: NoArgTest.java,v 1.1 2009-07-29 22:25:11 jitu Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package provider.no_arg_constructor.client;

import junit.framework.*;
import testutil.ClientServerTestUtil;
import testutil.HTTPResponseInfo;

/**
 *
 * @author Jitendra Kotamraju
 */
public class NoArgTest extends TestCase {

    public NoArgTest(String name) throws Exception {
        super(name);
    }

    Hello getStub() throws Exception {
        return new Hello_Service().getHelloPort();
    }

    public void testSource() throws Exception {
        String message = "<s:Envelope xmlns:s='http://schemas.xmlsoap.org/soap/envelope/'><s:Body/></s:Envelope>";
        // running multiple times so that service returns SAXSource(),
        // DOMSource(), StreamSource()
        for(int i=0; i < 3; i++) {
            HTTPResponseInfo rInfo = ClientServerTestUtil.sendPOSTRequest(getStub(),message);
            assertEquals(200, rInfo.getResponseCode());
        }
    }

}
