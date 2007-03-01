package fromjava_soap12.server;

import javax.jws.WebService;
import javax.xml.ws.BindingType;

@WebService
@BindingType(value="http://java.sun.com/xml/ns/jaxws/2003/05/soap/bindings/HTTP/")
public class AddNumbersImpl {
    
    /**
     * @param number1 must be > 0
     * @param number2 must be > 0
     * @return The sum
     * @throws AddNumbersException
     *             if any of the numbers to be added is negative.
     */
    public int addNumbers (int number1, int number2) throws AddNumbersException {
        if (number1 < 0 || number2 < 0) {
            throw new AddNumbersException ("Negative number cant be added!",
                "Numbers: " + number1 + ", " + number2);
        }
        return number1 + number2;
    }
}
