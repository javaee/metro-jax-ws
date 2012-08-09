package bugs.jaxws1050.client;

import junit.framework.TestCase;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import static java.io.File.separator;

public class Test extends TestCase {

    public void test() throws URISyntaxException, MalformedURLException {

        // construct URL for wsdl file
        String dir = System.getProperty("user.dir");
        URL url = new File(dir + separator +
                "testcases" + separator +
                "bugs" + separator +
                "jaxws1050" + separator +
                "Service_test.wsdl").toURI().toURL();

        // parse wsdl: fix ensures that it doesn't fail with NPE but warns the problem and continues ...
        Service.create(url, new QName("http://www.tempuri.org/NieuwArtikelService/v1", "NieuwArtikelService"));
    }
}