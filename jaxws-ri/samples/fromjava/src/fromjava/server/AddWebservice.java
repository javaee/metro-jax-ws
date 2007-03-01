package fromjava.server;

import javax.xml.ws.Endpoint;

public class AddWebservice {
    
    public static void main (String[] args) throws Exception {
        Endpoint endpoint = Endpoint.publish (
            "http://localhost:8080/jaxws-fromjava/addnumbers",
            new AddNumbersImpl ());

        // Stops the endpoint if it receives request http://localhost:9090/stop
        new EndpointStopper(9090, endpoint);
    }
}
