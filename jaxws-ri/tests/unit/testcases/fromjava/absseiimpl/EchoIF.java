
package fromjava.absseiimpl;

import java.rmi.Remote;
import java.rmi.RemoteException;


import javax.jws.WebService;

/**
 * @author Rama Pulavarthi
 */
@WebService(targetNamespace="portypeNamespace")
public interface EchoIF extends Remote {
    public Bar echoBar(Bar bar) throws RemoteException;
    public String echoString(String str) throws RemoteException;
}

