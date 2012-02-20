package bugs.jaxb274;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import static javax.jws.soap.SOAPBinding.ParameterStyle.WRAPPED;
import static javax.jws.soap.SOAPBinding.Style.DOCUMENT;
import static javax.jws.soap.SOAPBinding.Use.LITERAL;

@WebService(portName="DocLitWrappedPort")
@SOAPBinding(style=DOCUMENT, use=LITERAL ,parameterStyle=WRAPPED)
public class DocLitWrapped {
    public Person[] echo(Person[] people){
        System.out.println("Received array. length="+people.length);
        for (Person p : people) {
            System.out.println(p);
        }
        return people;
    }
}
