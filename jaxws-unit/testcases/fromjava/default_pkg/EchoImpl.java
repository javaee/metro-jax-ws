import javax.jws.WebService;

/**
 * Test fromjava Web Service in default package
 */

@WebService(name="Echo", serviceName="EchoService", targetNamespace="http://echo.org/")
public class EchoImpl {
  public int addNumbers(int x, int y) throws AddNumbersException {
      if(x<0 || y<0)
        throw new AddNumbersException("Can't add negative numders", x+ "&" + y);
      return x+y;
  }

}
