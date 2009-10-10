package provider.fault_detail_676.server;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceProvider;
import java.io.ByteArrayInputStream;

/**
 * @author Jitendra Kotamraju
 */
@WebServiceProvider(targetNamespace="urn:test", serviceName="Hello",
    portName="HelloPort")
public class HelloImpl implements Provider<Source> {

    // sends multiple detail entries in soap fault
    public Source invoke(Source source) {
        String body  =
            "<soap:Fault xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'>"+
                    "<faultcode>soap:Server</faultcode>"+
                    "<faultstring>fault message</faultstring>"+
                    "<faultactor>http://example.org/actor</faultactor>"+
                    "<detail>"+
                    "<entry1>entry1</entry1>"+
                    "<entry2>entry2</entry2>"+
                    "</detail>" +
                    "</soap:Fault>";
        return new StreamSource(new ByteArrayInputStream(body.getBytes()));
    }

}
