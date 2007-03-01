package supplychain.server;

import javax.xml.ws.Endpoint;

public class WarehouseLightWeight {
    
    public static void main (String[] args) throws Exception {
        Endpoint endpoint = Endpoint.publish(
            "http://localhost:8080/jaxws-supplychain/submitpo",
            new WarehouseImpl ());

		// Stops the endpoint if it receives request http://localhost:9090/stop
		new EndpointStopper(9090, endpoint);
    }
}
