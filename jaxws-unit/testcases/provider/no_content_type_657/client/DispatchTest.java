/*
 * $Id: DispatchTest.java,v 1.1 2009-03-26 01:53:46 jitu Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package provider.no_content_type_657.client;

import junit.framework.TestCase;

import javax.activation.DataSource;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;
import java.util.Map;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Jitendra Kotamraju
 */
public class DispatchTest extends TestCase {

    public DispatchTest(String name) throws Exception {
        super(name);
    }

    /*
     * Check for service's response code. It shouldn't be 202 since service
     * sets a http status code even for oneway
     */
    public void testNoContentType() throws Exception {
        BindingProvider bp = (BindingProvider)new Hello_Service().getHelloPort();
        String address = (String) bp.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);

        Service service = Service.create(new QName("", ""));
        service.addPort(new QName("",""), HTTPBinding.HTTP_BINDING, address);
        Dispatch<DataSource> d = service.createDispatch(new QName("",""), DataSource.class, Service.Mode.MESSAGE);

        // Set HTTP operation to PUT
        Map<String, Object> requestContext = d.getRequestContext();
        requestContext.put(MessageContext.HTTP_REQUEST_METHOD, "PUT");

        d.invoke(new DataSource() {

            public InputStream getInputStream() throws IOException {
                return null;
            }

            public OutputStream getOutputStream() throws IOException {
                return null;
            }

            public String getContentType() {
                return null;
            }

            public String getName() {
                return null;
            }
        });
    }

}
