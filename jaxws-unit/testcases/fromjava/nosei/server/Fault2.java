
package fromjava.nosei.server;


@javax.xml.ws.WebFault(faultBean="fromjava.nosei.server.MyFault2Bean", name="MyFault2", targetNamespace="http://mynamespace")
public class Fault2 extends Exception {
    
    String zing;
    int age;

    public Fault2 (String message, int age, String zing) {
	  super(message);
        this.age = age;
	  this.zing = zing;
    }

    public String getZing() {
        return zing;
    }

    public int getAge() {
        return age;
    }
}
