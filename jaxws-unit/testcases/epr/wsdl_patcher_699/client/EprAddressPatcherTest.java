package epr.wsdl_patcher_699.client;

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;


/**
 * Tests if wsa:Address is patched correctly
 *
 * @author Jitendra Kotamraju
 */
public class EprAddressPatcherTest extends TestCase {
    Hello_Service service;

    public EprAddressPatcherTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        service = new Hello_Service();
    }

    public void testAddress() throws Exception {
        URL url = service.getWSDLDocumentLocation();
        BufferedReader rdr = new BufferedReader(new InputStreamReader(url.openStream()));
        String DONOT_MODIFY = "DONOT_MODIFY_THIS";
        String MODIFY = "REPLACE_WITH_URL";
    }

}
