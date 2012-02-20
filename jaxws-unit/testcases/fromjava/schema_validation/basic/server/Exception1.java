
package fromjava.schema_validation.basic.server;


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
