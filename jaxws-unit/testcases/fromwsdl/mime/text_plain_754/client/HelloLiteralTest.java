/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package fromwsdl.mime.text_plain_754.client;

import junit.framework.TestCase;
import testutil.AttachmentHelper;

import javax.activation.DataHandler;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Holder;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import java.awt.Image;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;


public class HelloLiteralTest extends TestCase {

    private static CatalogPortType port;
    private AttachmentHelper helper = new AttachmentHelper();

    public HelloLiteralTest(String name) throws Exception {
        super(name);
        CatalogService service = new CatalogService();
        port = service.getCatalogPort();
    }

    public void testEchoString() throws Exception {
        Holder<String> outStr = new Holder<String>("output");
        Holder<DataHandler> att = new Holder<DataHandler>(); 
        port.echoString("testing", outStr, att);
    }

}
