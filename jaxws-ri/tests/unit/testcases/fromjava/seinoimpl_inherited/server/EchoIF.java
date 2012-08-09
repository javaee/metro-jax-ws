
package fromjava.seinoimpl_inherited.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.jws.WebService;

/**
 * @author JAX-RPC Development Team
 */
@WebService
public interface EchoIF extends EchoIF1, Remote {
    public String echoString(String str) throws RemoteException;
}

