package external_customize.server;

import javax.jws.WebService;

@WebService (serviceName = "AddNumbersService", targetNamespace = "http://duke.example.org")
public class AddNumbersImpl {
    
    /**
     * @param number1 must be > 0
     * @param number2 must be > 0
     * @return The sum
     * @throws AddNumbersException
     *             if any of the numbers to be added is negative.
     */
    public int addNumbers (int number1, int number2) throws AddNumbersException {
        if(number1 < 0 || number2 < 0){
            throw new AddNumbersException ("Negative number cant be added!", "Numbers: "+number1+", "+number2);
        }
        return number1 + number2;
    }
    
}
