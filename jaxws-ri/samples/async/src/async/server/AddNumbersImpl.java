package async.server;

import javax.jws.WebService;

@WebService (serviceName = "AddNumbersService", targetNamespace = "http://duke.example.org")
public class AddNumbersImpl {
    
    public int addNumbers (int number1, int number2) {
        System.out.println ("Received Request!");
        return number1 + number2;
    }
}
