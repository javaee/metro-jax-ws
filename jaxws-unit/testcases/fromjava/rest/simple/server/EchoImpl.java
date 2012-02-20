
package fromjava.rest.simple.server;

import javax.jws.*;

import javax.xml.ws.*;

import com.sun.xml.ws.developer.JAXWSProperties;

@WebService
@BindingType(JAXWSProperties.REST_BINDING)
public class EchoImpl {
    public int add(int num1, int num2) {
        return num1+num2;
    }
}
