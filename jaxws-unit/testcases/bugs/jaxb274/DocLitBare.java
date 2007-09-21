package bugs.jaxb274;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import static javax.jws.soap.SOAPBinding.ParameterStyle.BARE;
import static javax.jws.soap.SOAPBinding.Style.DOCUMENT;
import static javax.jws.soap.SOAPBinding.Use.LITERAL;

@WebService(portName="DocLitBarePort")
@SOAPBinding(style=DOCUMENT, use=LITERAL ,parameterStyle=BARE)
public class DocLitBare {
    public Person[] echo(Person[] people){
        System.out.println("Received array. length="+people.length);
        for (Person p : people) {
            System.out.println(p);
        }
        return people;
    }
}
