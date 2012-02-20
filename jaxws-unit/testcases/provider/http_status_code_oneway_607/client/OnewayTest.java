/*
 * $Id: OnewayTest.java,v 1.1 2008-09-03 23:48:52 jitu Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package provider.http_status_code_oneway_607.client;

import java.lang.reflect.Proxy;
import java.io.*;
import junit.framework.*;
import testutil.ClientServerTestUtil;
import testutil.HTTPResponseInfo;
import javax.xml.soap.*;
import javax.xml.namespace.QName;

/**
 *
 * @author Jitendra Kotamraju
 */
public class OnewayTest extends TestCase {

    public OnewayTest(String name) throws Exception {
        super(name);
    }

    Hello getStub() throws Exception {
        return new Hello_Service().getHelloPort();
    }


    /*
     * Check for service's response code. It shouldn't be 202 since service
     * sets a http status code even for oneway
     */
    public void testStatusCode() throws Exception {
        String message = "<s:Envelope xmlns:s='http://schemas.xmlsoap.org/soap/envelope/'><s:Body/></s:Envelope>";
        HTTPResponseInfo rInfo = ClientServerTestUtil.sendPOSTRequest(getStub(),message);
        assertEquals(502, rInfo.getResponseCode());
    }

}
