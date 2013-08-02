package deployment.partial_webxml_multi.server;

import javax.jws.WebService;

/**
 * @author Rama Pulavarthi
 */
@WebService(targetNamespace = "http://com.example.calculator", portName = "CalculatorPort")
public class CalculatorImpl {
    public int add(int i, int j) {
        return i+j;    
    }
}
