
package fromjava.nosei_rpclit_apt;


@javax.xml.ws.WebFault(name="fault1",
    targetNamespace="urn:test:types")
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
