
package whitebox.exception_mapping.client;


import javax.jws.WebService;
import javax.jws.Oneway;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Holder;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Deployemnt of this endpoint should fail as Oneway method throws checked exception.
 * @author Rama Pulavarthi
 */
@WebService
public class NegativeEchoImpl {

    @Oneway
    public void notify(String str) throws WebServiceException, RemoteException, EchoException {
        if(str.contains("fault")) {
            throw new RuntimeException("You asked it, you got it");
        } else if(str.contains("remote")) {
            throw new RemoteException("As asked here is the Remote Exception");
        }
        //do nothing
    }

}