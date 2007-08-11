
package fromjava.seinoimpl_inherited.server;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * @author JAX-RPC Development Team
 */
public interface EchoIF1 extends Remote {
    public Bar echoBar(Bar bar) throws RemoteException;
}

