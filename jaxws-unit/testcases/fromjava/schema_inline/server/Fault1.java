
package fromjava.schema_inline.server;


import javax.xml.ws.WebFault;

/**
 * @author Jitendra Kotamraju
 */
@WebFault(name="fault1", targetNamespace="urn:test:types")
public class Fault1 extends Exception {
    private FooException faultInfo;
    
    
    public Fault1(String message, FooException faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }
    
    public Fault1(String message, FooException faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }
    
    public FooException getFaultInfo() {
        return faultInfo;
    }
}
