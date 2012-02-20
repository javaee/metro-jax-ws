package fromjava.innerclass;

import javax.jws.WebService;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author JAX-RPC Development Team
 */
@WebService(targetNamespace="portypeNamespace")
public interface EchoIF extends Remote {
    public Bar echoBar(Bar bar) throws RemoteException;
    public String echoString(String str) throws RemoteException;
}
