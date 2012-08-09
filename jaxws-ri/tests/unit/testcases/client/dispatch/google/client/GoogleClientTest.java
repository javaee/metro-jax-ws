package client.dispatch.google.client;

import junit.framework.TestCase;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;

/**
 * RESTful client for Google Base web service.
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
public class GoogleClientTest extends TestCase {
    public void testGoogleClient() throws Exception {

        // Create resource representation
        URI address = new URI("http", null,
            "www.google.com", 80,
            "/base/feeds/snippets",
            "bq=sun+tech+days", null);

        System.out.println("Getting URL = '" + address + "' ...");

        // Create Dispatch object and invoke WS
        Dispatch<Source> d = createDispatch(address);
        Source result = d.invoke(null);

        // Output result returned from service
        outputSource(result);
    }

    private Dispatch<Source> createDispatch(URI uri) {
        // Create service and port to obtain Dispatch instance
        Service s = javax.xml.ws.Service.create(
                new QName("http://google.com", "google"));
        QName portName = new QName("http://google.com", "port");
        s.addPort(portName, HTTPBinding.HTTP_BINDING,
            uri.toString());

        // Create Dispatch instance and setup HTTP headers
        Dispatch<Source> d = s.createDispatch(portName,
                Source.class, Service.Mode.PAYLOAD);
        setupHTTPHeaders(d);
        return d;
    }

    private void setupHTTPHeaders(Dispatch<Source> d) {
        Map<String, Object> requestContext = d.getRequestContext();

        // Set HTTP operation to GET
        requestContext.put(MessageContext.HTTP_REQUEST_METHOD, new String("GET"));

        // Setup HTTP headers as required by service
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put("X-Google-Key",
            Arrays.asList("key=ABQIAAAA7VerLsOcLuBYXR7vZI2NjhTRERdeAiwZ9EeJWta3L_JZVS0bOBRIFbhTrQjhHE52fqjZvfabYYyn6A"));
        headers.put("Accept", Arrays.asList("application/atom+xml"));
        headers.put("Content-Type", Arrays.asList("application/atom+xml"));
        requestContext.put(MessageContext.HTTP_REQUEST_HEADERS, headers);
    }

    private void outputSource(Source s) throws Exception {
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty("indent", "yes");
        File output = new File("google.xml");
        t.transform(s, new StreamResult(output));
        System.out.println("Output written to '" + output.toURL() + "'");
        System.out.println("Done.");
    }
}
