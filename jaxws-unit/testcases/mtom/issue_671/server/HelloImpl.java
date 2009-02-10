package mtom.issue_671.server;

import javax.jws.WebService;
import javax.xml.ws.soap.MTOM;

/**
 * @author Rama Pulavarthi
 */
@MTOM
@WebService(targetNamespace="http://example.org/mtom", portName="HelloPort", serviceName="HelloService", endpointInterface = "mtom.issue_671.server.Hello")

public class HelloImpl {

    public XDoc xDoc(XDoc doc1) {
        return doc1;

    }

}
