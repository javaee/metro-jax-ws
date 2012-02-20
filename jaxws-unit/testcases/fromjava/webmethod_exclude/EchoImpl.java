package fromjava.webmethod_exclude;

import javax.jws.WebService;
import javax.jws.WebMethod;

@WebService(name="Echo", serviceName="EchoService", targetNamespace="http://echo.org/")

public class EchoImpl {
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
