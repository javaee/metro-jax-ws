
package fromjava.seiimpl.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.List;

import javax.jws.WebService;

/**
 * @author JAX-RPC Development Team
 */
@WebService(name="EchoIF")
public interface EchoIF extends Remote {
    public Bar echoBar(Bar bar) throws RemoteException;
    public String echoString(String str) throws RemoteException;
    public List<Bar> echoBarList(List<Bar> list);
}

