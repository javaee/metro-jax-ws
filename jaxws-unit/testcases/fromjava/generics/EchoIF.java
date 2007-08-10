package fromjava.generics;

import javax.jws.WebService;

/**
 * @author JAX-RPC Development Team
 */
@WebService(targetNamespace="portypeNamespace")
public interface EchoIF<T extends Bar>  {
    public T echoBar(T bar);
    public String echoString(String str);
}
