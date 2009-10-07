package fromjava.webmethod_exclude1;

import javax.jws.WebService;
import javax.jws.WebMethod;

/**
 * Tests @WebMethod(exclude=true) when one of the method has @WebMethod
 * Test for JAX-WS-789
 * @author Rama.Pulavarthi@sun.com
 */
@WebService(name="Echo", serviceName="EchoService", targetNamespace="http://echo.org/")
public class EchoImpl {
  @WebMethod
  public String echoString(String str) {
      return str;
  }

  @WebMethod(exclude = true)
  private static Handle decodeHandle(byte[] encodedHandle) {
   return null;
  }
  @WebMethod(exclude = true)
  private static byte[] encodeHandle(Handle handle) {
   return handle.decode();
  }


}
