
package fromjava.nosei.server;


@javax.xml.ws.WebFault
public class Exception1 extends Exception {
    String faultString;

    public Exception1(String faultString) {
        this.faultString = faultString;
    }

    public String getFaultString() {
        return faultString;
    }

    public boolean isValid() {
        return true;
    }
}
