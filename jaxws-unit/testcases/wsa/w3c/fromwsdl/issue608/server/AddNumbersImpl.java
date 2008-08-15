package wsa.w3c.fromwsdl.issue608.server;

import javax.jws.WebService;

/**
 * @author Rama Pulavarthi
 */

@WebService(serviceName="AddNumbersService", portName="AddNumbersPort",
        endpointInterface = "wsa.w3c.fromwsdl.issue608.server.AddNumbersPortType", targetNamespace = "http://example.com/")
public class AddNumbersImpl implements AddNumbersPortType {
    public int addNumbers( int number1, int number2) throws AddNumbersFault_Exception {
        return number1 + number2;
    }


}
