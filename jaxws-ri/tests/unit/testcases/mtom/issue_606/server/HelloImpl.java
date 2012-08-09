package mtom.issue_606.server;

import javax.xml.ws.WebServiceException;
import javax.jws.WebService;
import javax.xml.ws.soap.MTOM;

/**
 * @author Jitendra Kotamraju
 */
@MTOM
@WebService
public class HelloImpl {

    public byte[] echo(String doc, byte[] buf) {
        if (buf == null) {
            throw new WebServiceException("Received buf is null");
        }
        return buf;
    }

}
