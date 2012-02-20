
package fromjava.seinoimpl.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.jws.*;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.*;

/**
 * @author JAX-RPC Development Team
 */
@WebService(name="EchoIF", targetNamespace="http://example.org/")
public interface EchoIF extends Remote {
    public Bar echoBar(Bar bar) throws RemoteException;
    public String echoString(String str) throws RemoteException;
    public void echoIntHolder(Holder<Integer> age);

//    public void echoBogus(int fred);
}

