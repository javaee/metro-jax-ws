package fromwsdl.server;

/*
 * Normally the web service implementation class would implement the endpointInterface class.
 * However, it is not necessary as this sample demonstrates.  It is could to implement the
 * endpointInteface as the compiler will catch errors in the methods signatures of the 
 * implementation class.
 */
@javax.jws.WebService (endpointInterface="fromwsdl.server.AddNumbersPortType")
public class AddNumbersImpl {
    
    /**
     * @param number1
     * @param number2
     * @return The sum
     * @throws AddNumbersException
     *             if any of the numbers to be added is negative.
     */
    public int addNumbers (int number1, int number2)
    throws AddNumbersFault_Exception {
        if (number1 < 0 || number2 < 0) {
            String message = "Negative number cant be added!";
            String detail = "Numbers: " + number1 + ", " + number2;
            AddNumbersFault fault = new AddNumbersFault ();
            fault.setMessage (message);
            fault.setFaultInfo (detail);
            throw new AddNumbersFault_Exception (message, fault);
        }
        return number1 + number2;
    }
    
    /*
     * Simple one-way method that takes an integer.
     */
    public void oneWayInt(int number) {
        System.out.println("Service received: " + number);
    }
    
}
