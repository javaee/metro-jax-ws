package fromjava.multibytename.test;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * @author Jitendra Kotamraju
 */
@WebService(name="Hello",serviceName="Hello\u00EEService", targetNamespace="http://example.com/Hello")
@SOAPBinding(style=SOAPBinding.Style.RPC)
public class HelloService {
	public int echo(int a) {
		return a;
	}
}

