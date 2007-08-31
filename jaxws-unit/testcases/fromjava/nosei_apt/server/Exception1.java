
package fromjava.nosei_apt.server;


public class Exception1 extends Exception {
    String faultString;

    public Exception1(String faultString) {
        super(faultString+" Message");
        this.faultString = faultString;
    }

    public String getFaultString() {
        return faultString;
    }

    public boolean isValid() {
        return true;
    }
}
